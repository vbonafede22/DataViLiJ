package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class NameException extends Exception {

        public NameException(String name) {
            super(name);
        }
    }
    
    public static class InvalidDataNameException extends NameException {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }
    
    public static class DuplicateNameException extends NameException {

        private static final String NAME_ERROR_MSG = "";

        public DuplicateNameException(String name) {
            super(String.format("Duplicate name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;

    public Map<String, String> getDataLabels() {
        return dataLabels;
    }

    public Map<String, Point2D> getDataPoints() {
        return dataPoints;
    }
    
    public TSDProcessor() {
        dataLabels = new LinkedHashMap<>();
        dataPoints = new LinkedHashMap<>();
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    
    public String string;
    
    public void processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  try {
                      atomicInteger.getAndIncrement();
                      String   name  = checkedname(list.get(0));
                      String   label = list.get(1);
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      dataLabels.put(name, label);
                      dataPoints.put(name, point);
                      //System.out.println(atomicInteger.get());
                  } catch (NameException e){
                      errorMessage.setLength(0);
                      errorMessage.append(", error occured at line " + atomicInteger.get() + " because of " + e.getMessage()); //hard coded string
                      //applicationTemplate.manager.getPropertyValue(ERROR_LINE.name())
                      hadAnError.set(true);
                  } catch (Exception e) {
                      //System.out.println(atomicInteger.get());
                      errorMessage.setLength(0);
                      errorMessage.append(", error occured at line " + atomicInteger.get()); //hard coded string 
                      hadAnError.set(true);
                  } 
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    
    void toChartData(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        Map<XYChart.Data<Number, Number>, String> dataLabel = new HashMap<>();
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);            
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                XYChart.Data<Number, Number> d = new XYChart.Data<>(point.getX(), point.getY());
                series.getData().add(d);               
                dataLabel.put(d, entry.getKey());
            });            
            chart.getData().add(series);
            series.getNode().setVisible(false);
            for(XYChart.Data<Number, Number> d : series.getData()){
                Tooltip.install(d.getNode(), new Tooltip(dataLabel.get(d)));
                d.getNode().setOnMouseEntered(mouseListener);
            }
        }  
    }
    
    EventHandler<MouseEvent> mouseListener = (MouseEvent event) -> {
        ((Node)(event.getSource())).setCursor(Cursor.HAND);
    };
    
    public void toChartDataHelper(XYChart<Number, Number> chart){
        LineChart.Series<Number, Number> series2 = new LineChart.Series<>();
        series2.setName("Average"); //hard coded string
        double sum = 0;
        double maxValue = 0;
        Double minValue = Double.MAX_VALUE;
        for(String key: dataPoints.keySet()){
            Point2D point = dataPoints.get(key);
                sum += ((point.getY()));
                if ((point.getX()) > maxValue) {
                    maxValue = (point.getX());
                }
                if ((point.getX()) < minValue) {
                    minValue = (point.getX());
                }
            }
            double ave = (sum/(dataPoints.size()));
            //System.out.println(dataPoints.size()); //number of instances 
            series2.getData().add(new LineChart.Data (minValue, ave));
            series2.getData().add(new LineChart.Data (maxValue, ave));
            chart.getData().add(series2);
                for(LineChart.Data<Number, Number> data : series2.getData()){
                    StackPane stackPane = (StackPane) data.getNode();
                    stackPane.setVisible(false);
                }
    }
    
    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name) throws NameException {
        if (!name.startsWith("@")){
            throw new InvalidDataNameException(name);
        }
        
        if (dataPoints.containsKey(name)){
            throw new DuplicateNameException(name);
        }
        return name;
    }  
}
