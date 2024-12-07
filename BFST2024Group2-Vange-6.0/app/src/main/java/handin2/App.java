/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package handin2;

import handin2.UI.OsmChoice;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        String currentWorkingDir = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentWorkingDir);
        OsmChoice osmChoice = new OsmChoice();
        osmChoice.start(primaryStage);
    }
}
