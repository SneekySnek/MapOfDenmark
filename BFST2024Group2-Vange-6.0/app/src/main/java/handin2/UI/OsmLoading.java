package handin2.UI;

import handin2.Model;
import javafx.concurrent.Task;

public class OsmLoading extends Task<Model> {
    private String osmFilePath;

    public OsmLoading(String osmFilePath) {
        this.osmFilePath = osmFilePath;
    }

    @Override
    public Model call() throws Exception {
        OsmLoading taskInstance = this;
        return Model.load(osmFilePath, taskInstance::updateProgress);
    }

    @FunctionalInterface
    public interface ProgressHandler {
        void updateProgress(double workDone, double max);
    }
}
