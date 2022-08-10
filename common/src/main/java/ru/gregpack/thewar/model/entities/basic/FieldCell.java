package ru.gregpack.thewar.model.entities.basic;

import lombok.Getter;
import ru.gregpack.thewar.model.entities.composite.units.Barrack;
import ru.gregpack.thewar.model.entities.composite.units.Entity;
import ru.gregpack.thewar.model.entities.composite.units.Footman;
import ru.gregpack.thewar.model.entities.composite.units.Unit;

import java.util.ArrayList;
import java.util.List;

public class FieldCell {

    @Getter
    private Entity cellOccupant = null;

    public boolean isOccupied() {
        return cellOccupant != null;
    }

    public void addOccupant(Entity entity) {
        cellOccupant = entity;
    }

    public void removeOccupantById(int id) {
        if (cellOccupant != null && cellOccupant.getId() == id) {
            cellOccupant = null;
        }
    }

    @Override
    public String toString() {
        if (isOccupied()) {
            if (cellOccupant instanceof Barrack<?>) {
                return "#";
            }
            if (cellOccupant instanceof Unit) {
                return "F";
            }
        }
        return " ";
    }
}
