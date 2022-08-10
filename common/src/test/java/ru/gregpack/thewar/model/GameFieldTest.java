package ru.gregpack.thewar.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.Direction;
import ru.gregpack.thewar.model.entities.composite.units.Footman;
import ru.gregpack.thewar.model.entities.composite.units.Unit;

import java.util.List;

public class GameFieldTest {

    @Test
    public void findClosestUnitConditionalTest() {
        GameField field = new GameField();
        Unit footman2 = new Footman(new Coordinate(7, 9));
        field.addUnit(new Footman(new Coordinate(3, 5)));
        field.addUnit(footman2);

        Unit firstTest = field.findClosestUnitConditional(new Coordinate(3, 5), 2, (unit) -> true);
        Assertions.assertNull(firstTest);

        Unit secondTest = field.findClosestUnitConditional(new Coordinate(3, 5), 8, (unit) -> true);
        Assertions.assertEquals(footman2, secondTest);
    }

    @Test
    public void aStarTest() {
        GameField field = new GameField();
        field.addUnit(new Footman(new Coordinate(4, 5)));
        field.addUnit(new Footman(new Coordinate(5, 5)));
        field.addUnit(new Footman(new Coordinate(6, 5)));
        field.addUnit(new Footman(new Coordinate(7, 5)));

        Coordinate to = new Coordinate(5, 7);
        Coordinate from = new Coordinate(5, 3);

        List<Direction> direction = field.aStarAlgorithm(from, to);
        Assertions.assertEquals(direction.get(0), Direction.UP);
    }


    @Test
    public void findFreeSpaceAroundBarrackTest() {
        GameField field = new GameField();
        field.addUnit(new Footman(new Coordinate(1, 1)));
        field.addUnit(new Footman(new Coordinate(2, 1)));
        field.addUnit(new Footman(new Coordinate(3, 1)));
        field.addUnit(new Footman(new Coordinate(4, 1)));
        field.addUnit(new Footman(new Coordinate(5, 1)));
        field.addUnit(new Footman(new Coordinate(5, 2)));

        Coordinate firstTest = field.findFreeSpaceAroundBarrack(new Coordinate(2, 2));
        Assertions.assertEquals(new Coordinate(5, 3), firstTest);

        Coordinate secondTest = field.findFreeSpaceAroundBarrack(new Coordinate(0, 0));
        Assertions.assertEquals(new Coordinate(3, 0), secondTest);
    }

}
