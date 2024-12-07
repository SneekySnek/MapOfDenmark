package handin2.UI;

import handin2.Interfaces.WithOsmChoice;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class SelectFromFiles extends Button {
    WithOsmChoice toggler;
    Stage primaryStage;
    public SelectFromFiles(WithOsmChoice toggler, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.toggler = toggler;
        initializeSelectFromFiles();
    }

    public void initializeSelectFromFiles() {

        // Create a FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        // Set filter for the osm files
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OSM Files", "*.osm", "ZIP Files", "*.zip", "OSM OBJ Files", "*.osm.obj"));


        this.setText("Choose from file");

        // Handle the choiceBtn click event
        this.setOnAction(event -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                toggler.setChoice(file.getAbsolutePath());
                // Set the text of the choiceBtn to the name to the last part of the path
                this.setText(toggler.getChoice().replaceAll(".*/", ""));
            }
        });
    }
}