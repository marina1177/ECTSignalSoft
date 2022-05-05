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
            freqToDeepAndLengthAngleList;
    private Map<DefectTypes, Map<Integer, Map<Integer, List<HodographObject>>>>
            defectTypeToFreqToDeepAndLengthAngleModelList;


    public void plotPODCurves() {

    }

    public void plotModelDataAmongLimits() {
//
//        List<XYChart> charts = new ArrayList<>();
//        for (Map.Entry<Integer, Map<Integer, List<HodographObject>>> entry : freqToDeepAndLengthAngleList.entrySet()) {
//
//            XYChart chart = getLengthPhaseChartWithData(entry.getKey(),
//                    freqToDeepAndLengthAngleModelList.get(entry.getKey()),
//                    entry.getValue());
//            charts.add(chart);
//
//        }
//        new SwingWrapper<XYChart>(charts).displayChartMatrix();
    }

    private XYChart getLengthPhaseChartWithData(Integer freq,
                                                Map<Integer, List<HodographObject>> deepToDataPoints,
                                                Map<Integer, List<HodographObject>> deepToLimitPoints) {
        XYChart chart = new XYChartBuilder()
                .title("Length-Phase Curve: " + freq + "kHz")
                .xAxisTitle("Length of defect [mm]")
                .yAxisTitle("Phase of defect [deg]")
                .width(600).height(400)
                .build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideSW);
        chart.getStyler().setToolTipsEnabled(true);

        XYSeries seriesData;
        //для каждой глубины своя прямая
        for (Map.Entry<Integer, List<HodographObject>> deepHO : deepToDataPoints.entrySet()) {
            // deepHO: 5 точек для графика
            deepHO.getValue().sort(Comparator
                    .comparing(HodographObject::getDefectLength));
            List<Double> xData = deepHO.getValue().stream().map(HodographObject::getDefectLength).collect(Collectors.toList());
            List<Double> yData = deepHO.getValue().stream().map(o -> o.getComplexNumber().getArgument() * 180 / PI).collect(Collectors.toList());

            seriesData = chart.addSeries("deep = " + deepHO.getKey() +
                            "; type = " + deepHO.getValue().get(0).getTypeDefect(),
                    xData, yData);
            seriesData.setLineStyle(SeriesLines.NONE);
            chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);

            if (deepHO.getValue().get(0).getTypeDefect().equals(DefectTypes.RECT))
                seriesData.setMarker(SeriesMarkers.RECTANGLE);
            else if (deepHO.getValue().get(0).getTypeDefect().equals(DefectTypes.TRI))
                seriesData.setMarker(SeriesMarkers.TRIANGLE_UP);
            else if (deepHO.getValue().get(0).getTypeDefect().equals(DefectTypes.PLGN_W_30)
                    || deepHO.getValue().get(0).getTypeDefect().equals(DefectTypes.PLGN_W_45)
                    || deepHO.getValue().get(0).getTypeDefect().equals(DefectTypes.PLGN_W_60)
                    || deepHO.getValue().get(0).getTypeDefect().equals(DefectTypes.PLGN))
                seriesData.setMarker(SeriesMarkers.TRAPEZOID);
        }

        XYSeries seriesLimits;
        for (Map.Entry<Integer, List<HodographObject>> deepHO : deepToLimitPoints.entrySet()) {
            // deepHO: 5 точек для графика
            deepHO.getValue().sort(Comparator
                    .comparing(HodographObject::getDefectLength));
            List<Double> xData = deepHO.getValue().stream().map(HodographObject::getDefectLength).collect(Collectors.toList());
            List<Double> yData = deepHO.getValue().stream().map(o -> o.getComplexNumber().getArgument() * 180 / PI).collect(Collectors.toList());

            seriesLimits = chart.addSeries("deep = " + deepHO.getKey(), xData, yData);
            seriesLimits.setMarker(SeriesMarkers.CIRCLE);
        }
        return chart;
    }

    public void plotPhaseLengthCurves() {
        List<XYChart> charts = new ArrayList<>();

        for (Map.Entry<Integer, Map<Integer, List<HodographObject>>> entry : freqToDeepAndLengthAngleList.entrySet()) {
            XYChart chart = getLengthPhaseChart(entry, "linear");
            charts.add(chart);
        }
        new SwingWrapper<XYChart>(charts).displayChartMatrix();
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
    //    private TableXYDataset createTableXYDataset1() {
//        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
//
//        // Series
//        List<Double> xData = new ArrayList();
//        List<Double> yData = new ArrayList();
//        ;
//        dataset.addSeries(s1);
//
//
//        return dataset;
//    }
//
//    private JFreeChart getImPartJChart(Map.Entry<Integer,List<HodographObject>> entry){
//// Series
//        List<Double> xData = new ArrayList();
//        List<Double> yData = new ArrayList();
//        entry.getValue().forEach(o->{
//            xData.add(o.getDisplacement());
//            yData.add(o.getComplexNumber().getImaginary());
//        });
//
//        XYSeries series = chartIm.addSeries(entry.getKey()+"[kHz]", xData, yData);
//        series.setFillColor(Color.ORANGE);
//
//        JFreeChart chartIm =
//                ChartFactory.createHistogram("Imaginary part: "+entry.getKey()+"kHz",
//                        "",
//                        "",
//                        dataset,
//                        PlotOrientation.VERTICAL, true, true, false);
//
//        return chartIm;
//    }

}
