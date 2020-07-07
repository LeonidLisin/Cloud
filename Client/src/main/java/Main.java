import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private MainController mainController;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainWindow.fxml"));
        Parent root = loader.load();
        mainController = loader.getController();
        primaryStage.setTitle("Cloud");
        primaryStage.setScene(new Scene(root, 600, 575));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            mainController.quit();
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

