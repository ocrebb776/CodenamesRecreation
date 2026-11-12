package uk.olivercrebbin.codenameshost;

import io.nayuki.qrcodegen.QrCode;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uk.olivercrebbin.codenameshost.Game.EndGameInfo;
import uk.olivercrebbin.codenameshost.Game.GameState;
import uk.olivercrebbin.codenameshost.Server.ServerHandler;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class GUI_ITEMS {
    public class MENU_BAR{
        public static MenuItem serverStatusMenu = new MenuItem();
        public static MenuBar getMenuBar(Stage stage, GameState gameState){
            MenuBar menuBar = new MenuBar();

            Menu server = new Menu("Server");
            MenuItem startServerMenu = new MenuItem("Start Server");
            startServerMenu.setOnAction(x-> startServer(gameState));
            MenuItem closeServer = new MenuItem("Stop Server");
            closeServer.setOnAction(x->closeServer());
            MenuItem serverInfo = new MenuItem("Get Server Info");
            serverInfo.setOnAction(x->serverInfoPopup(stage));
            server.getItems().addAll(serverStatusMenu,startServerMenu,closeServer,serverInfo);


            serverStatusMenu.setText("Server Not Active");
            serverStatusMenu.setDisable(true);


            Menu gameManager = new Menu("manage game");
            MenuItem newGame = new MenuItem("newGame");
            newGame.setOnAction((event)->{
                gameState.newGame();
            });
            MenuItem newGameCustom = new MenuItem("new Game with custom words");
            newGameCustom.setOnAction((event)->{
                Stage dialouge = new Stage();
                dialouge.initModality(Modality.APPLICATION_MODAL);
                dialouge.initOwner(stage);
                try {
                    ViewSwitcher.switchTo(View.CustomWordsPage, dialouge);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            });
            gameManager.getItems().addAll(newGame,newGameCustom);

            MenuItem help = new MenuItem("How To play the game");
            help.setOnAction(Event->{
                try {
                    showGeneralHelp(stage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            Menu helpMenu = new Menu("Help");
            helpMenu.getItems().addAll(help);
            menuBar.getMenus().addAll(server,gameManager,helpMenu);
            return menuBar;
        }private  static void showGeneralHelp(Stage stage) throws IOException {
            Stage dialouge = new Stage();
            dialouge.initModality(Modality.APPLICATION_MODAL);
            dialouge.initOwner(stage);
            ViewSwitcher.switchTo(View.GeneralHelp,dialouge);
        }
        private static void startServer(GameState gameState){
            ServerHandler.State state =  ServerHandler.start(gameState);
            if(state == ServerHandler.State.SERVER_NO_START){
                Alert l = new Alert(Alert.AlertType.ERROR,"Error starting server \n message :\n"+ServerHandler.getMostRecentMessage(),new ButtonType("close"));
            l.show();
            }else {
                serverStatusMenu.setText("Server Active on: "+ServerHandler.getPort());
            }
        }
        private static void closeServer(){
            ServerHandler.closeServer();
            serverStatusMenu.setText("Server Not Active");
        }
        private static void serverInfoPopup(Stage stage) {
            if(ServerHandler.state == ServerHandler.State.RUNNING) {
                Stage dialouge = new Stage();
                dialouge.initModality(Modality.APPLICATION_MODAL);
                dialouge.initOwner(stage);
                serverInfoController cont = null;
                try {
                    cont = (serverInfoController) ViewSwitcher.switchTo(View.ServerInfo, dialouge);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                cont.address.setText(ServerHandler.getAdress());
                cont.joinLink.setText(ServerHandler.getJoinLink());
                cont.serverCode.setText(ServerHandler.getCode());
                serverInfoController finalCont1 = cont;
                cont.serverCode.setOnKeyTyped(x->{
                   ServerHandler.setCode( finalCont1.serverCode.getText());
                });
                serverInfoController finalCont = cont;
                cont.joinLink.setOnKeyTyped(x->{
                    updateQrcode(finalCont.joinLink.getText(), finalCont.QrCode);

                });
                updateQrcode(cont.joinLink.getText(),cont.QrCode);

            }else{
                new Alert(Alert.AlertType.INFORMATION,"Server isnt running").show();
            }
        }
        public static void updateQrcode(String text,HBox parent){
            try {
                BufferedImage imageContent = generateQrcode(text);
                Image image = SwingFXUtils.toFXImage(imageContent, null);
                ImageView view= new ImageView(image);
                view.setPreserveRatio(true);
                view.setFitWidth(300);

                view.setFitHeight(300);
                parent.getChildren().clear();
                parent.getChildren().add(view);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
    public static void winnerScreen(EndGameInfo info){
        Stage dialouge = new Stage();
        dialouge.initOwner(ViewSwitcher.stage);
        dialouge.initModality(Modality.APPLICATION_MODAL);
        try {
           WinnerPageController cont =  (WinnerPageController)ViewSwitcher.switchTo(View.WinnerPage,dialouge);
           cont.setWinnerInfo(info);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dialouge.show();
    }
    public static BufferedImage generateQrcode(String barcodeText) throws Exception {
        QrCode qrCode = QrCode.encodeText(barcodeText, QrCode.Ecc.MEDIUM);
        BufferedImage img = toImage(qrCode, 4, 1);
        return img;
    }
    public static BufferedImage toImage(QrCode qr, int scale, int border) {
        return toImage(qr, scale, border, 0xFFFFFF, 0x000000);
    }
    public static BufferedImage toImage(QrCode qr, int scale, int border, int lightColor, int darkColor) {
        Objects.requireNonNull(qr);
        if (scale <= 0 || border < 0) {
            throw new IllegalArgumentException("Value out of range");
        }
        if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale) {
            throw new IllegalArgumentException("Scale or border too large");
        }

        BufferedImage result = new BufferedImage(
                (qr.size + border * 2) * scale,
                (qr.size + border * 2) * scale,
                BufferedImage.TYPE_INT_RGB
        );
        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                boolean color = qr.getModule(x / scale - border, y / scale - border);
                result.setRGB(x, y, color ? darkColor : lightColor);
            }
        }
        return result;
    }
}
