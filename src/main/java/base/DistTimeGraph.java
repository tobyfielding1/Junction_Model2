package base;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DistTimeGraph extends JFrame {

    int seriesKey = 0;
    XYSeriesCollection data = new XYSeriesCollection();

    public DistTimeGraph(String title) throws HeadlessException {
        super(title);
    }

    public void addSeries(List<Double[]> distTime) {
        XYSeries series = new XYSeries(seriesKey++);
        int i = 0;
        for (Double[] nestedArray : distTime) {
            series.add(nestedArray[0], nestedArray[1]);
        }
        data.addSeries(series);
    }

    public void showChart(String chartTitle) {
        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, "Distance (mass)", "Time (s)", data);
        // we put the chart into a panel
        ChartPanel chartPanel = new ChartPanel(chart);
        // remove legend
        chart.removeLegend();
        // add it to our application
        setContentPane(chartPanel);
    }


}