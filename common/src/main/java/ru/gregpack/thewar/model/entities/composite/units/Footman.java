package ru.gregpack.thewar.model.entities.composite.units;

import lombok.ToString;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.basic.Armor;
import ru.gregpack.thewar.model.entities.basic.Attack;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.Direction;
import ru.gregpack.thewar.utils.PropertyUtil;

@ToString(callSuper = true, includeFieldNames = false)
public class Footman extends Unit {

    private static final String propertyName = "footman";

    public Footman() {
        super(propertyName);
    }

    public Footman(Coordinate position) {
        super(propertyName, position);
    }

}
