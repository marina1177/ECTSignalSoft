package mpei.mdobro.diploma.domain.print;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mpei.mdobro.diploma.domain.parse.DefectTypes;
import mpei.mdobro.diploma.domain.parse.HodographObject;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.lines.SeriesLines;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.PI;

@Data
@Slf4j
@RequiredArgsConstructor
public class PaintPlots {

    private final Map<Integer, List<HodographObject>> map;

    private Map<Integer, Map<Integer, List<HodographObject>>>
            freqToDeepAndLengthAngleLimitList;
    //    private Map<DefectTypes, Map<Integer, Map<Integer, List<HodographObject>>>>
//            defectTypeToFreqToDeepAndLengthAngleModelList;
    private Map<Integer, Map<Integer, List<HodographObject>>> freqToDeepAndLengthAngleModelList;

    public void plotPODCurves() {

    }

    public void plotModelDataAmongLimits() {

        List<XYChart> charts = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, List<HodographObject>>> limitEntry : freqToDeepAndLengthAngleLimitList.entrySet()) {

            XYChart chart = getLengthPhaseChartWithData(limitEntry.getKey(), limitEntry.getValue(),
                    freqToDeepAndLengthAngleModelList.get(limitEntry.getKey()));
            charts.add(chart);
        }
        for (XYChart chart : charts) {
            new SwingWrapper<XYChart>(chart).displayChart();
        }

    }

    private XYChart getLengthPhaseChartWithData(Integer freq,
                                                Map<Integer, List<HodographObject>> deepToLimitPoints,
                                                Map<Integer, List<HodographObject>> deepToDataPoints) {
        XYChart chart = new XYChartBuilder()
                .title("Length-Phase Curve: " + freq + "kHz")
                .xAxisTitle("Length of defect [mm]")
                .yAxisTitle("Phase of defect [deg]")
                .width(600).height(400)
                .build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideSW);
        chart.getStyler().setToolTipsEnabled(true);

        XYSeries seriesLimits;
        for (Map.Entry<Integer, List<HodographObject>> deepHO : deepToLimitPoints.entrySet()) {
            // deepHO: 5 точек для графика
            deepHO.getValue().sort(Comparator
                    .comparing(HodographObject::getDefectLength));

            seriesLimits = fillChart(deepHO.getValue(), chart, "deep = " + deepHO.getKey());
            seriesLimits.setLineStyle(SeriesLines.SOLID);
            seriesLimits.setLineWidth(20);
        }

        ArrayList<String> labels = new ArrayList<>();
        XYSeries seriesData = null;
        //для каждой глубины своя прямая
        for (Map.Entry<Integer, List<HodographObject>> deepHO : deepToDataPoints.entrySet()) {

            deepHO.getValue().sort(Comparator
                    .comparing(HodographObject::getTypeDefect));
            DefectTypes tmpDefectType = deepHO.getValue().get(0).getTypeDefect();
            List<HodographObject> tmpList = new ArrayList<>();

            for (HodographObject point : deepHO.getValue()) {

                if ((point.getTypeDefect() != tmpDefectType)) {

                    seriesData = fillChart(tmpList, chart, "deep = " + deepHO.getKey() +
                            "; type = " + tmpDefectType);
                    labels.add(tmpDefectType.name());
                    tmpList = new ArrayList<>();
                    tmpDefectType = point.getTypeDefect();
                }
                tmpList.add(point);

                if (deepHO.getValue().indexOf(point) == deepHO.getValue().size() - 1) {
                    seriesData = fillChart(tmpList, chart, "deep = " + deepHO.getKey() +
                            "; type = " + tmpDefectType);
                    labels.add(tmpDefectType.name());
                    tmpList = new ArrayList<>();
                    tmpDefectType = point.getTypeDefect();
                }
            }
        }

        return chart;
    }

    private XYSeries fillChart(List<HodographObject> tmpList, XYChart chart, String seriesString) {

        List<Double> xData = tmpList.stream().map(HodographObject::getDefectLength).collect(Collectors.toList());
        List<Double> yData = tmpList.stream().map(o -> o.getComplexNumber().getArgument() * 180 / PI).collect(Collectors.toList());
        XYSeries seriesData = chart.addSeries(seriesString, xData, yData);
        seriesData.setLineStyle(SeriesLines.NONE);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        seriesData.setMarker(getMarker(tmpList.get(0).getTypeDefect()));
        chart.getStyler().setToolTipsEnabled(true);
        chart.getStyler().setMarkerSize(16);
        return seriesData;
    }

    private Marker getMarker(DefectTypes types) {
        if (types.equals(DefectTypes.RECT))
            return SeriesMarkers.RECTANGLE;
        else if (types.equals(DefectTypes.TRI))
            return (SeriesMarkers.TRIANGLE_UP);
        else if (types.equals(DefectTypes.PLGN_W_30)
                || types.equals(DefectTypes.PLGN_W_45)
                || types.equals(DefectTypes.PLGN_W_60)
                || types.equals(DefectTypes.PLGN))
            return (SeriesMarkers.TRAPEZOID);

        return SeriesMarkers.CIRCLE;
    }

    public void plotPhaseLengthCurves() {
        List<XYChart> charts = new ArrayList<>();

        for (Map.Entry<Integer, Map<Integer, List<HodographObject>>> entry : freqToDeepAndLengthAngleLimitList.entrySet()) {
            XYChart chart = getLengthPhaseChart(entry, "linear");
            charts.add(chart);
        }
        for (XYChart chart : charts) {
            new SwingWrapper<XYChart>(chart).displayChart();
        }
    }

    private XYChart getLengthPhaseChart(Map.Entry<Integer, Map<Integer, List<HodographObject>>> entry, String type) {

        XYChart chart = new XYChartBuilder()
                .title("Length-Phase Curve: " + entry.getKey() + "kHz")
                .xAxisTitle("Length of defect [mm]")
                .yAxisTitle("Phase of defect [deg]")
                .width(600).height(400)
                .build();
//        chart.getStyler().setYAxisMin(-180.0);
//        chart.getStyler().setYAxisMax(180.0);

        XYSeries series;
        //для каждой глубины своя прямая
        for (Map.Entry<Integer, List<HodographObject>> deepHO : entry.getValue().entrySet()) {
            // deepHO: 5 точек для графика
            deepHO.getValue().sort(Comparator
                    .comparing(HodographObject::getDefectLength));
            List<Double> xData = deepHO.getValue().stream().map(HodographObject::getDefectLength).collect(Collectors.toList());
            List<Double> yData = deepHO.getValue().stream().map(o -> o.getComplexNumber().getArgument() * 180 / PI).collect(Collectors.toList());

            series = chart.addSeries("deep = " + deepHO.getKey(), xData, yData);
            series.setMarker(SeriesMarkers.CIRCLE);
        }
        return chart;
    }

    public void plotHodographs() {

        for (Map.Entry<Integer, List<HodographObject>> entry : map.entrySet()) {

            List<XYChart> charts = new ArrayList<XYChart>();

            XYChart hodographChart = getHodographChart(entry);
            XYChart ImChart = getImPartChart(entry);
            XYChart ReChart = getRePartChart(entry);
            XYChart amplitudeChart = getAmplitudeChart(entry);
            charts.add(ImChart);
            charts.add(hodographChart);
            charts.add(ReChart);

            charts.add(amplitudeChart);
//            JFrame frame = new JFrame("Chart");
//            frame.getContentPane().add(new ChartPanel(ImChart), BorderLayout.WEST);
//            frame.getContentPane().add(new ChartPanel(ReChart), BorderLayout.EAST);
//            frame.getContentPane().add(new ChartPanel(hodographChart), BorderLayout.CENTER);
//            frame.pack();
//            frame.setVisible(true);
            //new SwingWrapper(chart).displayChart();

            Integer deep = entry.getValue().get(0).getDeep();

            new SwingWrapper<XYChart>(charts)
                    .displayChartMatrix("deep: " + deep + "%\nfreq: " + entry.getKey() + "kHz");
        }
    }


    private XYChart getImPartChart(Map.Entry<Integer, List<HodographObject>> entry) {
        XYChart chartIm = new XYChartBuilder().width(600).height(400)
                .title("Imaginary part: " + entry.getKey() + "kHz")
                .xAxisTitle("displacement[m]")
                .yAxisTitle("Im[V]")
                .build();
        chartIm.getStyler().setPlotGridLinesVisible(true);

        // Series
        List<Double> xData = new ArrayList();
        List<Double> yData = new ArrayList();
        entry.getValue().forEach(o -> {
            xData.add(o.getDisplacement());
            yData.add(o.getComplexNumber().getImaginary());
        });

        chartIm.addSeries(entry.getKey() + "[kHz]", xData, yData)
                .setMarkerColor(Color.RED)
                .setLineColor(Color.ORANGE);
        return chartIm;
    }

    private XYChart getRePartChart(Map.Entry<Integer, List<HodographObject>> entry) {
        XYChart chartRe = new XYChartBuilder().width(600).height(400)
                .title("Real part: " + entry.getKey() + "kHz")
                .xAxisTitle("displacement[m]")
                .yAxisTitle("Re[V]")
                .build();
        chartRe.getStyler().setPlotGridLinesVisible(true);

        // Series
        List<Double> xData = new ArrayList();
        List<Double> yData = new ArrayList();
        entry.getValue().forEach(o -> {
            xData.add(o.getDisplacement());
            yData.add(o.getComplexNumber().getReal());
        });

        chartRe.addSeries(entry.getKey() + "[kHz]", xData, yData)
                .setMarkerColor(Color.ORANGE)
                .setLineColor(Color.GREEN);

        return chartRe;
    }

    private XYChart getHodographChart(Map.Entry<Integer, List<HodographObject>> entry) {
        XYChart chart_hod = new XYChartBuilder().width(600).height(400)
                .title("Hodograph: " + entry.getKey() + "kHz")
                .xAxisTitle("Re[V]")
                .yAxisTitle("Im[V]")
                .build();

        chart_hod.getStyler().setPlotGridLinesVisible(true);
        chart_hod.getStyler().setDatePattern("yyyy");
        // Series
        List<Double> xData = new ArrayList();
        List<Double> yData = new ArrayList();
        entry.getValue().forEach(o -> {
            xData.add(o.getComplexNumber().getReal());
            yData.add(o.getComplexNumber().getImaginary());
        });

        XYSeries series = chart_hod.addSeries(entry.getKey() + "[kHz]", xData, yData);
        series.setFillColor(new Color(230, 150, 150));
        return chart_hod;
    }

    private XYChart getAmplitudeChart(Map.Entry<Integer, List<HodographObject>> entry) {


        XYChart chart = new XYChartBuilder().width(1000).height(1000)
                .title("Real part: " + entry.getKey() + "kHz")
                .xAxisTitle("displacement[m]")
                .yAxisTitle("Amp[V]")
                .build();
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setPlotGridVerticalLinesVisible(true);
        //chart.getStyler()//.
        // Series
        List<Double> xData = new ArrayList();
        List<Double> yData = new ArrayList();
        entry.getValue().forEach(o -> {
            xData.add(o.getDisplacement() * 10000);
            yData.add(o.getComplexNumber().abs());
        });

        Double deltaX = Math.abs(xData.get(0) - xData.get(1));
        //Double deltaY = Math.abs(yData.get(0) - yData.get(1));
        //chart.getStyler().setAxisTickMarkLength(5);
        chart.getStyler().setXAxisTickMarkSpacingHint(deltaX.intValue());

        chart.addSeries(entry.getKey() + "[kHz]", xData, yData)
                .setMarkerColor(Color.RED)
                .setLineColor(Color.RED)
                .getXMax();

        return chart;

    }
}
