package com.example.elearn.ui.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom View that draws a line chart with gradient fill below the line.
 * Supports setting data points and x-axis labels.
 */
public class LineChartView extends View {

    private float[] dataValues;
    private String[] dataLabels;

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int LINE_COLOR = 0xFF6366F1;
    private static final int FILL_COLOR_START = 0x806366F1;
    private static final int FILL_COLOR_END = 0x006366F1;
    private static final int GRID_COLOR = 0xFFE5E7EB;
    private static final int LABEL_COLOR = 0xFF6B7280;

    private static final float PADDING_LEFT = 40f;
    private static final float PADDING_RIGHT = 20f;
    private static final float PADDING_TOP = 20f;
    private static final float PADDING_BOTTOM = 40f;

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setColor(LINE_COLOR);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(GRID_COLOR);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);

        labelPaint.setColor(LABEL_COLOR);
        labelPaint.setTextSize(28f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        dotPaint.setColor(LINE_COLOR);
        dotPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Sets the data for the line chart.
     *
     * @param values array of float values representing data points
     * @param labels array of String labels for the x-axis
     */
    public void setData(float[] values, String[] labels) {
        this.dataValues = values;
        this.dataLabels = labels;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (dataValues == null || dataValues.length == 0) return;

        float width = getWidth();
        float height = getHeight();

        float chartLeft = PADDING_LEFT;
        float chartRight = width - PADDING_RIGHT;
        float chartTop = PADDING_TOP;
        float chartBottom = height - PADDING_BOTTOM;
        float chartWidth = chartRight - chartLeft;
        float chartHeight = chartBottom - chartTop;

        // Find max value for scaling
        float maxValue = 0;
        for (float v : dataValues) {
            if (v > maxValue) maxValue = v;
        }
        if (maxValue == 0) maxValue = 1;
        maxValue *= 1.2f; // Add some headroom

        // Draw horizontal grid lines
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            float y = chartTop + (chartHeight * i / gridLines);
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);
        }

        // Calculate point positions
        int numPoints = dataValues.length;
        float[] xPoints = new float[numPoints];
        float[] yPoints = new float[numPoints];

        for (int i = 0; i < numPoints; i++) {
            xPoints[i] = chartLeft + (chartWidth * i / (numPoints - 1));
            yPoints[i] = chartBottom - (dataValues[i] / maxValue) * chartHeight;
        }

        // Draw gradient fill below line
        Path fillPath = new Path();
        fillPath.moveTo(xPoints[0], chartBottom);
        for (int i = 0; i < numPoints; i++) {
            fillPath.lineTo(xPoints[i], yPoints[i]);
        }
        fillPath.lineTo(xPoints[numPoints - 1], chartBottom);
        fillPath.close();

        LinearGradient gradient = new LinearGradient(
                0, chartTop, 0, chartBottom,
                FILL_COLOR_START, FILL_COLOR_END,
                Shader.TileMode.CLAMP);
        fillPaint.setShader(gradient);
        canvas.drawPath(fillPath, fillPaint);

        // Draw line
        Path linePath = new Path();
        linePath.moveTo(xPoints[0], yPoints[0]);
        for (int i = 1; i < numPoints; i++) {
            linePath.lineTo(xPoints[i], yPoints[i]);
        }
        canvas.drawPath(linePath, linePaint);

        // Draw dots at data points
        for (int i = 0; i < numPoints; i++) {
            canvas.drawCircle(xPoints[i], yPoints[i], 6f, dotPaint);
        }

        // Draw x-axis labels
        if (dataLabels != null) {
            for (int i = 0; i < Math.min(dataLabels.length, numPoints); i++) {
                canvas.drawText(dataLabels[i], xPoints[i], height - 8f, labelPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = 400; // default height in pixels
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
