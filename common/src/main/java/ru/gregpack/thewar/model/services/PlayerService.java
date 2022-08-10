package ru.gregpack.thewar.model.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ru.gregpack.thewar.model.GameStateSubscriber;
import ru.gregpack.thewar.model.PlayerSubscriber;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.repositories.PlayerInMemoryRepository;
import ru.gregpack.thewar.utils.PropertyUtil;

import java.util.List;

@Singleton
public class PlayerService implements PlayerSubscriber {
    private final PlayerInMemoryRepository playerRepository;
    private final int moneyToWin = PropertyUtil.getIntProperty("logic.moneytowin", 1000);
    private GameStateSubscriber gameStateSubscriber = null;

    @Inject
    public PlayerService(PlayerInMemoryRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public GamePlayer getPlayerById(int ownerId) {
        return playerRepository.getPlayerById(ownerId);
    }

    public List<GamePlayer> getPlayers() {
        return playerRepository.getPlayers();
    }

    public GamePlayer createPlayer(String name) throws IllegalArgumentException {
        GamePlayer gamePlayer = playerRepository.createPlayer(name);
        gamePlayer.setPlayerSubscriber(this);
        return gamePlayer;
    }

    public void addSubscriber(GameStateSubscriber gameStateSubscriber) {
        this.gameStateSubscriber = gameStateSubscriber;
    }

    @Override
    public void onMoneyAddition(GamePlayer player) {
        if (player.getMoney() >= moneyToWin) {
            gameStateSubscriber.onGameEnd(player);
        }
    }

}
