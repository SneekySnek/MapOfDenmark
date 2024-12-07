package handin2.UI;


import handin2.Model;
import handin2.Util;
import handin2.View;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Button;

public class MainPane extends StackPane {

    Menu menuBtn;
    StackPane menu;
    VBox panel;
    Direction direction;
    View view;
    Model model;
    HBox menuHbox;
    Stage primaryStage;
    Canvas canvas;
    SearchBox searchBox;
    SearchBox searchBox2;
    Text messageText;
    StackPane messageBox;
    Text progressBar;
    VBox progressBarBox;
    RadioBtn radioBtn;
    SearchBtn searchbtn;
    PathDistanceBox pathDistanceBox;

    Text distanceInKm;
    Text timeSpent;
    Text intersectionsSearched;
    Text PassableEdgesSearched;
    Text EdgesUpdated;

    public MainPane(View view, Model model, Canvas canvas, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.canvas = canvas;
        this.view = view;
        this.model = model;

        initializeDirection();
    }

    public void initializeDirection() {
        this.getChildren().add(canvas);
        searchBox2 = view.searchBox2;
        searchBox = view.searchBox;


        VBox vbScaleBar = new VBox();
        pathDistanceBox = new PathDistanceBox(this, view);
        pathDistanceBox.setVisible(false);
        pathDistanceBox.setMouseTransparent(true);
        GeoScale geoScale = view.scaleBar;


        messageBox = new StackPane();
        messageBox.setPadding(new Insets(10));
        messageBox.setVisible(false);
        messageText = new Text("Something went wrong");
        messageBox.getChildren().add(messageText);
        messageBox.setId("messageBox");
        messageBox.setAlignment(Pos.BOTTOM_CENTER);


        vbScaleBar.getChildren().addAll(pathDistanceBox, geoScale);
        vbScaleBar.setAlignment(Pos.BOTTOM_CENTER);
        geoScale.setAlignment(Pos.BOTTOM_RIGHT);


        HBox hbox = new HBox();

        searchbtn = new SearchBtn(this, model, view);

        VBox alignPanel = new VBox();

        // Initially hide the panel
        HBox forSearch = new HBox();
        forSearch.setPadding(new Insets(10));
        forSearch.getChildren().addAll(searchBox);
        direction = new Direction(this, false);
        direction.setId("direction");
        panel = new VBox();
        // Initially hide the panel
        panel.setVisible(false);
        panel.setId("panel");

        Button closeBtn = new Button();
        closeBtn.setText("Close");
        closeBtn.setId("closeBtn");
        closeBtn.setOnAction(event -> {
            togglePanel();
            direction.toggleDirection();
        });
        closeBtn.setAlignment(Pos.BOTTOM_LEFT);
        panel.getChildren().addAll(searchBox2);

        menu = new StackPane();
        menu.setId("menu");
        menuBtn = new Menu(this, primaryStage, view);
        menuBtn.setId("menuBtn");
        menu.setVisible(false);

        VBox setSearchAndRadio = new VBox();
        radioBtn = new RadioBtn();

        progressBar = new Text("Searching Path...");
        progressBar.setId("isSearching");
        progressBarBox = new VBox();
        progressBarBox.getChildren().add(progressBar);
        progressBarBox.setVisible(false);


        VBox textFields = new VBox();
        textFields.setId("textFields");
        distanceInKm = new Text("Distance in km: ");
        timeSpent = new Text("Time spent: ");
        intersectionsSearched = new Text("Intersections searched: ");
        PassableEdgesSearched = new Text("Passable edges searched: ");
        EdgesUpdated = new Text("Edges updated: ");
        textFields.getChildren().addAll(distanceInKm, timeSpent, intersectionsSearched, PassableEdgesSearched, EdgesUpdated);
        textFields.setPadding(new Insets(10));



        setSearchAndRadio.getChildren().addAll(searchBox2, radioBtn, progressBarBox, textFields, closeBtn);
        setSearchAndRadio.setPadding(new Insets(10));
        setSearchAndRadio.visibleProperty().bind(panel.visibleProperty());
        alignPanel.getChildren().addAll(searchBox, setSearchAndRadio);

        distanceInKm.getStyleClass().add("text");
        timeSpent.getStyleClass().add("text");
        intersectionsSearched.getStyleClass().add("text");
        PassableEdgesSearched.getStyleClass().add("text");
        EdgesUpdated.getStyleClass().add("text");

        StackPane searchPanelStack = new StackPane();
        searchPanelStack.getChildren().addAll(panel, alignPanel);
        searchPanelStack.setId("searchPanelStack");
        hbox.getChildren().addAll(menuBtn, searchPanelStack, searchbtn, direction);
        hbox.setId("mainGui");
        menuHbox = new HBox();
        menuHbox.getChildren().add(menu);
        menuHbox.setMouseTransparent(true);


        searchBox.setId("searchBox");
        primaryStage.setTitle("Group 2 Map");

        this.setStyle("-fx-background-color: #ffffff;");




        this.setPickOnBounds(false);
        this.getChildren().addAll(hbox, menuHbox);

        vbScaleBar.setPickOnBounds(false);
        vbScaleBar.setPadding(new Insets(10));
        this.getChildren().add(vbScaleBar);
        VBox vbMessageBox = new VBox();

        messageBox.setPrefSize(200, 60);
        this.setAlignment(messageBox, Pos.BOTTOM_CENTER);
        this.getChildren().add(messageBox);

        this.requestFocus();
    }

