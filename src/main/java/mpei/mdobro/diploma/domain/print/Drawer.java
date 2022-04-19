package mpei.mdobro.diploma.domain.print;

import org.knowm.xchart.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Drawer {

    public void simplePlot() throws IOException {
        double[] xData = new double[] { 0.0, 1.0, 2.0 };
        double[] yData = new double[] { 2.0, 1.0, 0.0 };

// Create Chart
        XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);

// Show it
        new SwingWrapper(chart).displayChart();

// Save it
        BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapEncoder.BitmapFormat.PNG);

// or save it in high-res
        BitmapEncoder.saveBitmapWithDPI(chart, "./Sample_Chart_300_DPI", BitmapEncoder.BitmapFormat.PNG, 300);
    }

    public void simpleChart2(){
        double[] xData=new double[100];
        double[] yData=new double[100];
        for (int i=0;i<100;i++) {
            yData[i]=i+1;
            xData[i]=Math.log(i+1);
        }
        XYChart chart = QuickChart.getChart("Graph", "X", "Y", "y(x)", xData, yData);

        JFrame frame=new JFrame("Main");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton clickMe=new JButton("Click me!");
        clickMe.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayChart(frame, chart);
            }
        });
        frame.setContentPane(clickMe);
        frame.pack();
        frame.setVisible(true);
    }

    public static void displayChart(JFrame owner, XYChart chart) {
        XChartPanel<XYChart> panel=new XChartPanel<XYChart>(chart);
        JDialog d=new JDialog(owner, "Chart");
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setContentPane(panel);
        d.pack();
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    public void simpleChart3(){

    }
}
