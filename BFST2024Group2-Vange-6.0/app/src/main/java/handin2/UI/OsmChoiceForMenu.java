package handin2.UI;

import handin2.Controller;
import handin2.Interfaces.WithOsmChoice;
import handin2.Model;
import handin2.View;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class OsmChoiceForMenu extends VBox implements WithOsmChoice {

    private String choice = "";
    Stage primaryStage;
    public OsmChoiceForMenu(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeChoice();
    }

    public void initializeChoice() {


        String directoryPath = "app/data/";
        choice = directoryPath + "Bornholm.osm.obj.zip";

        SelectFromData choiceBox = new SelectFromData(this);
        choiceBox.setId("osmChoice");

        SelectFromFiles choiceBtn = new SelectFromFiles(this, primaryStage);


        Button btn = new Button();
        btn.setId("startBtn");
        btn.setText("Start");
        Text orText = new Text("OR");
        orText.setFont(new Font(16));
        orText.setId("orText");



        VBox progress = new VBox(20);
        progress.setManaged(false);
        progress.setVisible(false);
        VBox mainLoading = new VBox();
        btn.setOnAction(event -> {
            if(!choice.isEmpty()) {
                progress.setManaged(true);
                progress.setVisible(true);
                mainLoading.setManaged(false);
                mainLoading.setVisible(false);
                // Create the OSM loading task
                OsmLoading osmLoading = new OsmLoading(choice);
                ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(100);
                progressBar.setPrefHeight(20);
                progressBar.setId("progressBar");
                // Bind the progress bar's progress property to the task's progress property
                progressBar.progressProperty().bind(osmLoading.progressProperty());

                Text text = new Text("Loading " + choice.replaceAll(".*/", "") + "...");
                text.setId("progressText");
                progress.getChildren().addAll(text, progressBar);


                // Start the task in a new thread
                new Thread(osmLoading).start();

                osmLoading.setOnSucceeded(e -> {
                    // When the task is done, create the view and controller with the loaded model
                    Model model = osmLoading.getValue();
                    var view = new View(model, primaryStage);
                    new Controller(model, view);
                });
            }
        });


        mainLoading.getChildren().addAll(choiceBox, orText, choiceBtn, btn);
        this.getChildren().addAll(mainLoading, progress);



    }
    @Override
    public void setChoice(String choice) {
        this.choice = choice;
    }

    @Override
    public String getChoice() {
        return choice;
    }
}