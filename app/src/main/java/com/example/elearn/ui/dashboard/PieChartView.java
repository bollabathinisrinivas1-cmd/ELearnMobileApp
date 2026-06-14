package com.example.elearn.ui.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Custom View that draws a two-segment pie chart with value labels.
 * Displays paid users (green) and free users (pink) segments with counts.
 */
public class PieChartView extends View {

    private float paidSweepAngle = 0f;
    private float freeSweepAngle = 0f;
    private int paidCount = 0;
    private int freeCount = 0;
    private int totalCount = 0;

    private final Paint paidPaint;
    private final Paint freePaint;
    private final Paint textPaint;
    private final Paint centerTextPaint;
    private final Paint centerSubTextPaint;
    private final RectF arcBounds = new RectF();

    private static final int PAID_COLOR = 0xFF4CAF50; // Green
    private static final int FREE_COLOR = 0xFFE91E63; // Pink

    public PieChartView(Context context) {
        this(context, null);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paidPaint.setStyle(Paint.Style.FILL);
        paidPaint.setColor(PAID_COLOR);

        freePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        freePaint.setStyle(Paint.Style.FILL);
        freePaint.setColor(FREE_COLOR);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        centerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerTextPaint.setColor(0xFFFFFFFF);
        centerTextPaint.setTextAlign(Paint.Align.CENTER);
        centerTextPaint.setFakeBoldText(true);

        centerSubTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerSubTextPaint.setColor(0xFFFFFFFF);
        centerSubTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Sets the pie chart data with actual counts.
     *
     * @param paidPercent percentage value for paid segment
     * @param freePercent percentage value for free segment
     * @param paid actual paid user count
     * @param free actual free user count
     */
    public void setData(float paidPercent, float freePercent, int paid, int free) {
        this.paidCount = paid;
        this.freeCount = free;
        this.totalCount = paid + free;
        float total = paidPercent + freePercent;
        if (total == 0f) {
            paidSweepAngle = 0f;
            freeSweepAngle = 0f;
        } else {
            paidSweepAngle = (paidPercent / total) * 360f;
            freeSweepAngle = 360f - paidSweepAngle;
        }
        invalidate();
    }

    /**
     * Sets the pie chart data (backward compatible).
     */
    public void setData(float paidPercent, float freePercent) {
        setData(paidPercent, freePercent, (int) paidPercent, (int) freePercent);
    }

    public float getPaidSweepAngle() {
        return paidSweepAngle;
    }

    public float getFreeSweepAngle() {
        return freeSweepAngle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        float padding = size * 0.05f;

        float left = (width - size) / 2f + padding;
        float top = (height - size) / 2f + padding;
        float right = left + size - 2 * padding;
        float bottom = top + size - 2 * padding;
        arcBounds.set(left, top, right, bottom);

        float cx = (left + right) / 2f;
        float cy = (top + bottom) / 2f;
        float radius = (right - left) / 2f;

        // Draw paid segment (starts at top, -90 degrees)
        if (paidSweepAngle > 0f) {
            canvas.drawArc(arcBounds, -90f, paidSweepAngle, true, paidPaint);
        }

        // Draw free segment (starts after paid segment)
        if (freeSweepAngle > 0f) {
            canvas.drawArc(arcBounds, -90f + paidSweepAngle, freeSweepAngle, true, freePaint);
        }

        // Draw value labels on each segment
        textPaint.setTextSize(size * 0.09f);

        // Paid segment label - position at midpoint of paid arc
        if (paidSweepAngle > 20f && paidCount > 0) {
            float paidMidAngle = -90f + paidSweepAngle / 2f;
            float labelRadius = radius * 0.6f;
            float paidLabelX = cx + (float) (labelRadius * Math.cos(Math.toRadians(paidMidAngle)));
            float paidLabelY = cy + (float) (labelRadius * Math.sin(Math.toRadians(paidMidAngle)));
            canvas.drawText(String.valueOf(paidCount), paidLabelX, paidLabelY + textPaint.getTextSize() / 3f, textPaint);
        }

        // Free segment label - position at midpoint of free arc
        if (freeSweepAngle > 20f && freeCount > 0) {
            float freeMidAngle = -90f + paidSweepAngle + freeSweepAngle / 2f;
            float labelRadius = radius * 0.6f;
            float freeLabelX = cx + (float) (labelRadius * Math.cos(Math.toRadians(freeMidAngle)));
            float freeLabelY = cy + (float) (labelRadius * Math.sin(Math.toRadians(freeMidAngle)));
            canvas.drawText(String.valueOf(freeCount), freeLabelX, freeLabelY + textPaint.getTextSize() / 3f, textPaint);
        }

        // Draw total count in center
        if (totalCount > 0) {
            centerTextPaint.setTextSize(size * 0.14f);
            centerSubTextPaint.setTextSize(size * 0.07f);
            canvas.drawText(String.valueOf(totalCount), cx, cy, centerTextPaint);
            canvas.drawText("Students", cx, cy + centerTextPaint.getTextSize() * 0.8f, centerSubTextPaint);
        }
    }
}
