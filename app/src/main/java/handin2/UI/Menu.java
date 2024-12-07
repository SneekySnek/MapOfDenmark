package handin2.UI;

import handin2.View;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Menu extends VBox {

    ImageView imageView;
    boolean showPanel = false;
    MainPane mainPane;
    Stage primaryStage;
    Button btn;
    Image close;
    Image menu;
    View view;
    DistancePath pathSetter;

    public Menu(MainPane mainPane, Stage primaryStage, View view) {
        btn = new Button();
        close = new Image("/Icons/close.png");
        menu = new Image("/Icons/menu.png");
        this.primaryStage = primaryStage;
        this.mainPane = mainPane;
        this.view = view;
        initializeDirection();
    }

    public void initializeDirection() {


        imageView = new ImageView(new Image("/Icons/menu.png"));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        btn.setGraphic(imageView);

        //making a duplicate button soo we can make the design possible with the right alignment
        Button duplicateBtn = new Button();
        Button closeBtn = new Button();
        ImageView duplicateImageView = new ImageView(menu);

        duplicateBtn.setGraphic(duplicateImageView);
        duplicateImageView.setFitWidth(20);
        duplicateImageView.setFitHeight(20);

        btn.setOnAction(event -> {
            mainPane.toggleMenu();

            showPanel = !showPanel;
            imageView = showPanel ? new ImageView(close) : new ImageView(menu);
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            btn.setGraphic(imageView);

        });

        duplicateBtn.setOnAction(btn.getOnAction());

        btn.setId("btnForMenu");

        closeBtn.setOnAction(btn.getOnAction());
        closeBtn.setText("Close");
        closeBtn.setId("closeBtn");

        ChangeColourBtn changeColourBTN = new ChangeColourBtn(view);
        changeColourBTN.setId("changeColourBtn");
        pathSetter = new DistancePath(view, mainPane);
        pathSetter.setId("changeColourBtn");

        ToggleDijkstra toggleDijkstra = new ToggleDijkstra(view, mainPane);
        toggleDijkstra.setId("changeColourBtn");
        this.getChildren().add(duplicateBtn);
        Rectangle rect = new Rectangle(200, 5);
        Rectangle rect2 = new Rectangle(200, 5);
        Rectangle rect3 = new Rectangle(200, 5);

        Direction direction = new Direction(mainPane, true);
        direction.setId("Directions");

        OsmChoiceForMenu osmChoiceForMenu = new OsmChoiceForMenu(primaryStage);
        osmChoiceForMenu.setPadding(new javafx.geometry.Insets(20, 0, 0, 0));
        VBox vbox = new VBox();
        vbox.getChildren().addAll(btn, changeColourBTN, rect, pathSetter, rect2, toggleDijkstra, rect3, direction, osmChoiceForMenu, closeBtn);
        osmChoiceForMenu.setAlignment(Pos.BOTTOM_LEFT);
        mainPane.menu.getChildren().add(vbox);


    }

    public void setClose() {
        showPanel = false;
        imageView = new ImageView(menu);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        btn.setGraphic(imageView);
    }

    public void closePathSetter() {
        pathSetter.setClose();
    }
}