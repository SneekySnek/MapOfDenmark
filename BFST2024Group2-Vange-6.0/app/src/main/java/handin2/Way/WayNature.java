package handin2.Way;

import handin2.Node;
import handin2.Util;
import handin2.Way.Way;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

import java.util.ArrayList;

public class WayNature extends Way {
    public WayNature(ArrayList<Node> way) {
        super(way);
    }

    public void draw(GraphicsContext gc, Affine trans) {
        super.drawFill(gc, trans, Util.currentNatureColor);
    }
}
