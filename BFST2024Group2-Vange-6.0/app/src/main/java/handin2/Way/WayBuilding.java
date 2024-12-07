package handin2.Way;

import handin2.Node;
import handin2.Util;
import handin2.Way.Way;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

import java.util.ArrayList;

public class WayBuilding extends Way {
    public WayBuilding(ArrayList<Node> way) {
        super(way);
    }

    public void draw(GraphicsContext gc, Affine trans) {
        outlineArea(gc, trans);
        gc.setStroke(Util.currentBuildingStrokeColor);
        gc.stroke();
        gc.setFill(Util.currentBuildingColor);
        gc.fill();

        gc.setStroke(Util.currentBaseColor);
    }
}
