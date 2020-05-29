/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static settings.AppPropertyTypes.ALGO_RUN_CONFIG;
import static settings.AppPropertyTypes.CLOSE;
import static settings.AppPropertyTypes.CONT_RUN;
import static settings.AppPropertyTypes.MAX_ITERATIONS;
import static settings.AppPropertyTypes.NUM_CLUSTERS;
import static settings.AppPropertyTypes.ONE;
import static settings.AppPropertyTypes.UPDATE;
import vilij.components.Dialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

/**
 *
 * @author vinbonafede1
 */
public class ClusteringConfigWindow extends Stage implements Dialog{
    
    private TextField interationTxtFld;
    private TextField intervalTxtFld;
    private TextField clusterTextFld;
    private CheckBox runCb;

    public String getInterationTxtFld() {
        return interationTxtFld.getText();
    }

    public String getIntervalTxtFld() {
        return intervalTxtFld.getText();
    }

    public String getClusterTextFld() {
        return clusterTextFld.getText();
    }
    
    public Double getInterationTxt() {
        return Double.valueOf(interationTxtFld.getText());
    }

    public Double getIntervalTxt() {
        return Double.valueOf(intervalTxtFld.getText());
    }
    
    public Double getClusterTxt(){
        return Double.valueOf(clusterTextFld.getText());
    }
    
    public Boolean getRunCb() {
        return runCb.isSelected();
    }

    private static ClusteringConfigWindow dialog;

    private Label errorMessage = new Label();

    private ClusteringConfigWindow() {/* empty constructor */ }

    public static ClusteringConfigWindow getDialog() {
        if (dialog == null)
            dialog = new ClusteringConfigWindow();
        return dialog;
    }

    private void setErrorMessage(String message) {
        this.errorMessage.setText(message);
    }
    
    public static boolean isAnInt(String isInt){
        try{
            Double.valueOf(isInt);
        } catch(Exception e){
            return false;
        }
        return true;
    }

    /**
     * Completely initializes the error dialog to be used.
     *
     * @param owner the window on top of which the error dialog window will be displayed
     */
    @Override
    public void init(Stage owner) {
        initModality(Modality.WINDOW_MODAL); // modal => messages are blocked from reaching other windows
        initOwner(owner);
        
        Label titleLbl = new Label("Algorithm Run Configuration"); //hard coded strings
        Label iterationLbl = new Label("Max. Iterations:");
        Label intervalLbl = new Label("Update Interval:");
        Label contRunLbl = new Label("Continous run:");
        Label clusterLbl = new Label("Number of clusters:");
        interationTxtFld = new TextField("1");
        intervalTxtFld = new TextField("1");
        clusterTextFld = new TextField("1");
        runCb = new CheckBox();
        Button closeBtn = new Button("close");
        VBox mainPane = new VBox();
        VBox leftPane = new VBox();
        VBox rightPane = new VBox();
        HBox paneContainer = new HBox();

        
        leftPane.getChildren().addAll(iterationLbl, intervalLbl, clusterLbl, contRunLbl);
        leftPane.setPadding(new Insets(20,20,20,20));
        leftPane.setAlignment(Pos.CENTER);
        leftPane.setSpacing(23);
        rightPane.getChildren().addAll(interationTxtFld, intervalTxtFld, clusterTextFld, runCb);
        rightPane.setPadding(new Insets(10,10,10,10));
        rightPane.setAlignment(Pos.CENTER);
        rightPane.setSpacing(10);
        paneContainer.getChildren().addAll(leftPane, rightPane);
        paneContainer.setPadding(new Insets(10,10,10,10));
        mainPane.getChildren().addAll(titleLbl, paneContainer, closeBtn);
        mainPane.setPadding(new Insets(10,10,10,10));
        
        closeBtn.setOnAction(e -> this.close());

        Scene messageScene = new Scene(mainPane);
        this.setScene(messageScene);
    }

    /**
     * Loads the specified title and message into the error dialog and then displays the dialog.
     *
     * @param errorDialogTitle the specified error dialog title
     * @param errorMessage     the specified error message
     */
    @Override
    public void show(String errorDialogTitle, String errorMessage) {
        setTitle(errorDialogTitle);    // set the title of the dialog
        setErrorMessage(errorMessage); // set the main error message
        showAndWait();                 // open the dialog and wait for the user to click the close button
    }
}
