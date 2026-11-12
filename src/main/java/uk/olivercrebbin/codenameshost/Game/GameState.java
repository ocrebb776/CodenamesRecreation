package uk.olivercrebbin.codenameshost.Game;

import uk.olivercrebbin.codenameshost.GUI_ITEMS;
import uk.olivercrebbin.codenameshost.HomeController;
import uk.olivercrebbin.codenameshost.Server.ServerHandler;

import java.util.*;

public class GameState {
    private Card[][] cards;
    private boolean spymasterSet = false;
    private boolean gameOver = false;
    private CurrentPlayer currentPlayer;
    private EndGameInfo endGameInfo;
    public GameState(Card[][] cards) {
        this.cards = cards;
        Set<String> wordSet = WordSet.getRandom();
        currentPlayer = new CurrentPlayer();
        createRoles();
        fillWords(wordSet);
    }
    public void newGame(){
        Set<String> wordSet = WordSet.getRandom();
        newGame(wordSet);
    }

    public void newGame(Set<String> wordSet){
        gameOver = false;
        currentPlayer = new CurrentPlayer();
        createRoles();
        fillWords(wordSet);
        for(Card[] x : cards){
            for(Card y : x){
                if(y.isFlipped()){
                    y.flipKeepText("Whitecard");
                    y.setFlipped(false);
                }
            }
        }
        setSpyMaster(false);
        ServerHandler.trigger();
        currentPlayer.addDislay(HomeController.controller.currPlayerLabel);
    }

    public CurrentPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    private void createRoles(){
        List<CardRoles> stack = new ArrayList<>();
        for(int i=0;i<8;i++){
            stack.add(CardRoles.BLUE_TEAM);
            stack.add(CardRoles.RED_TEAM);
        }

            stack.add(getCurrentPlayer().getCurrent().getPrefRole());
        stack.add(CardRoles.Bomb);

        List<Card> cardList = new ArrayList<>();
        Arrays.stream(cards).forEach(cards1 -> cardList.addAll(Arrays.stream(cards1).toList()));
        Random rgen = new Random();
        Collections.shuffle(stack,rgen);
        Collections.shuffle(cardList);
            for(Card card:cardList){
                if(stack.size() >0){
                int stackPos = rgen.nextInt(0, stack.size());
                card.setRole(stack.get(stackPos));
                stack.remove(stackPos);
                }else card.setRole(CardRoles.Civilian);
            }


    }
    private void fillWords(Set<String> wordSet){
        List<String> words = new ArrayList<>(wordSet);
        Collections.shuffle(words);
        Collections.shuffle(words);
        Random rgen = new Random();
        for(int x = 0;x<cards.length;x++) for(int y=0;y<cards[0].length;y++){
            if(words.size() >0){
                int stackPos = rgen.nextInt(0, words.size());
                cards[x][y].setOriginalText(words.get(stackPos));

                words.remove(stackPos);
            }else cards[x][y].setText("--ERROR--");


        }

    }
    public void handleClick(int x,int y){
        if(isGameOver()) return;
        if(!cards[x][y].isFlipped()){
            ServerHandler.cardFlip(+x+","+y);
            cards[x][y].setFlipped(true);
            cards[x][y].flip(cards[x][y].getRole().getClassName());
            if(cards[x][y].getRole() == CardRoles.Bomb){
                endGame(currentPlayer.getOtherPlayer().getName(),"Assassin uncovered");
        }


        checkGameOver();

        }



    }
    public void setSpyMaster(boolean value){
spymasterSet = value;
        if(value){
          for(Card[] row : cards) for(Card card:row){
              if(!card.isFlipped())
                card.flipKeepText(card.getRole().getClassName());
          }
        }else {
            for(Card[] row : cards) for(Card card:row){

                if(!card.isFlipped())
                card.flipKeepText("Whitecard");
            }
        }
    }
    public void toggleSpymaster(){
        setSpyMaster(!spymasterSet);
    }
    public void flipAll(){
        for( int x=0;x< cards.length;x++) for(int y=0;y<cards[x].length;y++){
            handleClick(x,y);
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public EndGameInfo getEndGameInfo() {
        return endGameInfo;
    }
    public void endGame(String winner,String endGameReason){
        ServerHandler.triggerEndGame();
        gameOver = true;
        endGameInfo = new EndGameInfo(winner,endGameReason);
        GUI_ITEMS.winnerScreen(endGameInfo);
    }
    public void checkGameOver(){
        boolean blueWon = true;
        boolean redWon = true;
        for(Card[] row : cards) for(Card card:row){
            if(card.getRole() == CardRoles.BLUE_TEAM && !card.isFlipped()) blueWon = false;
            if(card.getRole() == CardRoles.RED_TEAM && !card.isFlipped()) redWon = false;
        }
        if(redWon){
            endGame(CurrentPlayer.Player.RedTeam.getName(),"Last Red Agent Found" );
        }if(blueWon){
            endGame(CurrentPlayer.Player.BlueTeam.getName(),"Last Blue Agent Found" );

        }
    }
    public void nextTeam(){
        currentPlayer.setOtherPlayer();
    }

    public Card[][] getCards() {
        return cards;
    }

    public boolean isSpymasterSet() {
        return spymasterSet;
    }
}
