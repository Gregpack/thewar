package ru.gregpack.thewar.engine.network.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.network.messages.dto.mapping.GameStateMapper;
import ru.gregpack.thewar.network.messages.GameStatus;
import ru.gregpack.thewar.network.messages.GameStatusMessage;
import ru.gregpack.thewar.network.messages.ViewerConfirmMessage;
import ru.gregpack.thewar.utils.PropertyUtil;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class BasicAdapter implements NetworkAdapter, Closeable {
    private static final Logger logger = LogManager.getLogger(BasicAdapter.class.getName());

    protected final ObjectMapper objectMapper;
    protected final OutputStream outputStream;
    protected final GameStateMapper gameStateMapper;
    private final ViewerConfirmMessage viewerConfirmMessage;

    public BasicAdapter(OutputStream outputStream, GameStateMapper gameStateMapper, ObjectMapper objectMapper) {
        this.outputStream = outputStream;
        this.gameStateMapper = gameStateMapper;
        this.objectMapper = objectMapper;

        this.viewerConfirmMessage = new ViewerConfirmMessage();
        int height = PropertyUtil.getIntProperty("logic.field.height", 15);
        viewerConfirmMessage.setHeight(height);
        int length = PropertyUtil.getIntProperty("logic.field.length", 40);
        viewerConfirmMessage.setLength(length);
        String goldToWin = PropertyUtil.getProperty("logic.moneytowin", "1000");
        viewerConfirmMessage.setGoldToWin(goldToWin);
        int baseLength = PropertyUtil.getIntProperty("logic.base.length", 10);
        viewerConfirmMessage.setBaseLength(baseLength);
        int baseHeight = PropertyUtil.getIntProperty("logic.base.height", 15);
        viewerConfirmMessage.setBaseHeight(baseHeight);
        List<Coordinate> bases = PropertyUtil.getCoordinateList("logic.field.bases");
        viewerConfirmMessage.setBases(bases);
        int tickrate = PropertyUtil.getIntProperty("engine.logic.ticklength", 400);
        viewerConfirmMessage.setTickRate(tickrate);
    }

    @Override
    public void onNewGameState(GameState gameState) throws IOException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("{} sending gamestate dto: {}", this, gameState);
            }
            synchronized (outputStream) {
                GameStatusMessage gameStatusMessage = new GameStatusMessage(
                        GameStatus.IN_PROGRESS,
                        gameStateMapper.gameStateToDto(gameState),
                        null);
                objectMapper.writeValue(outputStream, gameStatusMessage);
            }
        } catch (IOException e) {
            logger.info("View adapter {} turned off. Reason - {}", this, e.getMessage());
            close();
            throw e;
        }
    }

    @Override
    public void onGameEnd(GamePlayer winner) {
        logger.info("View adapter {} turned off. Reason - game ended.", this);
        try {
            synchronized (outputStream) {
                GameStatusMessage gameStatusMessage = new GameStatusMessage(
                        GameStatus.END,
                        null,
                        gameStateMapper.playerToDto(winner));
                objectMapper.writeValue(outputStream, gameStatusMessage);
            }
        } catch (IOException ignored) {}
        close();
    }

    public void close() {
        try {
            synchronized (outputStream) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initAdapter() throws IOException {
        synchronized (outputStream) {
            objectMapper.writeValue(outputStream, viewerConfirmMessage);
        }
    }
}
