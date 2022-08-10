package ru.gregpack.thewar.model;

import ru.gregpack.thewar.model.entities.GamePlayer;

import java.io.IOException;

public interface GameTickSubscriber {

    void onNewGameState(GameState gameState) throws IOException;

    void onGameEnd(GamePlayer winner);
}
