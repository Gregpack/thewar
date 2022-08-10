package ru.gregpack.thewar.model.entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UnitInfo {

    private boolean didItsMove;
    private GamePlayer owner;

    public int getOwnerId() {
        return owner.getPlayerId();
    }

}
