package ru.gregpack.thewar.network.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.gregpack.thewar.network.messages.dto.GameStateDto;
import ru.gregpack.thewar.network.messages.dto.PlayerDto;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GameStatusMessage {
    @Getter
    private GameStatus gameStatus;

    @Getter
    private GameStateDto gameState;

    @Getter
    private PlayerDto gameWinner;
}
