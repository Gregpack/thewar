package ru.gregpack.thewar.model.repositories;

import com.google.inject.Singleton;
import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class PlayerInMemoryRepository {

    private final Map<Integer, GamePlayer> gamePlayers = new HashMap<>();

    public GamePlayer getPlayerById(int ownerId) {
        return gamePlayers.getOrDefault(ownerId, null);
    }

    public GamePlayer createPlayer(String name) {
        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setName(name);
        gamePlayers.put(gamePlayer.getPlayerId(), gamePlayer);
        return gamePlayer;
    }

    public List<GamePlayer> getPlayers() {
        return new ArrayList<>(gamePlayers.values());
    }
}
