package handin2.UI;

import handin2.View;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class Direction extends VBox {

    ImageView imageView;
    boolean showPanel = false;
    MainPane mainPane;

    boolean isText = false;
    Button btn;
    Image close;
    Image dir;

    public Direction(MainPane mainPane, boolean isText) {
        btn = new Button();
        close = new Image("/Icons/close.png");
        dir = new Image("/Icons/dir.png");
        this.mainPane = mainPane;
        this. isText = isText;
        initializeDirection();
    }

    public void initializeDirection() {


        imageView = new ImageView(new Image("/Icons/dir.png"));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        btn.setGraphic(imageView);
        if(isText) {
            btn.setText("Direction");
        }

        btn.setOnAction(event -> {
            mainPane.togglePanel();

            showPanel = !showPanel;
            if(isText) {
                mainPane.setClosedirection();
            } else {
                imageView = showPanel ? new ImageView(close) : new ImageView(dir);
                imageView.setFitWidth(20);
                imageView.setFitHeight(20);
                btn.setGraphic(imageView);
            }
        });

        this.getChildren().add(btn);
    }

    public void toggleDirection() {
        showPanel = !showPanel;
        imageView = showPanel ? new ImageView(close) : new ImageView(dir);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        btn.setGraphic(imageView);
    }

    public void setClose() {
        showPanel = true;
        imageView = new ImageView(close);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        btn.setGraphic(imageView);
    }

}