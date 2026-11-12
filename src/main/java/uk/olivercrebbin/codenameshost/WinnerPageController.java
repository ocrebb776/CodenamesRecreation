package uk.olivercrebbin.codenameshost;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import uk.olivercrebbin.codenameshost.Game.EndGameInfo;

public class WinnerPageController {
    @FXML
    private Label winnerLabel;
    @FXML
    private Label winnerReasonLabel;
    private  String  winerReason;
    private  String  winner;
private EndGameInfo endGameInfo;
    public void setWinnerInfo(EndGameInfo endGameInfo) {
        this.endGameInfo = endGameInfo;
        winnerReasonLabel.setText(endGameInfo.winReason());
        winnerLabel.setText(endGameInfo.winnerTeam());
    }

}
