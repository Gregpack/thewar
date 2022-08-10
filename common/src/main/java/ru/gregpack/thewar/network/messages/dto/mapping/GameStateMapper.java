package ru.gregpack.thewar.network.messages.dto.mapping;

import org.mapstruct.*;
import ru.gregpack.thewar.model.GameField;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.entities.basic.FieldCell;
import ru.gregpack.thewar.model.entities.composite.units.*;
import ru.gregpack.thewar.model.repositories.BarrackInMemoryRepository;
import ru.gregpack.thewar.model.repositories.UnitInMemoryRepository;
import ru.gregpack.thewar.network.messages.dto.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "jsr330")
public abstract class GameStateMapper {

    protected BarrackInMemoryRepository barrackRepository;
    protected UnitInMemoryRepository unitRepository;

    public void setUnitRepository(UnitInMemoryRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    public void setBarrackRepository(BarrackInMemoryRepository barrackRepository) {
        this.barrackRepository = barrackRepository;
    }

    @Mappings({
            @Mapping(target = "x", source = "position.x"),
            @Mapping(target = "y", source = "position.y")
    })
    public abstract void entityToDto(@MappingTarget EntityDto dto, Entity entity);

    @BeforeMapping
    protected void enrichDTOWithUnitTypeAndSuperClass(Unit unit, @MappingTarget UnitDto unitDto) {
        this.entityToDto(unitDto, unit);
        if (unit instanceof Footman) {
            unitDto.setUnitType(UnitType.FOOTMAN);
        } else if (unit instanceof Archer) {
            unitDto.setUnitType(UnitType.ARCHER);
        } else if (unit instanceof Assassin) {
            unitDto.setUnitType(UnitType.ASSASSIN);
        } else if (unit instanceof Cavalry) {
            unitDto.setUnitType(UnitType.CAVALRY);
        } else if (unit instanceof Alchemist) {
            unitDto.setUnitType(UnitType.ALCHEMIST);
        }
    }

    @Mappings({
            @Mapping(target = "behaviourTriggerUnitId", source = "behaviourTriggerUnit.id"),
    })
    public abstract UnitDto unitToDto(Unit unit);

    public abstract List<UnitDto> unitToDto(Collection<Unit> units);

    @BeforeMapping
    protected void enrichDTOWithSpawnProgressAndSuperClass(@MappingTarget BarrackDto unitDto, Barrack<?> unit) {
        this.entityToDto(unitDto, unit);
    }

    @Mappings({
            @Mapping(target = "spawnX", source = "defaultSpawnCoordinate.x"),
            @Mapping(target = "spawnY", source = "defaultSpawnCoordinate.y"),
            @Mapping(target = "playerId", expression = "java(barrackRepository.getBarrackOwnerById(barrack.getId()))")
    })
    public abstract BarrackDto barrackToDto(Barrack<?> barrack);

    public abstract List<BarrackDto> barrackToDto(Collection<Barrack<?>> barracks);

    @Mappings({
            @Mapping(target = "id", source = "playerId"),
            @Mapping(target = "baseX", source = "base.x"),
            @Mapping(target = "baseY", source = "base.y")
    })
    public abstract PlayerDto playerToDto(GamePlayer player);

    public abstract List<PlayerDto> playerToDto(List<GamePlayer> player);

    public GameStateDto gameStateToDto(GameState gameState) {
        GameStateDto gameStateDto = new GameStateDto();
        gameStateDto.setUnits(listToMapWithId(this.unitToDto(gameState.getUnitService().getUnits())));
        gameStateDto.setBarracks(listToMapWithId(this.barrackToDto(gameState.getBarrackService().getBarracks())));
        gameStateDto.setPlayers(listToMapWithId(this.playerToDto(gameState.getPlayerService().getPlayers())));
        gameStateDto.setAttackActions(gameState.getUnitService().getAttackActions());
        gameStateDto.setMoveActions(gameState.getUnitService().getMoveActions());
        gameStateDto.setField(gameFieldToArray(gameState.getGameField()));
        return gameStateDto;
    }

    private int[][] gameFieldToArray(GameField gameField) {
        FieldCell[][] cells = gameField.getGameField();
        int[][] result = new int[cells.length][cells[0].length];
        for (int y = 0; y < cells.length; y++) {
            FieldCell[] cell = cells[y];
            for (int x = 0; x < cell.length; x++) {
                result[y][x] = cell[x].getCellOccupant() == null ? -1 : cell[x].getCellOccupant().getId();
            }
        }
        return result;
    }

    private <T extends IdentifiableDto> Map<Integer, T> listToMapWithId(List<T> list) {
        Map<Integer, T> result = new HashMap<>();
        for (T t : list) {
            result.put(t.getId(), t);
        }
        return result;
    }

}
