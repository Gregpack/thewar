package ru.gregpack.thewar.model.repositories;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ru.gregpack.thewar.model.entities.BarrackInfo;
import ru.gregpack.thewar.model.entities.composite.units.Barrack;

import java.util.*;

@Singleton
public class BarrackInMemoryRepository {
    private final Map<Integer, Barrack<?>> barrackByIdMap = new HashMap<>();
    private final Map<Integer, BarrackInfo> barrackInfoById = new HashMap<>();
    private final PlayerInMemoryRepository playerRepository;

    @Inject
    public BarrackInMemoryRepository(PlayerInMemoryRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Barrack<?> getBarrackById(int id) {
        return barrackByIdMap.getOrDefault(id, null);
    }

    public BarrackInfo getBarrackInfoById(int id) {
        return barrackInfoById.getOrDefault(id, null);
    }

    public int getBarrackOwnerById(int id) {
        BarrackInfo barrackInfo = getBarrackInfoById(id);
        if (barrackInfo != null) {
            return barrackInfo.getOwnerId();
        }
        return -1;
    }

    public void addBarrack(Barrack<?> barrack, int ownerId) {
        barrackByIdMap.put(barrack.getId(), barrack);
        barrackInfoById.put(barrack.getId(),
                BarrackInfo.builder().owner(playerRepository.getPlayerById(ownerId)).build()
        );
    }

    public void removeBarrack(int barrackId) {
        barrackByIdMap.remove(barrackId);
        barrackInfoById.remove(barrackId);
    }

    public List<Integer> getBarracksIds() {
        return new ArrayList<>(barrackByIdMap.keySet());
    }

    public Collection<Barrack<?>> getBarracks() {
        return new ArrayList<>(barrackByIdMap.values());
    }
}
