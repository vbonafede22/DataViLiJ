
package clustering;

import algorithms.Clusterer;
import data.DataSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 *
 * @author vinbonafede1
 */
public class RandomClusterer extends Clusterer{
    
    private DataSet dataset;
    private final int maxIterations;
    private final int updateInterval;
    private final AtomicBoolean tocontinue;
    int counter = 0;
    private static AtomicBoolean isRunning = new AtomicBoolean(false);
    int runConfig = 0;
    
    ArrayList<LineChart.Series<Number, Number>> series = new ArrayList<>();
       
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
    
     public RandomClusterer(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            int numberOfClusters,
                            boolean tocontinue) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        for(int i = 0; i < numberOfClusters; i++){
            LineChart.Series<Number, Number> series2 = new LineChart.Series<>();
            series2.setName("random clustering " + i);
            series.add(series2);
        }
        runConfig = maxIterations / updateInterval;
    }
     

    @Override
    public void run() {
        isRunning.set(true);
        Platform.runLater( () ->{                  
                    AppUI app = (AppUI) applicationTemplate.getUIComponent();
                    app.getChart().getData().clear();
                    app.getChart().getData().addAll(series);
                    for(int i = 0; i < numberOfClusters; i++){
                        series.get(i).getNode().setVisible(false); 
                    }
                });
        //Iterator itr = dataset.getLocations().values().iterator(); //changes one at a time
        for (int i = 1; i <= runConfig; i++){  //maxIterations in place of runconfig originally
            counter = 0;
            AppUI app = (AppUI) applicationTemplate.getUIComponent();
            System.out.println("bye"); //print as many times as there are iterations
            
            Iterator itr = dataset.getLocations().values().iterator();
            while(itr.hasNext()){ //change to a for loop for one at time
                Point2D x = (Point2D) itr.next();
                int randomVarible = (int) (Math.random() * numberOfClusters); 
                XYChart.Data<Number, Number> d = new XYChart.Data<>(x.getX(), x.getY());               
                Platform.runLater( () ->{
                    series.get(randomVarible).getData().add(d);
                });   
            }
            
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
            AppUI app = (AppUI) applicationTemplate.getUIComponent();
            app.getScrnshotButton().setDisable(false);
            app.continueBtn.setDisable(true);
            app.getRunButton().setDisable(false);
                });
        System.out.println();
    }
    
}
