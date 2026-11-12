package uk.olivercrebbin.codenameshost;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import uk.olivercrebbin.codenameshost.Game.GameState;
import uk.olivercrebbin.codenameshost.Game.WordSet;


public class NewGameFromCSVController {
@FXML
TextArea textArea;
    @FXML
public void startGame(Event t){
        HomeController.gstate.newGame(WordSet.getFromCSV(textArea.getText()));
        Stage stage = (Stage) textArea.getScene().getWindow();
        stage.close();
    }
}
