package handin2.UI;

import handin2.Interfaces.WithOsmChoice;
import javafx.scene.control.ChoiceBox;

import java.io.File;

public class SelectFromData extends ChoiceBox<String>{
    WithOsmChoice toggler;
    public SelectFromData(WithOsmChoice toggler) {
        this.toggler = toggler;
        initializeSelectFromData();
    }

    public void initializeSelectFromData() {


        String directoryPath = "app/data/";

        // Create a File object for the directory and get a list of files
        File directory = new File(directoryPath);
        File[] fileList = directory.listFiles();

        for (File file : fileList) {
            this.getItems().add(file.getName());
        }

        // default value for choiceBox
        this.setValue("Bornholm.osm.obj.zip");
        toggler.getChoice();

        this.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> {
            this.setValue(newValue);
            toggler.setChoice(directoryPath + newValue);
        });
    }

}