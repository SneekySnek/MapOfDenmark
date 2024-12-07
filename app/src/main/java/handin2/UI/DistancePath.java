package handin2.UI;

import handin2.Util;
import handin2.View;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class DistancePath extends HBox {
    View view;
    ImageView imageView;
    boolean isToggle = false;
    MainPane mainPane;

    ToggleButton btn;
    Image on = new Image("/Icons/on.png");
    Image off = new Image("/Icons/off.png");

    public DistancePath(View view, MainPane mainPane) {
        this.mainPane = mainPane;
        this.view = view;
        initializeChangeColour();
    }
    public void initializeChangeColour() {
        on = new Image("/Icons/on.png");
        off = new Image("/Icons/off.png");

        imageView = new ImageView(off);
        btn = new ToggleButton();
        btn.setId("isToggleBtn");
        imageView.setFitWidth(60);
        imageView.setFitHeight(30);
        btn.setGraphic(imageView);
        Text text = new Text("Place markers");
        text.setId("textForIsToggleBtn");


        btn.setOnAction(e -> {
            setClose();
            view.toggleMarkerMode();
            if (isToggle) {
                mainPane.sendMessageBox("Right click on the map to place markers!", false);
            }

        });
        this.setSpacing(10);
        this.setAlignment(javafx.geometry.Pos.CENTER_LEFT);


        this.getChildren().addAll(text, btn);

    }

    public void setClose() {
        isToggle = !isToggle;
        view.removeMarkers();
        imageView = isToggle ? new ImageView(on) : new ImageView(off);
        imageView.setFitWidth(60);
        imageView.setFitHeight(30);
        btn.setGraphic(imageView);
        mainPane.togglePathDistanceBox();
    }
}