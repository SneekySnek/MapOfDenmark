package handin2.Way;

import handin2.Node;
import handin2.Util;
import handin2.Way.Way;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

import java.util.ArrayList;

public class WayWater extends Way {
    public WayWater(ArrayList<Node> way) {
        super(way);
    }

    public void draw(GraphicsContext gc, Affine trans) {
        super.draw(gc, trans);
        gc.setFill(Util.currentWaterColor);
        gc.fill();
    }
}
