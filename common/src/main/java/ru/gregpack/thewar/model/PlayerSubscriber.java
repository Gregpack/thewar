package ru.gregpack.thewar.model;

import ru.gregpack.thewar.model.entities.GamePlayer;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.composite.units.Unit;

public interface PlayerSubscriber {
    void onMoneyAddition(GamePlayer player);
}
