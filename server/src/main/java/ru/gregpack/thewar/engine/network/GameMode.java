package ru.gregpack.thewar.engine.network;


import lombok.Getter;

public enum GameMode {
    Singleplayer(1),
    Multiplayer(2),
    AIvsAI(3);
    @Getter
    private final int value;

    GameMode(int i) {
        value = i;
    }
}
