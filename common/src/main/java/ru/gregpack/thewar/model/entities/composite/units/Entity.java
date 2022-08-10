package ru.gregpack.thewar.model.entities.composite.units;

import lombok.Getter;
import lombok.Setter;
import ru.gregpack.thewar.model.entities.basic.Coordinate;

@Getter
@Setter
public class Entity {
    private Coordinate position;
    private int id = nextId();
    private int playerId;

    private static int idCounter = 0;
    private static int nextId() {
        int id = idCounter;
        idCounter++;
        return id;
    }
}
