package handin2.Relation;

import handin2.Relation.Relation;
import handin2.Util;
import handin2.Way.Way;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

public class RelationForest extends Relation {

    public RelationForest(List<Way> waysOuter, int waysOuterCoordsSize, List<Way> waysInner, int waysInnerCoordsSize) {
        super(waysOuter, waysOuterCoordsSize, waysInner, waysInnerCoordsSize);
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc, Util.currentForestColor);
    }
}
