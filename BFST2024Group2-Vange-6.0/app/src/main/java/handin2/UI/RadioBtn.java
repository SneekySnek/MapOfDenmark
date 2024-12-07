package handin2.UI;

import handin2.View;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class RadioBtn extends HBox {
    String modeOfTransport = "vehicle";
    public RadioBtn() {
        initializeRadioBtn();
    }

    public void initializeRadioBtn() {



        this.setPadding(new Insets(40, 10, 10, 10));
        // Create a ToggleGroup
        ToggleGroup group = new ToggleGroup();

        // Create RadioButtons
        RadioButton rb1 = new RadioButton();
        rb1.setUserData("vehicle");
        Image carImg = new Image("/Icons/car.png");
        ImageView carView = new ImageView(carImg);
        carView.setFitWidth(20);
        carView.setFitHeight(20);
        rb1.setGraphic(carView);
        rb1.getStyleClass().remove("radio-button");
        rb1.getStyleClass().add("toggle-button");
        rb1.setToggleGroup(group);
        rb1.setSelected(true); // Set this radio button as selected by default

        RadioButton rb2 = new RadioButton();
        rb2.setUserData("pedestrian");
        rb2.setToggleGroup(group);
        Image walkImg = new Image("/Icons/walk.png");
        ImageView walkView = new ImageView(walkImg);
        walkView.setFitWidth(20);
        walkView.setFitHeight(20);
        rb2.setGraphic(walkView);
        rb2.getStyleClass().remove("radio-button");
        rb2.getStyleClass().add("toggle-button");

        RadioButton rb3 = new RadioButton();
        rb3.setUserData("bike");
        rb3.setToggleGroup(group);
        Image bikeImg = new Image("/Icons/bike.png");
        ImageView bikeView = new ImageView(bikeImg);
        bikeView.setFitWidth(20);
        bikeView.setFitHeight(20);
        rb3.setGraphic(bikeView);
        rb3.getStyleClass().remove("radio-button");
        rb3.getStyleClass().add("toggle-button");
// Event Handling for RadioButton Selection
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (group.getSelectedToggle() != null) {
                RadioButton selectedRb = (RadioButton) group.getSelectedToggle();
                modeOfTransport = (String) selectedRb.getUserData();
            }
        });
        // Add radio buttons to the VBox
        this.setSpacing(10);
        this.getChildren().addAll(rb1, rb2, rb3);
        rb1.setId("radio");
        rb2.setId("radio");
        rb3.setId("radio");
    }

    public String getModeOfTransport() {
        return modeOfTransport;
    }
}