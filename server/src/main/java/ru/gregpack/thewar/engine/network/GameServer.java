package ru.gregpack.thewar.engine.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.engine.bots.*;
import ru.gregpack.thewar.engine.logic.GameLogic;
import ru.gregpack.thewar.engine.network.adapters.*;
import ru.gregpack.thewar.network.AI;
import ru.gregpack.thewar.network.messages.dto.mapping.GameStateMapper;
import ru.gregpack.thewar.network.messages.ErrorMessage;
import ru.gregpack.thewar.network.messages.RegistrationMessage;
import ru.gregpack.thewar.network.messages.Role;
import ru.gregpack.thewar.utils.PropertyUtil;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    private static final Logger logger = LogManager.getLogger(GameServer.class.getName());
    private static final int MAX_PLAYERS = PropertyUtil.getIntProperty("engine.network.maxplayers", 2);
    private static final int MAX_TICKS = PropertyUtil.getIntProperty("engine.network.maxticks", 1000);
    private final ServerSocket serverSocket = new ServerSocket(PropertyUtil.getIntProperty("engine.network.port", 1234));
    private final GameLogic gameLogic;
    private final ObjectMapper objectMapper;
    private final GameStateMapper gameStateMapper;
    private final List<PlayerAdapter> adapters = new ArrayList<>();

    private int players = 0;
    private boolean isGameFinished = false;

    @Inject
    public GameServer(ObjectMapper objectMapper,
                      GameLogic gameLogic,
                      GameStateMapper gameStateMapper) throws IOException {
        this.gameLogic = gameLogic;
        this.gameStateMapper = gameStateMapper;
        this.objectMapper = objectMapper;
    }

    public void launch(GameMode gameMode, List<BotType> botTypes) {
        logger.info("Server started on port {}", serverSocket.getLocalPort());
        try {
            serverSocket.setSoTimeout(2000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            switch (gameMode) {
                case Multiplayer:
                    break;
                case AIvsAI:
                    AI ai = botTypes.get(1).toAI();
                    createAIAdapter(ai, gameLogic);
                case Singleplayer:
                    ai = botTypes.get(0).toAI();
                    createAIAdapter(ai, gameLogic);
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            try {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    if (isGameFinished) {
                        break;
                    }
                    continue;
                }
                RegistrationMessage message = objectMapper.readValue(socket.getInputStream(), RegistrationMessage.class);
                NetworkAdapter networkAdapter = createAdapter(socket, message.getRole(), message.getName());
                if (networkAdapter != null) {
                    logger.info("Got new adapter {}", networkAdapter);
                    gameLogic.addSubscriber(networkAdapter);
                    if (networkAdapter instanceof PlayerAdapter) {
                        adapters.add((PlayerAdapter) networkAdapter);
                        players++;
                    }

                    if (players == MAX_PLAYERS) {
                        launchGame();
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to create new adapter: {}", e.getMessage());
            }
        }
    }

    private NetworkAdapter createAdapter(Socket socket, Role role, @Nullable String name) throws IOException {
        NetworkAdapter adapter = null;
        switch (role) {
            case PLAYER:
                try {
                    adapter = createPlayerAdapter(socket, name);
                } catch (IllegalArgumentException e) {
                    objectMapper.writeValue(socket.getOutputStream(), new ErrorMessage(e.getMessage()));
                    socket.close();
                }
                break;
            case VIEWER:
                adapter = createViewAdapter(socket);
        }
        if (adapter != null) {
            adapter.initAdapter();
        }
        return adapter;
    }

    private NetworkAdapter createViewAdapter(Socket socket) throws IOException {
        return new BasicAdapter(socket.getOutputStream(), gameStateMapper, objectMapper);
    }

    private NetworkAdapter createPlayerAdapter(Socket socket, String name) throws IOException {
        if (players >= MAX_PLAYERS) {
            objectMapper.writeValue(socket.getOutputStream(), new ErrorMessage("No more room for players!"));
            return null;
        }
        return new SocketPlayerAdapter(gameLogic.createPlayer(name), gameLogic,
                socket.getInputStream(),
                socket.getOutputStream(),
                gameStateMapper,
                objectMapper
        );
    }

    private void createAIAdapter(AI ai, GameLogic gameLogic) throws IOException {
        if (players >= MAX_PLAYERS) {
            return;
        }
        logger.info("Creating AI adapter.");
        PlayerAdapter networkAdapter = new AIPlayerAdapter(gameLogic.createPlayer(ai.getName()), gameLogic, ai);
        gameLogic.addSubscriber(networkAdapter);
        adapters.add(networkAdapter);
        networkAdapter.initAdapter();
        players++;
    }

    private void launchGame() {
        logger.info("Starting game.");
        adapters.forEach(adapter -> {
            Thread thread = new Thread(adapter, adapter.toString());
            thread.start();
        });
        Thread thread = new Thread(() -> {
            int ticks = -1;
            while (ticks < MAX_TICKS && !isGameFinished) {
                isGameFinished = gameLogic.nextTick();
                ticks++;
            }
        }, "server");
        thread.start();
    }

}
