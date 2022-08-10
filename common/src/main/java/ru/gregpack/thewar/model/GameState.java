package ru.gregpack.thewar.model;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.FieldCell;
import ru.gregpack.thewar.model.entities.composite.units.Barrack;
import ru.gregpack.thewar.model.entities.composite.units.Footman;
import ru.gregpack.thewar.model.services.BarrackService;
import ru.gregpack.thewar.model.services.PlayerService;
import ru.gregpack.thewar.model.services.UnitService;

@Singleton
public class GameState {
    @Getter
    private final GameField gameField;
    @Getter
    private final UnitService unitService;
    @Getter
    private final BarrackService barrackService;
    @Getter
    private final PlayerService playerService;

    @Inject
    public GameState(UnitService unitService,
                     BarrackService barrackService,
                     PlayerService playerService,
                     GameField gameField) {
        this.unitService = unitService;
        this.barrackService = barrackService;
        this.playerService = playerService;
        this.gameField = gameField;
    }

    public GamePlayer createPlayer(String name) {
        GamePlayer player = playerService.createPlayer(name);
        Coordinate base = gameField.getFreeBase();
        gameField.occupyFreeBase(base, player.getPlayerId());
        player.setBase(base);
        return player;
    }

    public int getCellOwner(Coordinate cell) {
        return gameField.getCellOwner(cell);
    }

    public boolean isSpaceOccupied(Coordinate coordinate, int size) {
        return gameField.isSpaceOccupied(coordinate, size);
    }

    public boolean isCellOccupied(Coordinate coordinate) {
        return gameField.isCellOccupied(coordinate);
    }

//    public String getGameFieldPrintable() {
//        StringBuilder sb = new StringBuilder();
//        for (FieldCell[] fieldCells : gameField.getGameField()) {
//            for (FieldCell cell : fieldCells) {
//                if (cell.isOccupied()) {
//                    if (cell.getCellOccupants().stream().anyMatch(e -> e instanceof Barrack<?>)) {
//                        sb.append("#");
//                    } else if (cell.getCellOccupants().stream().anyMatch(e -> e instanceof Footman)) {
//                        Footman footman = (Footman) cell.getCellOccupants().stream().filter(e -> e instanceof Footman).findFirst().get();
//                        int ownerId = unitService.getUnitOwnerId(footman.getId());
//                        sb.append(ownerId);
//                    }
//                } else {
//                    sb.append(" ");
//                }
//            }
//            sb.append('\n');
//        }
//        return sb.toString();
//    }
}
