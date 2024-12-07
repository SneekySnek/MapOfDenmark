package handin2.UI;

import handin2.Util;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.io.Serializable;


public class GeoScale extends VBox implements Serializable {
    double x1, y1, x2, y2;
    Text text;
    ImageView imageView;

    public GeoScale(double lat1, double lon1, double lat2, double lon2) {
        this.x1 = lon1;
        this.y1 = lat1;
        this.x2 = lon2;
        this.y2 = lat2;
        double geoLength = Util.haversine(y1, x1, y2, x2);
        text = new Text((x1 + x2) / 2, y1 - 10, String.format("%.2f km", geoLength ));
        Image scalebarImg = new Image("/Icons/scalebar.png");
        imageView = new ImageView(scalebarImg);
        imageView.setFitWidth(100);
        imageView.setFitHeight(10);
        this.getChildren().add(text);
        this.getChildren().add(imageView);
    }


    public void updateScaleBar(double lon1 , double lon2, double lat1, double lat2, double diagonalScreen) {
        double scale = diagonalScreen / 100;

        double geoLength = Util.haversine(lat1, lon1, lat2, lon2);
        double width = geoLength / scale;
        if(width > 5) {
            double WholeNumGeo = width % 0.5;
            text.setText(String.format("%.2f km", (width - WholeNumGeo)));
            imageView.setFitWidth(100* (1 + WholeNumGeo));
        }
        else if(width > 2) {
            double WholeNumGeo = width % 0.25;
            text.setText(String.format("%.2f km", (width - WholeNumGeo)));
            imageView.setFitWidth(100* (1 + WholeNumGeo));
        } else if (width > 1) {
            double WholeNumGeo = (width*1000) % 100;
            text.setText(String.format("%.2f m", (width * 1000 - WholeNumGeo)));
            imageView.setFitWidth(100 * (1 + WholeNumGeo/1000));
        }
        else if (width > 0.5) {
            double WholeNumGeo = (width*1000) % 50;
            text.setText(String.format("%.2f m", (width * 1000 - WholeNumGeo)));
            imageView.setFitWidth(100 * (1 + WholeNumGeo/1000));
        }
        else if (width > 0.1) {
            double WholeNumGeo = (width*1000) % 25;
            text.setText(String.format("%.2f m", (width * 1000 - WholeNumGeo)));
            imageView.setFitWidth(100 * (1 + WholeNumGeo/1000));
        } else if (width > 0.001){
            double WholeNumGeo = (width*1000) % 1;
            text.setText(String.format("%.2f m", (width * 1000 - WholeNumGeo)));
            imageView.setFitWidth(100 * (1 + WholeNumGeo/1000));
        } else {
            text.setText(String.format("%.2f cm", (width * 100000 )));
            imageView.setFitWidth(100);
        }

    }
}