package editor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

public class EditorMain extends Application {

    private static Window primaryWindow;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("editorUI.fxml"));
        loader.setControllerFactory(t -> new EditorController(new EditorModel()));
        //TODO: Make it so filename shows in window title
        primaryStage.setTitle("Memobook");
        Image appIcon = new Image(getClass().getResourceAsStream("resources/icon.png"));
        primaryStage.getIcons().add(appIcon);
        primaryStage.setScene(new Scene(loader.load()));
        primaryWindow = primaryStage;
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Window getPrimaryWindow() {
        return primaryWindow;
    }
}
