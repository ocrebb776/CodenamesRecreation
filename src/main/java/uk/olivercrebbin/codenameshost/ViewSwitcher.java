package uk.olivercrebbin.codenameshost;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ViewSwitcher {
    static ApplicationManager manager;
    static Stage stage;

    public static void switchTo(View v) throws IOException {
        // Use the class loader to get the FXML resource
        URL resource = ViewSwitcher.class.getResource(v.getFxmlPage());
        if (resource == null) {
            throw new IllegalStateException("FXML file not found: " + v.getFxmlPage());
        }
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle(v.getTitle());
        stage.setScene(scene);
        stage.show();
    }

    public static Object switchTo(View v, Stage stage) throws IOException {
        String fileName = v.getFxmlPage();
        // Use the class loader to get the FXML resource
        URL resc = ViewSwitcher.class.getResource(fileName);
        if (resc == null) {
            throw new IllegalStateException("FXML file not found: " + fileName);
        }
        FXMLLoader loader = new FXMLLoader(resc);

        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle(v.getTitle());
        stage.setScene(scene);
        stage.show();
        return loader.getController();
    }
}