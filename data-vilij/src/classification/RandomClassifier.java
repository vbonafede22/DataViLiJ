package classification;

import algorithms.Classifier;
import data.DataSet;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    private final int maxIterations;
    private final int updateInterval;
    private double yIntercept;
    private double slope;
    int counter = 0;
    private static AtomicBoolean isRunning = new AtomicBoolean(false);

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;
    
    private ApplicationTemplate applicationTemplate;

    public void setApplicationTemplate(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public static boolean getIsRunning() {
        return isRunning.get();
    }

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
    }

    @Override
    public void run() {
        isRunning.set(true);
        for (int i = 1; i <= maxIterations; i++) { //&& tocontinue()
            
            int xCoefficient =  new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant     = RAND.nextInt(11);
            
            counter = 0;
            
            AppUI app = (AppUI) applicationTemplate.getUIComponent();
            
            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);                     

            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i % updateInterval == 0) {               
                
                System.out.printf("Iteration number %d: ", i); //
                flush();                       
                
                try {
                Thread.sleep(500);
                Platform.runLater(() ->{           
                    app.getChart().getData().clear();   
                    app.displayPoints();
                    createLine();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
            } 
              
                slopeIntercept(xCoefficient, yCoefficient, constant);
                
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
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }    
                
                Platform.runLater(() ->{
                    app.getScrnshotButton().setDisable(true);
                    app.continueBtn.setDisable(true);
                    app.getRunButton().setDisable(true);
                });
            }           
            
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                
                System.out.printf("Iteration number %d: ", i);               
                flush();                 
                try {
                Thread.sleep(500);
                Platform.runLater(() ->{  
                    app.getScrnshotButton().setDisable(false);
                    app.continueBtn.setDisable(false);
                    app.getRunButton().setDisable(false);
                    app.getChart().getData().clear();                  
                    app.displayPoints();
                    createLine();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }   
                slopeIntercept(xCoefficient, yCoefficient, constant);                               
                break;
            }     
            
        } 
        isRunning.set(false);
        Platform.runLater(() ->{
            AppUI app = (AppUI) applicationTemplate.getUIComponent();
            app.getScrnshotButton().setDisable(false);
            app.continueBtn.setDisable(true);
            app.getRunButton().setDisable(false);
                });
    }
    
    public void slopeIntercept(double xCoefficent, double yCoefficent, double constant){
        slope = -xCoefficent/yCoefficent;
        yIntercept = constant/yCoefficent;
        System.out.println("slope: " + slope + "    yIntercept: " + yIntercept);
    }
   
    public void createLine(){ //XYChart<Number, Number> chart
        LineChart.Series<Number, Number> series2 = new LineChart.Series<>();
        series2.setName("Classification line"); //hard coded string
        double maxValue = 0;
        Double minValue = Double.MAX_VALUE;
        for(String key: dataset.getLocations().keySet()){
            Point2D point = dataset.getLocations().get(key);
                if ((point.getX()) > maxValue) {
                    maxValue = (point.getX());
                }
                if ((point.getX()) < minValue) {
                    minValue = (point.getX());
                }
            }
        double upperYCoordinate = slope * maxValue + yIntercept;
        double lowerYCoordinate = slope * minValue + yIntercept;
            series2.getData().add(new XYChart.Data (maxValue, upperYCoordinate));
            series2.getData().add(new XYChart.Data (minValue, lowerYCoordinate));
            AppUI app = (AppUI) applicationTemplate.getUIComponent();
            app.getChart().getData().add(series2);
                for(LineChart.Data<Number, Number> data : series2.getData()){
                    StackPane stackPane = (StackPane) data.getNode();
                    stackPane.setVisible(false);
                }
    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }

    /** A placeholder main method to just make sure this code runs smoothly */
    public static void main(String... args) throws IOException {
        DataSet          dataset    = DataSet.fromTSDFile(Paths.get("/Users/vinbonafede1/NetBeansProjects/vbonafede/hw1/data-vilij/resources/data/sample-data.tsd"));
        RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true);
        classifier.run(); // no multithreading yet
    }
}