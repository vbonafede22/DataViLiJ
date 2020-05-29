package ui;

//Vincent Bonafede

import actions.AppActions;
import classification.RandomClassifier;
import clustering.KMeansClusterer;
import clustering.RandomClusterer;
import dataprocessors.AppData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static settings.AppPropertyTypes.ALGO_TYPE;
import static settings.AppPropertyTypes.CLASSIFICATION;
import static settings.AppPropertyTypes.CLASS_ALGO;
import static settings.AppPropertyTypes.CLASS_ALGO_1;
import static settings.AppPropertyTypes.CLUSTERING;
import static settings.AppPropertyTypes.CLUST_ALGO;
import static settings.AppPropertyTypes.CLUST_ALGO_1;
import static settings.AppPropertyTypes.EDIT_DONE;
import static settings.AppPropertyTypes.INSTANCES;
import static settings.AppPropertyTypes.INSTANCES_WITH;
import static settings.AppPropertyTypes.LABELS_ARE;
import static settings.AppPropertyTypes.LABELS_ARE1;
import static settings.AppPropertyTypes.LABELS_LOADED;
import static settings.AppPropertyTypes.RUN;
import static settings.AppPropertyTypes.SCREENSHOT_TOOLTIP;
import vilij.components.ConfirmationDialog;
import vilij.components.ErrorDialog;
import vilij.components.Dialog;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private Button                       runButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private CheckBox                     radioButton;
    private Label                        instLabel;
    private Label                        lblLabel;
    private Label                        fileLabel;
    VBox labelBox = new VBox();
    VBox leftPanel = new VBox(8);
    VBox btnBox = new VBox();
    private Label                        btnBoxLbl;
    private Button                       classificationBtn;
    private Button                       clusteringBtn;
    private Label                        clusteringLbl;
    private Label                        classificationLbl;
    private RadioButton                  randomClusteringAlgo;
    private RadioButton                  randomClassificationAlgo;
    private RadioButton                  kMeansClusteringAlgo;
    VBox classificationBox = new VBox();
    VBox clusteringBox = new VBox();
    VBox algoBox = new VBox();
    VBox metadataBox = new VBox();
    HBox classificationHBox = new HBox();
    HBox randomClusteringHBox = new HBox();
    HBox kMeansClusteringHBox = new HBox();
    private Button                       settingsBtnCluster1;
    private Button                       settingsBtnClass1;
    private Button                       settingsBtnCluster2;
    private final ToggleGroup group = new ToggleGroup();
    public Button  continueBtn;
    
    public LineChart<Number, Number> getChart() { return chart; }

    public TextArea getTextArea() {
        return textArea;
    }

    public Button getScrnshotButton() {
        return scrnshotButton;
    }

    public Button getRunButton() {
        return runButton;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }
    
    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join("/", //separator
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join("/", //separator
                                              iconsPath,
                                              manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                                          manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                                          true); 
        toolBar.getItems().add(scrnshotButton);
        scrnshotButton.setOnAction( e->{
            AppActions app = (AppActions) applicationTemplate.getActionComponent();
            try {
                app.handleScreenshotRequest();
                applicationTemplate.getDialog(Dialog.DialogType.ERROR).show(
                        applicationTemplate.manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()),
                        applicationTemplate.manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()));
            } catch (IOException ex) {
                Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    //@Override;
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setDisable(false);
        newButton.setOnAction(e -> {
            applicationTemplate.getActionComponent().handleNewRequest();
            leftPanel.setVisible(true);
            btnBox.setVisible(false);
            algoBox.setVisible(false);
            metadataBox.setVisible(false);
            fileLabel.setVisible(false);
            scrnshotButton.setDisable(true);
                });
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> {
            leftPanel.setVisible(true);
            labelBox.getChildren().clear();
            applicationTemplate.getActionComponent().handleLoadRequest();
            AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
            Map<String,String> map = dataComponent.getProcessor().getDataLabels();
            Set<String> set = new HashSet<String>();
            for (Map.Entry<String, String> entry : map.entrySet()){
                set.add(entry.getValue());
            }
            AppActions app = (AppActions) applicationTemplate.getActionComponent();
            instLabel.setText(dataComponent.getProcessor().getDataPoints().size() + applicationTemplate.manager.getPropertyValue(INSTANCES_WITH.name()));
            lblLabel.setText(set.size() + applicationTemplate.manager.getPropertyValue(LABELS_LOADED.name()));
            fileLabel.setText(app.getFileName() + applicationTemplate.manager.getPropertyValue(LABELS_ARE.name()));
            fileLabel.setVisible(true);
        for(String s : set){
                Label label = new Label(s);
                labelBox.getChildren().add(label);
            }  
        });    
        exitButton.setOnAction(e -> {
            if(RandomClassifier.getIsRunning() == true ||
                    RandomClusterer.getIsRunning() == true ||
                    KMeansClusterer.getIsRunning() == true){
                ConfirmationDialog dialog  = ConfirmationDialog.getDialog();                        
                dialog.show("Algorithm error", "the algorithm is running, are you sure you want to exit");
                if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
                    applicationTemplate.getActionComponent().handleExitRequest();
                } 
                if(dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL)){
                    dialog.close();
                }
                if(dialog.getSelectedOption().equals(ConfirmationDialog.Option.NO)){
                    dialog.close();
                }
                } else {
                applicationTemplate.getActionComponent().handleExitRequest();
            }
                });
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
    }

    public String getCurrentText() { return textArea.getText(); }
    
    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis      xAxis   = new NumberAxis();
        NumberAxis      yAxis   = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));
        
        xAxis.forceZeroInRangeProperty().setValue(false);
        yAxis.forceZeroInRangeProperty().setValue(false);

        applicationTemplate.getUIComponent().getPrimaryScene().getStylesheets().add("gui/newCascadeStyleSheet1.css");
        
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setAnimated(false);
   
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        Text   leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname       = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize       = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();
        
        radioButton = new CheckBox(manager.getPropertyValue(EDIT_DONE.name())); //hard coded string
         
        instLabel = new Label();        
        lblLabel = new Label();        
        fileLabel = new Label();      
        
        btnBoxLbl = new Label(manager.getPropertyValue(ALGO_TYPE.name())); //hardcoded string
        classificationBtn = new Button(manager.getPropertyValue(CLASSIFICATION.name()));
        clusteringBtn = new Button(manager.getPropertyValue(CLUSTERING.name()));
        
        String iconsPath = "/" + String.join("/", //separator
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String settingsIconPath = String.join("/", //separator
                                              iconsPath,
                                              manager.getPropertyValue(AppPropertyTypes.SETTINGS_ICON.name()));
        settingsBtnCluster1 = new Button();
        settingsBtnCluster1.setDisable(true);
        settingsBtnCluster1.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(settingsIconPath))));
         
        settingsBtnCluster1.setOnAction( e->{ 
            do{
                ClusteringConfigWindow.getDialog().show("Algorithm run configuration", "hi" ); //hard coded string
            } while(!ClusteringConfigWindow.isAnInt(ClusteringConfigWindow.getDialog().getIntervalTxtFld()) || 
                    Double.valueOf(ClusteringConfigWindow.getDialog().getIntervalTxtFld()) <= 0 ||
                    !ClusteringConfigWindow.isAnInt(ClusteringConfigWindow.getDialog().getInterationTxtFld()) ||
                    Double.valueOf(ClusteringConfigWindow.getDialog().getInterationTxtFld()) <= 0 ||
                    !ClusteringConfigWindow.isAnInt(ClusteringConfigWindow.getDialog().getClusterTextFld()) ||
                    Double.valueOf(ClusteringConfigWindow.getDialog().getClusterTextFld()) <= 0);
        });
        
        settingsBtnCluster2 = new Button();
        settingsBtnCluster2.setDisable(true);
        settingsBtnCluster2.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(settingsIconPath))));
        
        settingsBtnCluster2.setOnAction( e->{ 
            do{
                ClusteringConfigWindow.getDialog().show("Algorithm run configuration", "hi" ); //hard coded string
            } while(!ClusteringConfigWindow.isAnInt(ClusteringConfigWindow.getDialog().getIntervalTxtFld()) || 
                    Double.valueOf(ClusteringConfigWindow.getDialog().getIntervalTxtFld()) <= 0 ||
                    !ClusteringConfigWindow.isAnInt(ClusteringConfigWindow.getDialog().getInterationTxtFld()) ||
                    Double.valueOf(ClusteringConfigWindow.getDialog().getInterationTxtFld()) <= 0 ||
                    !ClusteringConfigWindow.isAnInt(ClusteringConfigWindow.getDialog().getClusterTextFld()) ||
                    Double.valueOf(ClusteringConfigWindow.getDialog().getClusterTextFld()) <= 0);
        });
        
        settingsBtnClass1 = new Button();
        settingsBtnClass1.setDisable(true);
        settingsBtnClass1.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(settingsIconPath))));
        
        settingsBtnClass1.setOnAction( e->{
            do{
                ClassificationConfigWindow.getDialog().show("Algorithm Run configuration", "hi"); //hard coded string
            } while(!ClassificationConfigWindow.isAnInt(ClassificationConfigWindow.getDialog().getIntervalTxtFld()) || 
                    Double.valueOf(ClassificationConfigWindow.getDialog().getIntervalTxtFld()) <= 0 ||
                    !ClassificationConfigWindow.isAnInt(ClassificationConfigWindow.getDialog().getInterationTxtFld()) ||
                    Double.valueOf(ClassificationConfigWindow.getDialog().getInterationTxtFld()) <= 0);
        });
        
        classificationLbl = new Label(manager.getPropertyValue(CLASS_ALGO.name())); //hard coded string
        randomClassificationAlgo = new RadioButton();
        randomClassificationAlgo.setToggleGroup(group);
        randomClassificationAlgo.setText("Random Classification Algorithm"); //hardcoded string manager.getPropertyValue(CLASS_ALGO_1.name())
        classificationHBox.getChildren().addAll(randomClassificationAlgo, settingsBtnClass1);
        classificationBox.getChildren().addAll(classificationLbl, classificationHBox);
        classificationBox.setVisible(false);
        
        classificationBtn.setOnAction( e->{
            //btnBox.setVisible(false);
            classificationBox.setVisible(true);
            clusteringBox.setVisible(false);
        });
        
        clusteringLbl = new Label(manager.getPropertyValue(CLUST_ALGO.name()));
        randomClusteringAlgo = new RadioButton();
        randomClusteringAlgo.setToggleGroup(group);
        randomClusteringAlgo.setText("Random Clustering Algorithm"); //manager.getPropertyValue(CLUST_ALGO_1.name())
        kMeansClusteringAlgo = new RadioButton();
        kMeansClusteringAlgo.setToggleGroup(group);
        kMeansClusteringAlgo.setText("K Means Clustering Algorithm"); //manager.getPropertyValue(CLUST_ALGO_1.name())
        randomClusteringHBox.getChildren().addAll(randomClusteringAlgo, settingsBtnCluster1);
        kMeansClusteringHBox.getChildren().addAll(kMeansClusteringAlgo, settingsBtnCluster2);
        clusteringBox.getChildren().addAll(clusteringLbl, randomClusteringHBox, kMeansClusteringHBox);
        clusteringBox.setVisible(false);
        
        clusteringBtn.setOnAction( e->{
            //btnBox.setVisible(false);
            clusteringBox.setVisible(true);
            classificationBox.setVisible(false);
        });
        
        String runIconPath = String.join("/", //separator
                                              iconsPath,
                                              manager.getPropertyValue(AppPropertyTypes.RUN_ICON.name()));
        runButton = new Button();
        runButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(runIconPath))));
        runButton.setTooltip(new Tooltip(manager.getPropertyValue(RUN.name()))); //hard coded string
        runButton.setDisable(true);
        
        continueBtn = new Button("Countinue"); //hard coded string, mess with visablity
        continueBtn.setDisable(true);
        
        group.selectedToggleProperty().addListener((obs) ->{
            runButton.setDisable(false);
            settingsBtnClass1.setDisable(false);
            settingsBtnCluster1.setDisable(false); 
            settingsBtnCluster2.setDisable(false);
        });
        
        btnBox.getChildren().addAll(btnBoxLbl, classificationBtn, clusteringBtn);
        btnBox.setVisible(false);       
        
        algoBox.getChildren().addAll(btnBox, classificationBox, clusteringBox, runButton, continueBtn);
        algoBox.setVisible(false);
        
        metadataBox.getChildren().addAll(instLabel, lblLabel, fileLabel);
        metadataBox.setVisible(false);       
        
        leftPanel.getChildren().addAll(leftPanelTitle, textArea, radioButton, metadataBox, labelBox, algoBox); 
        leftPanel.setVisible(false);

        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
        setReadOnly();
        //setRunBtnActions();
    }
    
