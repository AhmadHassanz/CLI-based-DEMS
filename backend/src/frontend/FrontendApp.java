package frontend;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FrontendApp extends Application {
    private static final Path FRONTEND_DIR = Paths.get("..", "frontend").toAbsolutePath().normalize();
    private static Stage mainStage;

    @Override
    public void start(Stage stage) throws IOException {
        mainStage = stage;
        stage.setTitle("SEQE - Secure Evidence Query Engine");
        showLogin();
        stage.show();
    }

    public static void showLogin() throws IOException {
        showScreen("Login.fxml", 1280, 760);
    }

    public static void showInvestigatorDashboard() throws IOException {
        showScreen("InvestigatorDashboard.fxml", 1440, 900);
    }

    public static void showAnalystDashboard() throws IOException {
        showScreen("AnalystDashboard.fxml", 1440, 900);
    }

    public static void showAdminDashboard() throws IOException {
        showScreen("AdminDashboard.fxml", 1440, 900);
    }

    private static void showScreen(String fxmlName, int width, int height) throws IOException {
        boolean wasMaximized = mainStage.isMaximized();
        boolean wasFullScreen = mainStage.isFullScreen();
        double currentWidth = mainStage.getWidth() > 0 ? mainStage.getWidth() : width;
        double currentHeight = mainStage.getHeight() > 0 ? mainStage.getHeight() : height;

        FXMLLoader loader = new FXMLLoader(FRONTEND_DIR.resolve("fxml").resolve(fxmlName).toUri().toURL());
        Parent root = loader.load();
        Scene scene = new Scene(root, currentWidth, currentHeight);
        scene.getStylesheets().add(FRONTEND_DIR.resolve("styles").resolve("seqe.css").toUri().toString());
        mainStage.setScene(scene);

        Platform.runLater(() -> {
            root.applyCss();
            root.layout();
            mainStage.setFullScreen(wasFullScreen);
            mainStage.setMaximized(wasMaximized);
            if (!wasMaximized && !wasFullScreen) {
                mainStage.centerOnScreen();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
