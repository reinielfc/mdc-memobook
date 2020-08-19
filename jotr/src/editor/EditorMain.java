package editor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

public class EditorMain extends Application {

    private static Window primaryWindow;
    private static EditorController controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("editorUI.fxml"));
        controller = new EditorController(new EditorModel());
        loader.setControllerFactory(t -> controller);
        primaryStage.setScene(new Scene(loader.load()));

        primaryStage.setTitle("Memobook"); //TODO: Make it so filename shows in window title
        Image appIcon = new Image(getClass().getResourceAsStream("resources/icon.png"));
        primaryStage.getIcons().add(appIcon);

        primaryWindow = primaryStage;

        primaryStage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            controller.onExit();
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Window getPrimaryWindow() {
        return primaryWindow;
    }
}
