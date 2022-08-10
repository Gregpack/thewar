package ru.gregpack.thewar.view.canvas;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ru.gregpack.thewar.network.messages.dto.GameStateDto;
import ru.gregpack.thewar.network.messages.dto.PlayerDto;

import java.util.HashMap;
import java.util.Map;


public class GameStatusPanel {

    public final static int HEIGHT = 100;
    public final static int FONT = 30;
    public final static int INSET_SIZE = (HEIGHT - FONT) / 2;

    private final BorderPane panel;
    private final Map<String, Label> playerLabels = new HashMap<>();
    private final Label playerOneLabel;
    private final Label playerTwoLabel;
    private final String goldToWin;

    public GameStatusPanel(String goldToWin) {
        Insets insets = new Insets(INSET_SIZE);
        panel = new BorderPane();
        panel.setPrefHeight(HEIGHT);
        playerOneLabel = createTextLabel("Waiting for game to start...");
        playerTwoLabel = createTextLabel("");
        this.goldToWin = goldToWin;
        panel.setLeft(playerOneLabel);
        BorderPane.setMargin(playerOneLabel, insets);
        panel.setRight(playerTwoLabel);
        BorderPane.setMargin(playerTwoLabel, insets);
    }

    public BorderPane getPanel() {
        return panel;
    }

    public void drawGameState(GameStateDto gameStateDto) {
        PlayerDto playerOne = gameStateDto.getPlayers().get(0);
        if (!playerLabels.containsKey(playerOne.getName() + playerOne.getId())) {
            playerLabels.put(playerOne.getName() + playerOne.getId(), playerOneLabel);
        }
        PlayerDto playerTwo = gameStateDto.getPlayers().get(1);
        if (!playerLabels.containsKey(playerTwo.getName() + playerTwo.getId())) {
            playerLabels.put(playerTwo.getName() + playerTwo.getId(), playerTwoLabel);
        }
        playerLabels.get(playerOne.getName() + playerOne.getId()).setText(String.format("%s: %d/%s", playerOne.getName(), playerOne.getMoney(), goldToWin));
        playerLabels.get(playerTwo.getName() + playerTwo.getId()).setText(String.format("%s: %d/%s", playerTwo.getName(), playerTwo.getMoney(), goldToWin));
    }

    private Label createTextLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Times New Roman", FontWeight.BOLD, FONT));
        return label;
    }

    public void drawWinner(PlayerDto winner) {
        Label winnerLabel = playerLabels.get(winner.getName() + winner.getId());
        winnerLabel.setText(String.format("%s is a winner!", winner.getName()));
    }
}
