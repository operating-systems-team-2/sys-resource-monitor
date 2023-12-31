import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import java.text.SimpleDateFormat;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import javax.swing.*;

public class SystemResourceMonitor extends JFrame {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    private TimeSeries cpuSeries;
    private TimeSeries memorySeries;

    public SystemResourceMonitor(String title) {
        super(title);

        cpuSeries = new TimeSeries("CPU Usage (%)");
        memorySeries = new TimeSeries("Memory Usage (%)");

        createChart();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Clean up resources if needed
            }
        });

        setVisible(true);
    }

    private void createChart() {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(cpuSeries);
        dataset.addSeries(memorySeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "System Resource Monitor",
                "Time",
                "Value",
                dataset,
                true,
                true,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();

        DateAxis domainAxis = new DateAxis("Time");
        domainAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
        plot.setDomainAxis(domainAxis);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero(false);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setContentPane(chartPanel);

        // Start a thread to update the data periodically
        Thread dataUpdateThread = new Thread(() -> {
            while (true) {
                updateData();
                try {
                    Thread.sleep(1000); // Update every second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        dataUpdateThread.start();
    }

    private void updateData() {
        // Fetch real system resource data
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        double cpuUsage = osBean.getSystemCpuLoad() * 100; // CPU usage as a percentage
        double memoryUsage = (1.0 - osBean.getFreePhysicalMemorySize() * 1.0 / osBean.getTotalPhysicalMemorySize()) * 100;

        // Add data points to the series
        Second currentSecond = new Second();
        cpuSeries.addOrUpdate(currentSecond, cpuUsage);
        memorySeries.addOrUpdate(currentSecond, memoryUsage);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SystemResourceMonitor("System Resource Monitor"));
    }
}
