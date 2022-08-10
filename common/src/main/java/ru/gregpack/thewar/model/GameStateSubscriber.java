package ru.gregpack.thewar.model;

import ru.gregpack.thewar.model.entities.GamePlayer;

public interface GameStateSubscriber {

    void onGameEnd(GamePlayer winner);
}
