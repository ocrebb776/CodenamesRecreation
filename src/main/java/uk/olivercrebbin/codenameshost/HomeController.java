package uk.olivercrebbin.codenameshost;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import uk.olivercrebbin.codenameshost.Game.Card;
import uk.olivercrebbin.codenameshost.Game.GameState;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML
    MenuBar menuBar;
@FXML
    public Label currPlayerLabel;
    @FXML
    VBox L00;
    @FXML
    VBox L10;
    @FXML
    VBox L20;
    @FXML
    VBox L30;
    @FXML
    VBox L40;
    @FXML
    VBox L01;
    @FXML
    VBox L11;
    @FXML
    VBox L21;
    @FXML
    VBox L31;
    @FXML
    VBox L41;
    @FXML
    VBox L02;
    @FXML
    VBox L12;
    @FXML
    VBox L22;
    @FXML
    VBox L32;
    @FXML
    VBox L42;
    @FXML
    VBox L03;
    @FXML
    VBox L13;
    @FXML
    VBox L23;
    @FXML
    VBox L33;
    @FXML
    VBox L43;
    @FXML
    VBox L04;
    @FXML
    VBox L14;
    @FXML
    VBox L24;
    @FXML
    VBox L34;
    @FXML
    VBox L44;
    @FXML
    public Label joinCode;

    @FXML
    protected void toggleSpymasterView(){
        if(!gameState.isSpymasterSet()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are You sure you want to Toggle Spymaster");
            if (alert.showAndWait().get().getButtonData() == ButtonBar.ButtonData.OK_DONE)
                gameState.toggleSpymaster();
        }else{
            gameState.toggleSpymaster();

        }
    }

    GameState gameState;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        controller = this;
        joinCode.setText("");
       Card[][] card = new Card[5][5];
        card[0][0] = new Card(L00);
        card[1][0] = new Card(L10);
        card[2][0] = new Card(L20);
        card[3][0] = new Card(L30);
        card[4][0] = new Card(L40);

        card[0][1] = new Card(L01);
        card[1][1] = new Card(L11);
        card[2][1] = new Card(L21);
        card[3][1] = new Card(L31);
        card[4][1] = new Card(L41);

        card[0][2] = new Card(L02);
        card[1][2] = new Card(L12);
        card[2][2] = new Card(L22);
        card[3][2] = new Card(L32);
        card[4][2] = new Card(L42);

        card[0][3] = new Card(L03);
        card[1][3] = new Card(L13);
        card[2][3] = new Card(L23);
        card[3][3] = new Card(L33);
        card[4][3] = new Card(L43);

        card[0][4] = new Card(L04);
        card[1][4] = new Card(L14);
        card[2][4] = new Card(L24);
        card[3][4] = new Card(L34);
        card[4][4] = new Card(L44);

        for(int x = 0;x< card.length;x++) for(int y = 0;y< card[x].length;y++){
            int finalY = y;
            int finalX = x;
            card[x][y].getVBox().setOnMouseClicked(Event->{
                mangeCardClick(finalX, finalY);
            });
            card[x][y].setText("");
            card[x][y].getVBox().setEffect(new DropShadow(25,new Color(0,0,0,0.7)));
        }


        gameState = new GameState(card);
        gstate = gameState;
        gameState.getCurrentPlayer().addDislay(currPlayerLabel);
        menuBar.getMenus().clear();
        menuBar.getMenus().addAll(GUI_ITEMS.MENU_BAR.getMenuBar(ViewSwitcher.stage,gameState).getMenus());
    }

    private void mangeCardClick(int x,int y){
        gameState.handleClick(x,y);
        if(gameState.isGameOver()){
            GUI_ITEMS.winnerScreen(gameState.getEndGameInfo());
        }
    }

    @FXML
    protected void nextTeam(){
        gameState.nextTeam();
    }

    public static GameState gstate;
    public static HomeController controller;

}
