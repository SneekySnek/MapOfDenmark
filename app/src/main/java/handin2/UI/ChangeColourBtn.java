package handin2.UI;

import handin2.Util;
import handin2.View;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class ChangeColourBtn extends HBox {
    View view;
    ImageView imageView;
    boolean isToggle = false;

    public ChangeColourBtn(View view) {
        this.view = view;
        initializeChangeColour();
    }
    public void initializeChangeColour() {
        Image on = new Image("/Icons/on.png");
        Image off = new Image("/Icons/off.png");

        imageView = new ImageView(off);
        ToggleButton btn = new ToggleButton();
        btn.setId("isToggleBtn");
        imageView.setFitWidth(60);
        imageView.setFitHeight(30);
        btn.setGraphic(imageView);
        Text text = new Text("Colorblind mode");
        text.setId("textForIsToggleBtn");


        btn.setOnAction(e -> {
            isToggle = !isToggle;
            imageView = isToggle ? new ImageView(on) : new ImageView(off);
            imageView.setFitWidth(60);
            imageView.setFitHeight(30);
            btn.setGraphic(imageView);
            Util.toggleColorblindMode(isToggle, view);
        });
        this.setSpacing(10);
        this.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        this.getChildren().addAll(text, btn);
    }

}