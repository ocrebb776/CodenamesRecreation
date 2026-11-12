package uk.olivercrebbin.codenameshost.Game;

import javafx.application.Platform;
import javafx.scene.control.Label;
import uk.olivercrebbin.codenameshost.Server.ServerHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CurrentPlayer {

    public enum Player{
        RedTeam("Red Team",CardRoles.RED_TEAM),
        BlueTeam("Blue Team",CardRoles.BLUE_TEAM);
        private  String name;
        private CardRoles ideal;
        Player(String name ,CardRoles idealRole){
            this.name = name;
            ideal= idealRole;
        }

        public String getName() {
            return name;
        }
        public CardRoles getPrefRole(){
            return ideal;
        }
    }
    private List<Label> displayLabel;
    private Player current;
    public CurrentPlayer(){
        current = Player.BlueTeam;
        displayLabel = new ArrayList<>();
    }

    public Player getCurrent() {
        return current;
    }
    public void addDislay(Label t){
       if(!displayLabel.contains(t)) displayLabel.add(t);
       updateLabel(t);
    }

    public void setCurrent(Player current) {
        this.current = current;
        updateLabels();
        ServerHandler.triggerNewTeam();
    }
    private void updateLabels(){
        displayLabel.stream().forEach(x->updateLabel(x));
    }
    private void updateLabel(Label label){
        if (label != null) {
            Platform.runLater(() -> label.setText(current.getName()));
        }    }
    public Player getOtherPlayer(){
        return Arrays.stream(Player.values()).filter(x->x!=current).findFirst().orElse(null);
    }
    public void setOtherPlayer(){
        setCurrent(getOtherPlayer());
    }
    public String getCurrentName(){
        return getCurrent().getName();
    }

}
