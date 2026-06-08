package com.example.elearn.ui.dashboard;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for PieChartView arc angle calculation.
 * Verifies that paid + free sweep angles always sum to 360 degrees.
 */
class PieChartViewTest {

    @Test
    void arcAngles_sumTo360_whenBothSegmentsPresent() {
        PieChartView view = new PieChartView(null);
        view.setData(33.3f, 66.7f);
        float sum = view.getPaidSweepAngle() + view.getFreeSweepAngle();
        assertEquals(360f, sum, 0.01f);
    }

    @Test
    void arcAngles_sumTo360_when100PercentPaid() {
        PieChartView view = new PieChartView(null);
        view.setData(100f, 0f);
        assertEquals(360f, view.getPaidSweepAngle(), 0.01f);
        assertEquals(0f, view.getFreeSweepAngle(), 0.01f);
    }

    @Test
    void arcAngles_sumTo360_when100PercentFree() {
        PieChartView view = new PieChartView(null);
        view.setData(0f, 100f);
        assertEquals(0f, view.getPaidSweepAngle(), 0.01f);
        assertEquals(360f, view.getFreeSweepAngle(), 0.01f);
    }

    @Test
    void arcAngles_bothZero_whenNoData() {
        PieChartView view = new PieChartView(null);
        view.setData(0f, 0f);
        assertEquals(0f, view.getPaidSweepAngle(), 0.01f);
        assertEquals(0f, view.getFreeSweepAngle(), 0.01f);
    }

    @Test
    void arcAngles_equalSplit() {
        PieChartView view = new PieChartView(null);
        view.setData(50f, 50f);
        assertEquals(180f, view.getPaidSweepAngle(), 0.01f);
        assertEquals(180f, view.getFreeSweepAngle(), 0.01f);
    }

    /**
     * Property-based test: for any non-negative paid and free percentages
     * where at least one is positive, the arc angles must sum to 360.
     */
    @Property(tries = 1000)
    void arcAngles_alwaysSumTo360(
            @ForAll @FloatRange(min = 0f, max = 100f) float paidPercent,
            @ForAll @FloatRange(min = 0f, max = 100f) float freePercent) {

        Assume.that(paidPercent + freePercent > 0f);

        PieChartView view = new PieChartView(null);
        view.setData(paidPercent, freePercent);

        float sum = view.getPaidSweepAngle() + view.getFreeSweepAngle();
        assertEquals(360f, sum, 0.01f,
                "Paid=" + paidPercent + ", Free=" + freePercent + " => angles sum=" + sum);
    }
}
