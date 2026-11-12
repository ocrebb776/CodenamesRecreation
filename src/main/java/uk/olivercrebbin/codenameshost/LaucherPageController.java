package uk.olivercrebbin.codenameshost;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class LaucherPageController {


    @FXML
    protected void startGame() throws IOException {

        ViewSwitcher.switchTo(View.Home);
    }
}
