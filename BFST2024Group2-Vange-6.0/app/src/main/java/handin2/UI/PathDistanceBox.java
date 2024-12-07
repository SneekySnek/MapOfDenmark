package handin2.UI;

import handin2.View;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class PathDistanceBox extends VBox {
    public Text text;
    MainPane mainPane;
    View view;

    public PathDistanceBox(MainPane mainPane, View view) {
        this.view = view;
        this.mainPane = mainPane;
        initializePathDistanceBox();
    }

    private void initializePathDistanceBox() {
        this.setPadding(new Insets(10));
        this.setStyle("-fx-background-color: #FFFFFF; -fx-border-radius: 1000px; -fx-max-width: 200px;");
        this.setAlignment(Pos.BOTTOM_CENTER);

        Button removeBtn = new Button("Close");


        removeBtn.setOnAction(e -> {
            view.toggleMarkerMode();
            mainPane.closePathToggle();
            view.removeMarkers();
            text.setText("Distance: ");

        });

        Button back = new Button("Back");

        back.setOnAction(e -> {
            view.stepBack();
        });

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        hBox.getChildren().addAll(back, removeBtn);
        text = new Text("Distance: ");
        this.getChildren().addAll(text, hBox);
    }
}