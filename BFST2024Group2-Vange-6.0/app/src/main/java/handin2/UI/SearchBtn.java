package handin2.UI;

import handin2.Model;
import handin2.Node;
import handin2.View;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;


public class SearchBtn extends VBox {
    Model model;
    ImageView imageView;
    MainPane mainPane;
    View view;
    boolean isDijktra = false;

    public SearchBtn(MainPane mainPane, Model model, View view) {
        this.view = view;
        this.model = model;
        this.mainPane = mainPane;
        initializeSearchbtn();
    }

    public void initializeSearchbtn() {
        Button btn = new Button();
        Image searchImg = new Image("/Icons/search.png");

        imageView = new ImageView(searchImg);
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        btn.setGraphic(imageView);

        btn.setOnAction(event -> {
            if(!mainPane.searchBox.getValue().isEmpty() && !mainPane.searchBox2.getValue().isEmpty()){
                try {
                    Node startNode = mainPane.searchBox.getSelectedNode();
                    Node endNode = mainPane.searchBox2.getSelectedNode();

                    if(startNode == endNode){
                        mainPane.sendMessageBox("Same address selected", true);
                        return;
                    }

                    if(startNode == null || endNode == null) throw new RuntimeException();
                    mainPane.toggleProgressBar();

                    List<Double> shortestPath = model.aStarSP.findPath(startNode, endNode, mainPane.getRadioBtnValue(), isDijktra);

                    if(shortestPath == null) throw new RuntimeException();

                    view.shortestPath = shortestPath;

                    double newMinLon = Math.min(startNode.lon, endNode.lon) / 0.56;
                    double newMaxLon = Math.max(startNode.lon, endNode.lon) / 0.56;
                    double newMinLat = Math.abs(Math.max(startNode.lat, endNode.lat));
                    double newMaxLat = Math.abs(Math.min(startNode.lat, endNode.lat));

                    view.changeZoomToPath(newMinLon, newMaxLon, newMinLat, newMaxLat);
                    view.redraw();
                    mainPane.toggleProgressBar();
                    mainPane.setTextFields();
                } catch (RuntimeException err) {
                    mainPane.sendMessageBox("Could not find valid path to address", true);
                    mainPane.toggleProgressBar();
                }
            } else if(!mainPane.searchBox.getValue().isEmpty()) {
                try {
                    Node node = view.searchBox.getSelectedNode();

                    if(node == null) throw new RuntimeException();

                    view.shortestPath = new ArrayList<>();
                    view.changeZoomToPoint(node.lon / 0.56, Math.abs(node.lat));

                    view.redraw();
                } catch (RuntimeException err) {
                    mainPane.sendMessageBox("Something went wrong", true);
                }
            } else {
                mainPane.sendMessageBox("Remember to add an address", true);
            }
        });

        this.getChildren().add(btn);
    }

    public void setDijkstra() {
        isDijktra = !isDijktra;
    }
}
