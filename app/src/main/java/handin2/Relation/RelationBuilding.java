package handin2.Relation;

import handin2.Relation.Relation;
import handin2.Util;
import handin2.Way.Way;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

public class RelationBuilding extends Relation {

    public RelationBuilding(List<Way> waysOuter, int waysOuterCoordsSize, List<Way> waysInner, int waysInnerCoordsSize) {
        super(waysOuter, waysOuterCoordsSize, waysInner, waysInnerCoordsSize);
    }
    @Override
    public void draw(GraphicsContext gc) {
        super.drawOuterWithStroke(gc, Util.currentBuildingStrokeColor, Util.currentBuildingColor);
        super.draw(coordsInner, gc, Util.currentLandColor);
    }
}
