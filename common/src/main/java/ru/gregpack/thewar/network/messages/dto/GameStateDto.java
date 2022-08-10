package ru.gregpack.thewar.network.messages.dto;

import lombok.Data;
import ru.gregpack.thewar.model.actions.AttackAction;
import ru.gregpack.thewar.model.actions.MoveAction;

import java.util.List;
import java.util.Map;

@Data
public class GameStateDto {
    private Map<Integer, BarrackDto> barracks;
    private Map<Integer, UnitDto> units;
    private Map<Integer, PlayerDto> players;
    private List<AttackAction> attackActions;
    private List<MoveAction> moveActions;
    private int[][] field;
}
