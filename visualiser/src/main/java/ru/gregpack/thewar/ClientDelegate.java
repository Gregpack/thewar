package ru.gregpack.thewar;

import ru.gregpack.thewar.network.messages.dto.GameStateDto;
import ru.gregpack.thewar.network.messages.dto.PlayerDto;

import java.util.function.Consumer;

public interface ClientDelegate {
    void startListening(Consumer<GameStateDto> onNewGameState, Runnable onServerFail, Consumer<PlayerDto> onGameEnd);
    void shutdown();
}
