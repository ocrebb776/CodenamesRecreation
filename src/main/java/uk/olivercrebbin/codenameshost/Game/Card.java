package uk.olivercrebbin.codenameshost.Game;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class Card {
    private VBox button;
    private String currentColourClass;
    private boolean flipped;
    private CardRoles role;
    private String cardText;
    private String originalText;
    public Card(VBox button){
        this.button = button;
        this.currentColourClass = "WhiteCard";
    }

    public void setCurrentColourClass(String currentColourClass) {
        button.getStyleClass().remove(this.currentColourClass);
        button.getStyleClass().add(currentColourClass);
        this.currentColourClass = currentColourClass;
    }

    public CardRoles getRole() {
        return role;
    }

    public void setRole(CardRoles role) {
        this.role = role;
    }

    public VBox getVBox() {
        return button;
    }
    public String getText(){
        return cardText;
    }
    public void setText(String text){
        cardText = text;
        updateButtonText();
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
        setText(originalText);
    }

    private void updateButtonText(){
        ((Label)button.getChildren().getFirst()).setText(cardText);
    }
    private void updateButtonText(String cardText){
        ((Label)button.getChildren().getFirst()).setText(cardText);
    }


    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public void flipKeepText(String newClass){
        flip(getText(),newClass);
    }
    public void flip(String newClass){
        flip("",newClass);
    }
    public void flip(String newText,String newClass){
        // Create the lift animation (move up)
        TranslateTransition liftUp = new TranslateTransition(Duration.millis(150), button);
        liftUp.setByY(-20);
        liftUp.setInterpolator(Interpolator.EASE_OUT);

        // Create the put down animation (move back to original position)
        TranslateTransition putDown = new TranslateTransition(Duration.millis(150), button);
        putDown.setByY(20);
        putDown.setInterpolator(Interpolator.EASE_IN);

        // Create the rotation animation
        RotateTransition rotate = new RotateTransition(Duration.millis(300), button);
        rotate.setAxis(javafx.geometry.Point3D.ZERO.add(0, 1, 0)); // Rotate around Y-axis
        rotate.setByAngle(90);
        rotate.setInterpolator(Interpolator.EASE_BOTH);

        // Change text and class when rotation reaches 180 degrees
        rotate.setOnFinished(e -> {
            changeCard(newText,newClass);
            button.setRotate(-90);

        });
        RotateTransition rotate2 = new RotateTransition(Duration.millis(300), button);
        rotate2.setAxis(javafx.geometry.Point3D.ZERO.add(0, 1, 0)); // Rotate around Y-axis
        rotate2.setByAngle(90);
        rotate2.setInterpolator(Interpolator.EASE_BOTH);
        putDown.setOnFinished(e->{
            isCurrentlyAnimated = false;
        });

        // Create sequential animation: lift -> rotate -> put down
        SequentialTransition sequentialTransition = new SequentialTransition();
        sequentialTransition.getChildren().addAll(liftUp, rotate,rotate2, putDown);
        if(isCurrentlyAnimated){
            changeCard(newText,newClass);
        }else {
            isCurrentlyAnimated = true;

            sequentialTransition.play();
        }
    }
    private boolean isCurrentlyAnimated = false;
    private void changeCard(String newText,String newClass){
        setText(newText);
        setCurrentColourClass(newClass);
    }
}
