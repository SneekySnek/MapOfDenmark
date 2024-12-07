package handin2.UI;

import handin2.Controller;
import handin2.Interfaces.WithOsmChoice;
import handin2.Model;
import handin2.View;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class OsmChoice extends Application implements WithOsmChoice {
    String choice = "";

    String filename = ("app/data/");
    @Override
    public void start(Stage primaryStage) throws XMLStreamException, IOException, ClassNotFoundException, Exception {


        String directoryPath = "app/data/";
        choice = directoryPath + "Bornholm.osm.obj.zip";

        SelectFromData choiceBox = new SelectFromData(this);
        choiceBox.setPrefWidth(250);
        choiceBox.setId("osmChoice");

        SelectFromFiles choiceBtn = new SelectFromFiles(this, primaryStage);

        Text heading = new Text("Map of Denmark");
        heading.setId("heading");
        Text subHeading = new Text("ITU project: group 2");
        subHeading.setId("subHeading");

        Button btn = new Button();
        btn.setId("startBtn");
        btn.setText("Start");
        Text orText = new Text("OR");
        orText.setFont(new Font(26));
        orText.setId("orText");


        VBox root = new VBox(20);
        VBox root2 = new VBox();
        VBox progress = new VBox(20);

        btn.setOnAction(event -> {
            if(!choice.isEmpty()) {
                root2.setManaged(false);
                root2.setVisible(false);
                // Create the OSM loading task
                OsmLoading osmLoading = new OsmLoading(choice);
                ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(250);
                progressBar.setPrefHeight(30);
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

        VBox header = new VBox();
        header.setId("header");
        header.getChildren().addAll(heading, subHeading);
        header.setAlignment(Pos.CENTER);
        root2.getChildren().addAll(header, choiceBox, orText, choiceBtn, btn);
        root.setAlignment(Pos.CENTER);
        root2.setAlignment(Pos.CENTER);
        progress.setAlignment(Pos.CENTER);
        root.getChildren().addAll(root2, progress);
        root.setId("osmChoiceRoot");

        Scene scene = new Scene(root, 640, 480);
        String path = this.getClass().getResource("/main.css").toExternalForm();
        scene.getStylesheets().add(path);
        primaryStage.setTitle("Choose osm files");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
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