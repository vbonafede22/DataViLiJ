package dataprocessors;

import data.DataSet;
import java.io.BufferedReader;
import java.io.FileReader;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.control.TextArea;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;

    public TSDProcessor getProcessor() {
        return processor;
    }

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        // TODO: NOT A PART OF HW  
        String line = null;
            AppUI app = (AppUI) applicationTemplate.getUIComponent();
            app.getTextArea().clear();
            try {
            FileReader fileReader = new FileReader(dataFilePath.toString());
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            TextArea text = ((AppUI) applicationTemplate.getUIComponent()).getTextArea();
            while((line = bufferedReader.readLine()) != null) {
                text.setText(text.getText().concat(line) + "\n");
            }
            app.getChart().getData().clear();
            AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
            dataComponent.clear();
            dataComponent.loadData(app.getTextArea().getText());
            bufferedReader.close();
        } catch(Exception e){
            System.out.println("hi");
        }
    }

    public void loadData(String dataString) {
        try{    
            processor.processString(dataString);
        } catch (Exception e) {
            ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            PropertyManager manager  = applicationTemplate.manager;
            String          errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
            String          errMsg   = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
            String          errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
            dialog.show(errTitle, errMsg + errInput + e.getMessage());
            clear();
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
            // NOTE: completing this method was not a part of HW 1. You may have implemented file saving from the
            // confirmation dialog elsewhere in a different way.        
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {         
            writer.write(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } 
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
        //processor.toChartDataHelper(((AppUI) applicationTemplate.getUIComponent()).getChart()); //displays the average line
    }
}
