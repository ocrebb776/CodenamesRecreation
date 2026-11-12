package uk.olivercrebbin.codenameshost;

import javafx.application.Application;
import uk.olivercrebbin.codenameshost.Server.ServerHandler;

import java.io.IOException;

public class Launcher {
    public static void main(String[] args) throws IOException {
        Application.launch(ApplicationManager.class, args);

    }
}
