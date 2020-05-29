package clustering;

import algorithms.Clusterer;
import data.DataSet;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    private DataSet       dataset;
    private List<Point2D> centroids;

    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    int counter = 0;
    private static AtomicBoolean isRunning = new AtomicBoolean(false);
    int runConfig = 0;
    
    private ApplicationTemplate applicationTemplate;

    public void setApplicationTemplate(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    public KMeansClusterer(DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, boolean tocontinue) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        runConfig = maxIterations / updateInterval;
    }

    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }
    
    public static boolean getIsRunning() {
        return isRunning.get();
    }

    @Override
    public void run() {
        isRunning.set(true);
        AppUI app = (AppUI) applicationTemplate.getUIComponent();
        initializeCentroids();
        int iteration = 0;
        for (int i = 1; i <= runConfig; i++) { //while (iteration++ < maxIterations) //& tocontinue.get()
            counter = 0;
            assignLabels();
            recomputeCentroids();
            Platform.runLater( () -> {
                app.getChart().getData().clear();
                runHelper();
        });
            
            try {
                    Thread.sleep(500);                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(RandomClusterer.class.getName()).log(Level.SEVERE, null, ex);
                }
            
            Platform.runLater(() ->{
                    app.getScrnshotButton().setDisable(false);
                    app.continueBtn.setDisable(false);
                    app.getRunButton().setDisable(true);
                });
            
            while(!tocontinue()){
                    app.continueBtn.setOnAction( e->{
                        counter++;
                    });
                    if(counter == 1){
                        break;
                    }               
                }
            
            Platform.runLater(() ->{
                    app.getScrnshotButton().setDisable(true);
                    app.continueBtn.setDisable(true);
                    app.getRunButton().setDisable(true);
                }); 
        }
        isRunning.set(false);
        Platform.runLater(() ->{
            app.getScrnshotButton().setDisable(false);
            app.continueBtn.setDisable(true);
            app.getRunButton().setDisable(false);
                });
    }
    
    public void runHelper(){
        AppUI app = (AppUI) applicationTemplate.getUIComponent();
        Set<String> labels = new HashSet<>(dataset.getLabels().values());
        Map<XYChart.Data<Number, Number>, String> dataLabel = new HashMap<>();
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);            
            dataset.getLabels().entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataset.getLocations().get(entry.getKey());
                XYChart.Data<Number, Number> d = new XYChart.Data<>(point.getX(), point.getY());
                series.getData().add(d);               
                dataLabel.put(d, entry.getKey());
            });            
            app.getChart().getData().add(series);
            series.getNode().setVisible(false);
        }  
    }

    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i)))
                ++i;
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        //tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance      = Double.MAX_VALUE;
            int    minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));            
        });
    }

    private void recomputeCentroids() {
        //tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                                 .entrySet()
                                 .stream()
                                 .filter(entry -> i == Integer.parseInt(entry.getValue()))
                                 .map(entry -> dataset.getLocations().get(entry.getKey()))
                                 .reduce(new Point2D(0, 0), (p, q) -> {
                                     clusterSize.incrementAndGet();
                                     return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                                 });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                //tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }
    
}