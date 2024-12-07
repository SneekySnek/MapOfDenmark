package handin2.UI;

import handin2.View;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class ToggleDijkstra extends HBox {
    View view;
    ImageView imageView;
    boolean isToggle = false;
    MainPane mainPane;

    ToggleButton btn;
    Image on = new Image("/Icons/on.png");
    Image off = new Image("/Icons/off.png");

    public ToggleDijkstra(View view, MainPane mainPane) {
        this.mainPane = mainPane;
        this.view = view;
        initializeToggleDijkstra();
    }
    public void initializeToggleDijkstra() {
        on = new Image("/Icons/on.png");
        off = new Image("/Icons/off.png");

        imageView = new ImageView(off);
        btn = new ToggleButton();
        btn.setId("isToggleBtn");
        imageView.setFitWidth(60);
        imageView.setFitHeight(30);
        btn.setGraphic(imageView);
        Text text = new Text("Toggle Dijkstra");
        text.setId("textForIsToggleBtn");


        btn.setOnAction(e -> {

            setClose();
            if (isToggle) {
                mainPane.sendMessageBox("You have turned on dijstra. Make a search!", false);
            }

        });
        this.setSpacing(10);
        this.setAlignment(javafx.geometry.Pos.CENTER_LEFT);


        this.getChildren().addAll(text, btn);

    }

    public void setClose() {
        isToggle = !isToggle;
        mainPane.toggledijkstra();
        imageView = isToggle ? new ImageView(on) : new ImageView(off);
        imageView.setFitWidth(60);
        imageView.setFitHeight(30);
        btn.setGraphic(imageView);
        //todo
    }
}