package actions;

import classification.RandomClassifier;
import clustering.KMeansClusterer;
import clustering.RandomClusterer;
import data.DataSet;
import dataprocessors.AppData;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import settings.AppPropertyTypes;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;


import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import static settings.AppPropertyTypes.DATA_FILE_EXT;
import static settings.AppPropertyTypes.DATA_FILE_EXT_DESC;
import ui.AppUI;
import ui.ClassificationConfigWindow;
import ui.ClusteringConfigWindow;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;
import static vilij.templates.UITemplate.separator;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;
    private DataSet dataset;
    private ClassificationConfigWindow classConfig = ClassificationConfigWindow.getDialog();
    private ClusteringConfigWindow clusteringConfig = ClusteringConfigWindow.getDialog();

    /** Path to the data file currently active. */
    Path dataFilePath;
    
    //public static final String separator = "/";

    /** The boolean property marking whether or not there are any unsaved changes. */
    SimpleBooleanProperty isUnsaved;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
    }

    public void setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }

    @Override
    public void handleNewRequest() {
        try {
            if (!isUnsaved.get() || promptToSave()) {
                applicationTemplate.getDataComponent().clear();
                applicationTemplate.getUIComponent().clear();
                isUnsaved.set(false);
                dataFilePath = null;
            }
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handleSaveRequest() {
        try {
            promptToSave();
            AppUI app = (AppUI) applicationTemplate.getUIComponent();
            app.setSaveButtonActions();
        } catch (IOException ex) {
            Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, ex);
        }
}  

    String fileName;

    public String getFileName() {
        return fileName;
    }
    
    @Override
    public void handleLoadRequest() {
        // TODO: NOT A PART OF HW 1
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()),
                applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())); //not appearing on in window
        File file = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (file != null) {
                    dataFilePath = file.toPath();
                    fileName = file.getName();
                    AppData app = (AppData) applicationTemplate.getDataComponent();
                    app.loadData(dataFilePath);
            try {
                dataset = DataSet.fromTSDFile(dataFilePath);
                isUnsaved.set(false);
            } catch (IOException ex) {
                Logger.getLogger(AppActions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void fromTextArea(){
        AppUI app = (AppUI) applicationTemplate.getUIComponent();
        if(dataset == null){
            dataset = DataSet.fromTextArea(app.getTextArea().getText());
        }
    }
    
    public void runRandomClassificationAlgo(){
        fromTextArea();
 //        Class alg = Class.forName("clustering.RandomClusterer"); //dont do this here
        RandomClassifier randomClassifier = new RandomClassifier(dataset, (int) classConfig.getInterationTxt().doubleValue(), (int) classConfig.getIntervalTxt().doubleValue(), classConfig.getRunCb());
        randomClassifier.setApplicationTemplate(applicationTemplate);
        Thread thread = new Thread(randomClassifier);
        thread.start();
    }
    
    public void runRandomClusteringAlgo(){
        fromTextArea();
        RandomClusterer randomClusterer = new RandomClusterer(dataset, (int) clusteringConfig.getInterationTxt().doubleValue(), (int) clusteringConfig.getIntervalTxt().doubleValue(), (int) clusteringConfig.getClusterTxt().doubleValue(), clusteringConfig.getRunCb());
        randomClusterer.setApplicationTemplate(applicationTemplate);
        Thread thread = new Thread(randomClusterer);
        thread.start();
    }
    
    public void runKMeansClusteringAlgo(){
        fromTextArea();
        KMeansClusterer kMeansClusterer = new KMeansClusterer(dataset, (int) clusteringConfig.getInterationTxt().doubleValue(), (int) clusteringConfig.getIntervalTxt().doubleValue(), (int) clusteringConfig.getClusterTxt().doubleValue(), clusteringConfig.getRunCb());
        kMeansClusterer.setApplicationTemplate(applicationTemplate);
        Thread thread = new Thread(kMeansClusterer);
        thread.start();
    }
    
    @Override
    public void handleExitRequest() {
        try {
            if (!isUnsaved.get() || promptToSave())
                System.exit(0);
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
        AppUI app = (AppUI) applicationTemplate.getUIComponent();
        WritableImage image = app.getChart().snapshot(new SnapshotParameters(), null);
        File file = new File("chart.png");
        try{
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (Exception e){
                System.out.println(e);
        }
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        ConfirmationDialog dialog  = ConfirmationDialog.getDialog();
        dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if (dialog.getSelectedOption() == null) return false; // if user closes dialog using the window's close button

        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            if (dataFilePath == null) {
                FileChooser fileChooser = new FileChooser();
                String      dataDirPath = "/" + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name()); //separator
                URL         dataDirURL  = getClass().getResource(dataDirPath);
                
                if (dataDirURL == null)
                    
                    throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

                fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

                String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
                String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                                                                String.format("*.%s", extension));

                fileChooser.getExtensionFilters().add(extFilter);
                File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                if (selected != null) {
                    dataFilePath = selected.toPath();
                    save();
                } else return false; // if user presses escape after initially selecting 'yes'
            } else
                save();
        }
        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }

    private void save() throws IOException {
        applicationTemplate.getDataComponent().saveData(dataFilePath);
        isUnsaved.set(false);
    }

    private void errorHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }
}
