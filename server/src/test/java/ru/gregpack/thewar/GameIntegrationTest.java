package ru.gregpack.thewar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import ru.gregpack.thewar.engine.logic.GameLogicImpl;
import ru.gregpack.thewar.engine.logic.LogicModule;
import ru.gregpack.thewar.model.entities.composite.units.UnitType;
import ru.gregpack.thewar.network.messages.dto.mapping.GameStateMapper;
import ru.gregpack.thewar.engine.network.adapters.SocketPlayerAdapter;
import ru.gregpack.thewar.network.messages.OrderMessage;
import ru.gregpack.thewar.network.messages.orders.BuildOrder;
import ru.gregpack.thewar.network.messages.orders.EmptyOrder;
import ru.gregpack.thewar.network.messages.orders.Order;
import ru.gregpack.thewar.network.messages.orders.OrderQueue;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.composite.units.Footman;
import ru.gregpack.thewar.model.repositories.BarrackInMemoryRepository;
import ru.gregpack.thewar.model.repositories.PlayerInMemoryRepository;
import ru.gregpack.thewar.model.repositories.UnitInMemoryRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

public class GameIntegrationTest {

    private Injector injector = Guice.createInjector(new LogicModule());
    private GameLogicImpl gameLogic = injector.getInstance(GameLogicImpl.class);
    private OrderQueue orderQueue = injector.getInstance(OrderQueue.class);
    private GameStateMapper gameStateMapper = injector.getInstance(GameStateMapper.class);
    private GameState gameState = injector.getInstance(GameState.class);
    private ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);

    private final List<Thread> runningAdapters = new ArrayList<>();

    @BeforeEach
    public void wireInjects() {
        injector = Guice.createInjector(new LogicModule());
        gameLogic = injector.getInstance(GameLogicImpl.class);
        orderQueue = injector.getInstance(OrderQueue.class);
        gameStateMapper = injector.getInstance(GameStateMapper.class);
        gameState = injector.getInstance(GameState.class);
        objectMapper = injector.getInstance(ObjectMapper.class);
    }

    @AfterEach
    public void clean() {
        runningAdapters.forEach(Thread::interrupt);
    }

    @Test
    public void unitBaseDeathTest() throws InterruptedException {
        setupTwoPlayersWithOrders(
                List.of(),
                List.of()
        );
        GamePlayer firstPlayer = gameState.getPlayerService().getPlayers().get(0);
        Footman footman = new Footman(new Coordinate(69, 5));
        gameState.getUnitService().addUnit(footman, firstPlayer.getPlayerId());

        Assertions.assertEquals(1, gameState.getUnitService().getUnits().size());
        Assertions.assertEquals(500, firstPlayer.getMoney());

        gameLogic.nextTick();

        Assertions.assertEquals(0, gameState.getUnitService().getUnits().size());
        Assertions.assertEquals(500 + footman.getMoneyWorth(), firstPlayer.getMoney());
    }

    @Test
    public void unitKillTest() throws InterruptedException {
        setupTwoPlayersWithOrders(
                List.of(),
                List.of()
        );
        GamePlayer firstPlayer = gameState.getPlayerService().getPlayers().get(0);
        GamePlayer secondPlayer = gameState.getPlayerService().getPlayers().get(1);
        Footman footman1 = new Footman(new Coordinate(19, 5));
        Footman footman2 = new Footman(new Coordinate(20, 5));
        footman2.setHealthPoints(1);
        gameState.getUnitService().addUnit(footman1, firstPlayer.getPlayerId());
        gameState.getUnitService().addUnit(footman2, secondPlayer.getPlayerId());

        Assertions.assertEquals(2, gameState.getUnitService().getUnits().size());
        Assertions.assertEquals(500, firstPlayer.getMoney());

        gameLogic.nextTick();

        Assertions.assertEquals(1, gameState.getUnitService().getUnits().size());
        Assertions.assertEquals(500 + footman2.getMoneyWorth(), firstPlayer.getMoney());
    }

    @Test
    public void twoAdaptersTest() throws InterruptedException {
        setupTwoPlayersWithOrders(
                List.of(createBarrackOrder(3, 3), createBarrackOrder(3, 6)),
                List.of(createBarrackOrder(35, 3), createBarrackOrder(35, 6))
        );
        Thread.sleep(1000);
        Assertions.assertEquals(4, orderQueue.queueSize());
    }

    @Test
    public void serverWithTwoAdaptersTest() throws InterruptedException {
        setupTwoPlayersWithOrders(
                List.of(createBarrackOrder(3, 3), createBarrackOrder(3, 6)),
                List.of(createBarrackOrder(75, 3), createBarrackOrder(75, 6))
        );
        PlayerInMemoryRepository players = injector.getInstance(PlayerInMemoryRepository.class);
        players.getPlayers().forEach(p -> p.setMoney(2000));
        Thread.sleep(1000);
        for (int i = 0; i < 60; i++) {
            gameLogic.nextTick();
        }
        Assertions.assertEquals(0, orderQueue.queueSize());
        BarrackInMemoryRepository barrack = injector.getInstance(BarrackInMemoryRepository.class);
        Assertions.assertEquals(4, barrack.getBarracks().size());
        UnitInMemoryRepository unit = injector.getInstance(UnitInMemoryRepository.class);
        Assertions.assertEquals(4, unit.getUnits().size());
    }

    private void setupTwoPlayersWithOrders(List<Order> ordersOne, List<Order> ordersTwo) throws InterruptedException {
        GamePlayer playerOne = gameLogic.createPlayer("test");
        InputStream inputStreamOne = Mockito.mock(InputStream.class);
        try {
            OngoingStubbing<?> readStub = Mockito.when(inputStreamOne.read(any(byte[].class), anyInt(), anyInt()));
            for (Order order : ordersOne) {
                readStub = mockRead(readStub, wrapIntoMessage(order, playerOne.getPlayerId()));
            }
            readStub.then(invocation -> {
                Thread.sleep(100000);
                return -1;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        GamePlayer playerTwo = gameLogic.createPlayer("test2");
        InputStream inputStreamTwo = Mockito.mock(InputStream.class);
        try {
            OngoingStubbing<?> readStub = Mockito.when(inputStreamTwo.read(any(byte[].class), anyInt(), anyInt()));
            for (Order order : ordersTwo) {
                readStub = mockRead(readStub, wrapIntoMessage(order, playerTwo.getPlayerId()));
            }
            readStub.then(invocation -> {
                Thread.sleep(100000);
                return -1;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        PlayerInMemoryRepository player = injector.getInstance(PlayerInMemoryRepository.class);
        Assertions.assertEquals(2, player.getPlayers().size());

        SocketPlayerAdapter adapterOne = new SocketPlayerAdapter(playerOne,
                gameLogic,
                inputStreamOne,
                OutputStream.nullOutputStream(),
                gameStateMapper,
                objectMapper
        );

        SocketPlayerAdapter adapterTwo = new SocketPlayerAdapter(playerTwo,
                gameLogic,
                inputStreamTwo,
                OutputStream.nullOutputStream(),
                gameStateMapper,
                objectMapper
        );
        Thread threadOne = new Thread(adapterOne);
        Thread threadTwo = new Thread(adapterTwo);
        threadOne.start();
        threadTwo.start();
        runningAdapters.add(threadOne);
        runningAdapters.add(threadTwo);
        Thread.sleep(1000);
    }

    private OngoingStubbing<?> mockRead(OngoingStubbing<?> readStub, byte[] readResult) {
        return readStub.then(invocation -> {
            ByteArrayInputStream byteArrayInputStreamOne = new ByteArrayInputStream(readResult);
            return byteArrayInputStreamOne.read(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2));
        });
    }

    private Order createBarrackOrder(int x, int y) {
        return new BuildOrder(x, y, UnitType.FOOTMAN);
    }


    private byte[] wrapIntoMessage(Order order, int playerId) throws JsonProcessingException {
        order.setSenderId(playerId);
        OrderMessage message = new OrderMessage(List.of(order));
        return objectMapper.writeValueAsBytes(message);
    }
}
