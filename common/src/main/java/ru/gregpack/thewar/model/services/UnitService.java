package ru.gregpack.thewar.model.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.model.GameField;
import ru.gregpack.thewar.model.UnitSubscriber;
import ru.gregpack.thewar.model.actions.AttackAction;
import ru.gregpack.thewar.model.actions.MoveAction;
import ru.gregpack.thewar.model.actions.SkillAction;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.Direction;
import ru.gregpack.thewar.model.entities.basic.SkillActivator;
import ru.gregpack.thewar.model.entities.composite.units.Unit;
import ru.gregpack.thewar.model.repositories.PlayerInMemoryRepository;
import ru.gregpack.thewar.model.repositories.UnitInMemoryRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class UnitService implements UnitSubscriber {
    private static final Logger logger = LogManager.getLogger(UnitService.class.getName());
    private final UnitInMemoryRepository unitRepository;
    private final PlayerInMemoryRepository playerRepository;
    private final GameField gameField;
    @Getter
    private final List<AttackAction> attackActions = new ArrayList<>();
    @Getter
    private final List<MoveAction> moveActions = new ArrayList<>();
    @Getter
    private final List<SkillAction> skillActions = new ArrayList<>();

    @Inject
    public UnitService(UnitInMemoryRepository unitRepository,
                       PlayerInMemoryRepository playerRepository,
                       GameField gameField) {
        this.unitRepository = unitRepository;
        this.playerRepository = playerRepository;
        this.gameField = gameField;
    }

    public void addUnit(Unit unit, int ownerId) {
        if (unit == null) {
            return;
        }
        if (unit.getPosition() == null) {
            throw new IllegalArgumentException("Unit " + unit + " position is null!");
        }
        gameField.addUnit(unit);
        unitRepository.addUnitToRepo(unit, ownerId);
        unitRepository.markUnitAsMoved(unit.getId());
        unit.subscribe(this);
    }

    private void removeUnit(Unit attacked) {
        int id = attacked.getId();
        Unit realUnit = unitRepository.getUnitById(id);
        if (realUnit == null) {
            return;
        }

        if (realUnit.getPosition() != null) {
            gameField.removeEntity(realUnit);
        }
        unitRepository.removeUnitFromRepo(id);
        attacked.desubscribe(this);
    }

    private void moveUnit(Unit unit, Coordinate oldPosition, Coordinate newPosition) {
        unit.setPosition(newPosition);
        gameField.removeUnit(oldPosition, unit.getId());
        gameField.addUnit(newPosition, unit);
    }

    public Unit getUnitById(int id) {
        return unitRepository.getUnitById(id);
    }

    public int getUnitOwnerId(int unitId) {
        return unitRepository.getUnitOwnerId(unitId);
    }

    public List<Integer> getNotMovedUnits() {
        return unitRepository.getNotMovedUnits();
    }

    public void resetUnitsMovement() {
        unitRepository.setUnitsNotMoved();
    }

    public Direction getUnitDefaultDirection(int id) {
        return unitRepository.getUnitDefaultDirection(id);
    }

    public void markUnitAsMoved(int id) {
        unitRepository.markUnitAsMoved(id);
    }

    public List<Direction> nextMoveToTarget(Unit unit, Coordinate target) {
        return gameField.nextMoveToTarget(unit, target);
    }

    // manhattan metric !
    public Unit findClosestEnemyUnitInRange(Unit searcher, int range) {
        int ownerId = getUnitOwnerId(searcher.getId());
        return gameField.findClosestUnitConditional(searcher.getPosition(), range, unit ->
                unit.isAlive() && getUnitOwnerId(unit.getId()) != ownerId
        );
    }

    public Collection<Unit> getUnits() {
        return unitRepository.getUnits();
    }

    public List<Unit> getSurroundingUnits(Unit unit) {
        List<Unit> list = new ArrayList<>();
        Coordinate coordinate = unit.getPosition();
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                if (x == 0 && y == 0) {
                    continue;
                }
                Unit foundUnit = gameField.checkUnitInCellConditional(coordinate.sub(x, y), (u) -> u.getPlayerId() == unit.getPlayerId());
                if (foundUnit != null) {
                    list.add(foundUnit);
                }
            }
        }
        return list;
    }

    private boolean isCellOccupied(Coordinate cell) {
        return gameField.isCellOccupied(cell);
    }

    public void clearActions() {
        attackActions.clear();
        moveActions.clear();
    }

    public void removeDeadBodies() {
        for (Unit unit : unitRepository.getUnits()) {
            if (!unit.isAlive()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unit {} is now dead.", unit);
                }
                removeUnit(unit);
            }
        }
    }

    @Override
    public void onAttack(Unit attacker, Unit attacked) {
        markUnitAsMoved(attacker.getId());
        if (logger.isDebugEnabled()) {
            logger.debug("Unit {} attacked {}.", attacker, attacked);
        }
        if (!attacked.isAlive()) {
            //if (logger.isDebugEnabled()) {
            //    logger.debug("Unit {} is now dead.", attacked);
            //}
            //removeUnit(attacked);
            GamePlayer player = playerRepository.getPlayerById(unitRepository.getUnitOwnerId(attacker.getId()));
            player.addMoney(attacked.getMoneyWorth());
        }
        attackActions.add(new AttackAction(attacker.getId(), attacked.getId()));
    }

    @Override
    public void onMove(Unit unit, Coordinate oldPosition, Coordinate newPosition) {
        if (unit.getHealthPoints() <= 0) {
            return;
        }
        if (gameField.isCellOutsideBounds(newPosition) || isCellOccupied(newPosition)) {
            unit.setPosition(oldPosition);
            if (logger.isDebugEnabled()) {
                logger.debug("Unit {} tried to move from {} to {}. It is occupied.", unit, oldPosition, newPosition);
            }
        } else {
            MoveAction moveAction = new MoveAction(oldPosition.getX(), oldPosition.getY(), newPosition.getX(), newPosition.getY());
            moveAction.setPerformerId(unit.getId());
            moveActions.add(moveAction);
            moveUnit(unit, oldPosition, newPosition);
            if (logger.isDebugEnabled()) {
                logger.debug("Unit {} moved from {} to {}.", unit, oldPosition, newPosition);
            }
            int ownerId = unitRepository.getUnitOwnerId(unit.getId());
            int cellOwnerId = gameField.getCellOwner(newPosition);
            if (cellOwnerId != -1 && cellOwnerId != ownerId) {
                GamePlayer player = playerRepository.getPlayerById(ownerId);
                player.addMoney(unit.getMoneyWorth());
                unit.setHealthPoints(-1);
                removeUnit(unit);
                // we have no need to tell unit that he is dead - in removeUnit we desubscribe him so after he finishes his moves he will disappear
                return;
            }
        }
        markUnitAsMoved(unit.getId());
    }

    @Override
    public void onSkill(Unit unit, SkillActivator skillActivator) {
        //skillActions.add(new SkillAction(skillActivator.getX(), skillActivator.getY()));
    }
}