    public void setClosedirection() {
        direction.setClose();
        menuBtn.setClose();
    }

    public void togglePanel() {
        menuHbox.setMouseTransparent(true);
        menu.setVisible(false);
        panel.setVisible(!panel.isVisible());
        menuBtn.setManaged(!panel.isVisible());
    }

    public void toggleMenu() {
        menuHbox.setMouseTransparent(menu.isVisible());
        menu.setVisible(!menu.isVisible());
        panel.setVisible(false);
        menuBtn.setManaged(true);
    }

    public void setTextPathDistanceBox(String text) {
        pathDistanceBox.text.setText(text);
        pathDistanceBox.setVisible(true);
        pathDistanceBox.setMouseTransparent(false);

    }

    public void togglePathDistanceBox() {
        pathDistanceBox.setVisible(!pathDistanceBox.isVisible());
        pathDistanceBox.setMouseTransparent(!pathDistanceBox.isMouseTransparent());
    }


    public void sendMessageBox(String message, Boolean isError) {
        if (isError) {
            messageBox.setStyle("-fx-background-color: #ff0000;");
        } else {
            messageBox.setStyle("-fx-background-color: #00ff00;");
        }
        messageText.setText(message);
        messageBox.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(event -> messageBox.setVisible(false));
        pause.play();
    }

    public void closePathToggle() {
        menuBtn.closePathSetter();
    }

    public String getRadioBtnValue() {
        return  radioBtn.getModeOfTransport();
    }

    public void toggledijkstra() {
        searchbtn.setDijkstra();
    }

    public void toggleProgressBar() {
        progressBarBox.setVisible(!progressBarBox.isVisible());
        System.out.println("Toggled progress bar" + progressBarBox.isVisible());
    }

    public void setTextFields() {
        double distance = Util.segmentLength(view.shortestPath);
        distance = Math.round(distance * 100.0) / 100.0;
        distanceInKm.setText("Distance in km: " + distance);
        timeSpent.setText("Time spent: " + model.aStarSP.timeSpent);
        intersectionsSearched.setText("Intersections searched: " + model.aStarSP.intersectionsPassed);
        PassableEdgesSearched.setText("Passable edges searched: " + model.aStarSP.searchedEdges);
        EdgesUpdated.setText("Edges updated: " + model.aStarSP.updatedBest);
    }
}