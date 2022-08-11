package ru.gregpack.thewar.model.repositories;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.entities.UnitInfo;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.Direction;
import ru.gregpack.thewar.model.entities.composite.units.Unit;

import java.util.*;

@Singleton
public class UnitInMemoryRepository {

    private final Map<Integer, Unit> unitById = new HashMap<>();
    private final Map<Integer, UnitInfo> unitInfoById = new HashMap<>();
    private final PlayerInMemoryRepository playerRepository;

    @Inject
    public UnitInMemoryRepository(PlayerInMemoryRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Collection<Unit> getUnits() {
        return new ArrayList<>(unitById.values());
    }

    public Unit getUnitById(int id) {
        return unitById.getOrDefault(id, null);
    }

    public UnitInfo getUnitInfoById(int id) {
        return unitInfoById.getOrDefault(id, null);
    }

    public void markUnitAsMoved(int id) {
        getUnitInfoById(id).setDidItsMove(true);
    }

    public void setUnitsNotMoved() {
        unitInfoById.values().forEach(unit -> unit.setDidItsMove(false));
    }

    public List<Integer> getNotMovedUnits() {
        List<Integer> notMovedUnits = new ArrayList<>();
        unitInfoById.forEach((id, info) -> {
            if (!info.isDidItsMove()) {
                notMovedUnits.add(id);
            }
        });
        return notMovedUnits;
    }

    public int getUnitOwnerId(int unitId) {
        UnitInfo unitInfo = unitInfoById.get(unitId);
        if (unitInfo == null) {
            return -1;
        }
        return unitInfo.getOwnerId();
    }

    public Direction getUnitDefaultDirection(int unitId) {
        GamePlayer gamePlayer = unitInfoById.get(unitId).getOwner();
        Unit unit = unitById.get(unitId);
        Coordinate target = playerRepository.getPlayers().stream()
                .filter(player -> gamePlayer.getPlayerId() != player.getPlayerId())
                .min(Comparator.comparingInt(player -> player.getBase().manhattanRange(gamePlayer.getBase())))
                .get().getBase().sub(unit.getPosition());
        if (target.getX() == 0) {
            if (target.getY() > 0) {
                return Direction.UP;
            }
            return Direction.DOWN;
        }
        if (target.getX() > 0) {
            return Direction.RIGHT;
        }
        return Direction.LEFT;
    }

    public void addUnitToRepo(Unit unit, int ownerId) {
        unitById.put(unit.getId(), unit);
        unitInfoById.put(unit.getId(),
                UnitInfo.builder().owner(playerRepository.getPlayerById(ownerId)).build());
    }

    public void removeUnitFromRepo(int unitId) {
        unitById.remove(unitId);
        unitInfoById.remove(unitId);
    }
}