//    private void setRunBtnActions(){ // doesnt work great
//        textArea.setOnKeyReleased(e->{
//            if(textArea.getText().matches("^[a-zA-Z0-9_]*$")){
//                runButton.setDisable(true);
//                System.out.println("hi");
//            } else {
//                runButton.setDisable(false);
//                System.out.println("bye");
//            }
//        });
//    }
    
    private void setReadOnly(){
    radioButton.setOnAction(e->{
        labelBox.getChildren().clear();
        if(radioButton.isSelected()){
        textArea.setDisable(true);
        AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
            try {
                dataComponent.clear();
                dataComponent.getProcessor().processString(textArea.getText());
            } catch (Exception ex) {
                //not catching the exception
            }
            Map<String,String> map = dataComponent.getProcessor().getDataLabels();
            
            Set<String> set = new HashSet<String>();
            for (Map.Entry<String, String> entry : map.entrySet()){
                set.add(entry.getValue());
            }
            AppActions app = (AppActions) applicationTemplate.getActionComponent();
            instLabel.setText(dataComponent.getProcessor().getDataPoints().size() + applicationTemplate.manager.getPropertyValue(INSTANCES.name())); //hard coded string
            lblLabel.setText(set.size() + applicationTemplate.manager.getPropertyValue(LABELS_ARE1.name()));
        for(String s : set){
                Label label = new Label(s);
                labelBox.getChildren().add(label);
            }  
        btnBox.setVisible(true);
        algoBox.setVisible(true);
        metadataBox.setVisible(true);
           if(set.size() != 2){ 
                classificationBtn.setDisable(true);
           } else { 
                classificationBtn.setDisable(false);
           }
        } else {
            textArea.setDisable(false);
            algoBox.setVisible(false);
            metadataBox.setVisible(false);
            } 
        });
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                    if (newValue.charAt(newValue.length() - 1) == '\n' || newValue.isEmpty())
                        hasNewText = true;
                    newButton.setDisable(false);
                    saveButton.setDisable(false);
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });
    }
    
    public void setSaveButtonActions() {
            if(hasNewText){ 
               saveButton.setDisable(true);
           }  
    }
    
        private void setDisplayButtonActions() {
        runButton.setOnAction(event -> {
            if (hasNewText) {
                try {
                    if(randomClassificationAlgo.isSelected()){
                        randomClassificationStart();
                    } else if (randomClusteringAlgo.isSelected()){
                        randomClusteringStart();
                    } else if (kMeansClusteringAlgo.isSelected()){
                        kMeansClusteringStart();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
        
        public void displayPoints(){
                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    dataComponent.loadData(textArea.getText());
                    dataComponent.displayData();                    
        }
        
        public void randomClassificationStart(){
            if(ClassificationConfigWindow.getDialog().getInterationTxt() == 1 &&
                        ClassificationConfigWindow.getDialog().getIntervalTxt() == 1){
                        ErrorDialog dialog  = ErrorDialog.getDialog();                        
                        dialog.show("Configureation error", "You did not select configuration components");                        
                    } else {
                    AppActions actions = (AppActions) applicationTemplate.getActionComponent();
                    actions.runRandomClassificationAlgo();
                    displayPoints();
                    }
        }
        
        public void randomClusteringStart(){
            if(ClusteringConfigWindow.getDialog().getInterationTxt() == 1 &&
                        ClusteringConfigWindow.getDialog().getIntervalTxt() == 1 &&
                        ClusteringConfigWindow.getDialog().getClusterTxt() == 1){
                        ErrorDialog dialog  = ErrorDialog.getDialog();                        
                        dialog.show("Configureation error", "You did not select configuration components");                        
                    } else if(ClusteringConfigWindow.getDialog().getClusterTxt() < 2 ||
                            ClusteringConfigWindow.getDialog().getClusterTxt() > 4){
                        ErrorDialog dialog  = ErrorDialog.getDialog();                        
                        dialog.show("Configureation error", "Please choose a clustering value between 2 and 4");     
                    } else { 
                        AppActions actions = (AppActions) applicationTemplate.getActionComponent();
                        actions.runRandomClusteringAlgo();
                        displayPoints();
            }
        }
        
        public void kMeansClusteringStart(){
            if(ClusteringConfigWindow.getDialog().getInterationTxt() == 1 &&
                        ClusteringConfigWindow.getDialog().getIntervalTxt() == 1 &&
                        ClusteringConfigWindow.getDialog().getClusterTxt() == 1){
                        ErrorDialog dialog  = ErrorDialog.getDialog();                        
                        dialog.show("Configureation error", "You did not select configuration components");                        
                    } else if(ClusteringConfigWindow.getDialog().getClusterTxt() < 2 ||
                            ClusteringConfigWindow.getDialog().getClusterTxt() > 4){
                        ErrorDialog dialog  = ErrorDialog.getDialog();                        
                        dialog.show("Configureation error", "Please choose a clustering value between 2 and 4");     
                    } else {
                        AppActions actions = (AppActions) applicationTemplate.getActionComponent();
                        actions.runKMeansClusteringAlgo();
                        displayPoints();
            }
        }
}
