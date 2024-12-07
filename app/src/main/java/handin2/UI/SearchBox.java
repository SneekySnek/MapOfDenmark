package handin2.UI;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import handin2.Model;
import handin2.Node;
import handin2.View;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javax.xml.stream.XMLStreamException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class SearchBox extends VBox {
    private Model model;
    private View view;
    private ObservableList<String> items;
    private ComboBox<String> searchBox;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> futureTask = null; // For debounce mechanism
    private ChangeListener<String> textChangeListener;
    private Map<String, Node> searchResultsMap;
    private List<Node> searchResultsNodes;
    private Node selectedNode;
    String value;
    Image image;
    Image dotIcon;

    public SearchBox(View view, Model model, boolean isBlue)  {
        dotIcon = isBlue ? new Image("/Icons/dotBlue.png") : new Image("/Icons/dotRed.png");
        this.view = view;
        this.model = model;
        this.value = "";
        initializeSearchBox();
    }

    private void initializeSearchBox() {
        searchBox = new ComboBox<>();
        searchBox.getStyleClass().add("searchbox");
        searchBox.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/main.css")).toExternalForm());
        searchBox.setEditable(true);
        searchBox.setItems(items); // Initially, display all items

        searchBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return object; // Display the selected item in the text field
            }

            @Override
            public String fromString(String string) {
                return string; // Handle user's custom input without alteration
            }
        });


        textChangeListener = (observable, oldValue, newValue) ->  {
            if (futureTask != null && !futureTask.isDone()) {
                futureTask.cancel(true);
            }

            futureTask = executorService.schedule(() -> {

                searchResultsNodes = model.addressTST.search(newValue);

                searchResultsMap = searchResultsNodes.stream()
                        .collect(Collectors.toMap(node -> node.getAddress().getAddressToSTR(), node -> node));
                List<String> searchResults = new ArrayList<>(searchResultsMap.keySet());
                Platform.runLater(() -> {
                    searchBox.setItems(FXCollections.observableArrayList(searchResults.stream().limit(4).collect(Collectors.toList())));
                    value = searchBox.getEditor().getText();
                    searchBox.setValue(value);
                    searchBox.getEditor().setText(value);
                    searchBox.getEditor().positionCaret(value.length());
                    selectedNode = (searchResultsMap.get(value) == null) ? null : searchResultsMap.get(value);
                    if (!searchResults.isEmpty() && !searchBox.isShowing() && selectedNode == null && !value.equals("")) {
                        searchBox.show();
                    }

                });
            }, 300, TimeUnit.MILLISECONDS);
        };

        searchBox.getEditor().textProperty().addListener(textChangeListener);
        HBox forSearch = new HBox();
        Button remove = new Button();
        Image closeImage = new Image("/Icons/close.png");
        ImageView imageView = new ImageView(closeImage);
        imageView.setFitWidth(10);
        imageView.setFitHeight(10);
        remove.setGraphic(imageView);
        remove.setOnAction(e -> {
            setValue("");
            selectedNode = null;
            view.shortestPath = new ArrayList<>();
            searchBox.hide();
            view.redraw();
        });

        ImageView dotIconimageView = new ImageView(dotIcon);
        dotIconimageView.setFitWidth(15);
        dotIconimageView.setFitHeight(20);
        HBox forSearchandClose = new HBox();
        forSearchandClose.getChildren().addAll(searchBox, remove);
        forSearchandClose.setId("forSearchandClose");
        forSearch.getChildren().addAll(dotIconimageView, forSearchandClose);
        forSearch.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        this.getChildren().add(forSearch);

    }

    public void draw(GraphicsContext gc, double circleSize) {
        if(selectedNode == null) {
            //System.out.println("Not Working");
            return;
        }
        if(this.equals(view.searchBox2)) {
            image = new Image("/Icons/dotBlue.png");
        } else {
            image = new Image("/Icons/dotRed.png");
        }

        double zoomFactor = (view.maxlat - view.minlat) + (view.maxlon - view.minlon);
        zoomFactor *= 0.5;

        double scaledWidth = Math.max(0.025 * zoomFactor, 0.00004);
        double scaledHeight = Math.max(0.03 * zoomFactor, 0.00006);

        double centerImageX = selectedNode.lon - (scaledWidth / 2);
        double bottomImageY = selectedNode.lat - scaledHeight;

        gc.drawImage(image, centerImageX, bottomImageY, scaledWidth, scaledHeight);
    }

    public Node getSelectedNode() {
        return selectedNode;
    }

    public void printResults() {
        for (handin2.Node node : searchResultsNodes) {
            if (node != null && node.getAddress() != null) {
                System.out.println(node.getAddress().getAddressToSTR() + " is located at: " + node.lat + ", " + node.lon);
            }
        }
        System.out.println("We found " + searchResultsNodes.size() + " results for the address query: " + currentValue());
    }

    public String currentValue() {
        return searchBox.getEditor().getText();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {

        searchBox.getEditor().textProperty().removeListener(textChangeListener);
        this.value = value;
        searchBox.setValue(value);
        searchBox.getEditor().setText(value);
        searchBox.getEditor().textProperty().addListener(textChangeListener);
        view.redraw();
    }
}