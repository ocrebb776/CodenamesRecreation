package uk.olivercrebbin.codenameshost;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uk.olivercrebbin.codenameshost.Server.ServerHandler;

import java.io.IOException;
import java.net.URL;

public class ApplicationManager extends Application {
    @Override
    public void start(Stage stage) throws IOException {
  ViewSwitcher.manager = this;
          ViewSwitcher.stage = stage;
          ViewSwitcher.switchTo(View.Launcher);
    }
    public static URL getResource(String filePath){
        return ApplicationManager.class.getResource(filePath);
    }

    @Override
    public void stop() throws Exception {
        ServerHandler.closeServer();
    }
}
