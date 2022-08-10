package ru.gregpack.thewar.engine.logic;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.model.GameField;
import ru.gregpack.thewar.model.GameTickSubscriber;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.FieldCell;
import ru.gregpack.thewar.model.entities.composite.units.Entity;
import ru.gregpack.thewar.network.messages.orders.Order;
import ru.gregpack.thewar.network.messages.orders.OrderQueue;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.GameStateSubscriber;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.entities.composite.units.Barrack;
import ru.gregpack.thewar.model.entities.composite.units.Unit;
import ru.gregpack.thewar.utils.PropertyUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class GameLogicImpl implements GameLogic, GameStateSubscriber {
    private static final Logger logger = LogManager.getLogger(GameLogic.class.getName());

    private final GameState gameState;

    private final OrderQueue orderQueue;

    private final List<GameTickSubscriber> gameStateListeners = new ArrayList<>();

    private static final int TICK_SLEEP_TIME = PropertyUtil.getIntProperty("engine.logic.ticklength", 2000);
    private long lastTickStartTime = 0;
    private boolean isGameEnded = false;
    private int ticks = 0;

    @Inject
    public GameLogicImpl(GameState gameState, OrderQueue orderQueue) {
        this.gameState = gameState;
        this.orderQueue = orderQueue;
        gameState.getPlayerService().addSubscriber(this);
    }

    @SneakyThrows
    @Override
    public boolean nextTick() {
        lastTickStartTime = System.currentTimeMillis();
        ticks++;
        long tickCalculateTime = System.currentTimeMillis();
        tickStart();
        synchronized (orderQueue) {
            tickCycle:
            while (true) {
                while (!orderQueue.hasNextOrder()) {
                    // if we are limiting each tick with a constant tick time, we wake up once in a while
                    // to make sure that tick did not end yet.
                    orderQueue.wait(10);
                    if (isTickEnded()) {
                        break tickCycle;
                    }
                }
                Order order = orderQueue.nextOrder();
                try {
                    logger.info("Executing order {}", order);
                    order.execute(gameState);
                } catch (IllegalArgumentException e) {
                    //TODO if i want more players to win - fix
                    GamePlayer winner = gameState.getPlayerService().getPlayers().stream().filter(p -> p.getPlayerId() != order.getSenderId()).findFirst().get();
                    logger.error("Error executing order: {}", e.getMessage());
                    onGameEnd(winner);
                    return true;
                }
            }
        }
        tickEnd();
        tickCalculateTime = System.currentTimeMillis() - tickCalculateTime;
        logger.info("Tick calculated in {} ms", tickCalculateTime - TICK_SLEEP_TIME);
        if (logger.isDebugEnabled()) {
            //logger.debug(gameState.getGameFieldPrintable());
        }
        return isGameEnded;
    }

    private void tickStart() {
        gameState.getUnitService().resetUnitsMovement();
        gameState.getUnitService().clearActions();
    }

    private void tickEnd() {
        for (Barrack<?> barrack : gameState.getBarrackService().getBarracks()) {
            Unit unit = barrack.generateUnit(gameState, ticks);
            gameState.getUnitService().addUnit(unit, gameState.getBarrackService().getBarrackOwnerId(barrack.getId()));
        }
        List<Integer> idleUnits = gameState.getUnitService().getNotMovedUnits();
        for (Integer idleUnitId : idleUnits) {
            Unit unit = gameState.getUnitService().getUnitById(idleUnitId);
            if (unit != null) {
                unit.act(gameState);
            }
        }
        gameState.getUnitService().removeDeadBodies();
        List<GameTickSubscriber> subsToRemove = new ArrayList<>();
        gameStateListeners.forEach(subscriber -> {
            try {
                subscriber.onNewGameState(gameState);
            } catch (IOException e) {
                subsToRemove.add(subscriber);
            }
        });
        gameStateListeners.removeAll(subsToRemove);
    }

    private boolean isTickEnded() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastTickStartTime > TICK_SLEEP_TIME;
    }

    @Override
    public void addSubscriber(GameTickSubscriber gameStateSubscriber) {
        gameStateListeners.add(gameStateSubscriber);
    }

    @Override
    public void removeSubscriber(GameTickSubscriber gameStateSubscriber) {
        gameStateListeners.remove(gameStateSubscriber);
    }

    @Override
    public GamePlayer createPlayer(String name) throws IllegalArgumentException {
        return gameState.createPlayer(name);
    }

    @Override
    public void handleOrder(Order order) {
        orderQueue.addOrder(order);
    }

    @Override
    public void onGameEnd(GamePlayer winner) {
        isGameEnded = true;
        gameStateListeners.forEach(gameTickSubscriber -> gameTickSubscriber.onGameEnd(winner));
        gameStateListeners.clear();
    }
}
