package mpei.mdobro.diploma.domain.research;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PODPrinter {

    void print(Map<Integer, Map<Integer, Double>> freqToDeepToPOD) {
        List<XYChart> charts = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, Double>> freqEntry : freqToDeepToPOD.entrySet()) {

            XYChart chart = getPODChart(freqEntry.getKey(), freqEntry.getValue());

            charts.add(chart);
        }
        for (XYChart chart : charts) {
            new SwingWrapper<XYChart>(chart).displayChart();
        }
    }

    private XYChart getPODChart(Integer freq, Map<Integer, Double> deepToPOD) {
        XYChart chart = new XYChartBuilder()
                .title("POD Curve: " + freq + "kHz")
                .xAxisTitle("Deep of defect [%]")
                .yAxisTitle("POD ")
                .width(1000).height(800)
                .build();
        chart.getStyler().setYAxisMin(0.0);
        chart.getStyler().setYAxisMax(1.0);

        XYSeries series;
        Map<Integer, Double> deepPODSorted = new TreeMap<>(deepToPOD);
        deepPODSorted.put(0,0.0);
        List<Integer> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();
        for (Map.Entry<Integer, Double> deepPOD : deepPODSorted.entrySet()) {

           xData.add(deepPOD.getKey());
           yData.add(deepPOD.getValue());
        }
        series = chart.addSeries("limit = 60%", xData, yData);
        series.setMarker(SeriesMarkers.DIAMOND);
        chart.getStyler().setMarkerSize(16);
        series.setLineWidth(5);
        return chart;
    }
}
