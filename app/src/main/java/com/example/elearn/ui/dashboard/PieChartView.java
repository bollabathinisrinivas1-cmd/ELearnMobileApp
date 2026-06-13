package com.example.elearn.ui.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Custom View that draws a two-segment pie chart using Canvas.drawArc().
 * Displays paid students (blue) and free students (grey) segments.
 */
public class PieChartView extends View {

    private float paidSweepAngle = 0f;
    private float freeSweepAngle = 0f;

    private final Paint paidPaint;
    private final Paint freePaint;
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
    }

    /**
     * Sets the pie chart data and recalculates arc sweep angles.
     * The two segments always sum to 360 degrees.
     *
     * @param paidPercent percentage of paid students (0-100)
     * @param freePercent percentage of free students (0-100)
     */
    public void setData(float paidPercent, float freePercent) {
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
     * Returns the calculated paid segment sweep angle in degrees.
     */
    public float getPaidSweepAngle() {
        return paidSweepAngle;
    }

    /**
     * Returns the calculated free segment sweep angle in degrees.
     */
    public float getFreeSweepAngle() {
        return freeSweepAngle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        float padding = size * 0.1f;

        float left = (width - size) / 2f + padding;
        float top = (height - size) / 2f + padding;
        float right = left + size - 2 * padding;
        float bottom = top + size - 2 * padding;
        arcBounds.set(left, top, right, bottom);

        // Draw paid segment (starts at top, -90 degrees)
        if (paidSweepAngle > 0f) {
            canvas.drawArc(arcBounds, -90f, paidSweepAngle, true, paidPaint);
        }

        // Draw free segment (starts after paid segment)
        if (freeSweepAngle > 0f) {
            canvas.drawArc(arcBounds, -90f + paidSweepAngle, freeSweepAngle, true, freePaint);
        }
    }
}
