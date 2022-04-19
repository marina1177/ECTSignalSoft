package mpei.mdobro.diploma.domain.print;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mpei.mdobro.diploma.domain.parse.HodographObject;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@RequiredArgsConstructor
public class PaintPlots {

    private final Map<Integer, List<HodographObject>> map;

    public void plotHodographs() {

        for (Map.Entry<Integer, List<HodographObject>> entry : map.entrySet()) {
            int numCharts = 4;
            List<XYChart> charts = new ArrayList<XYChart>();

            XYChart hodographChart = getHodographChart(entry);
            XYChart ImChart = getImPartChart(entry);
            XYChart ReChart = getRePartChart(entry);
            XYChart amplitudeChart = getAmplitudeChart(entry);
//            charts.add(ImChart);
//            charts.add(hodographChart);
//            charts.add(ReChart);

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
                    .displayChartMatrix("deep: "+deep+"%\nfreq: "+entry.getKey() + "kHz");
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
        //series.setFillColor(Color.ORANGE);
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

    public XYChart getAmplitudeChart(Map.Entry<Integer, List<HodographObject>> entry) {


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
            xData.add(o.getDisplacement()*10000);
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


    public void plotPhaseCurves() {
//        for () {
//
//            XYChart chart = new XYChartBuilder().width(800).height(600)
//                    .title("Amplitude curve: " + entry.getKey() + "kHz")
//                    .xAxisTitle("Phase[degree]")
//                    .yAxisTitle("deep[%]")
//                    .build();
//        }
    }

    public void plotPODCurves() {

    }
}