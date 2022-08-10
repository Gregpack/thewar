package ru.gregpack.thewar.model.entities.composite.units;

import lombok.ToString;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.Direction;

@ToString(callSuper = true, includeFieldNames = false)
public class Archer extends Unit {

    private static final String propertyName = "archer";

    public Archer() {
        super(propertyName);
    }

    public Archer(Coordinate position) {
        super(propertyName, position);
    }

}
