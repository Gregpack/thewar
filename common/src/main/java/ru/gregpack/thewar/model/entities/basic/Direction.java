package ru.gregpack.thewar.model.entities.basic;

import java.util.List;
import java.util.Map;

import static ru.gregpack.thewar.model.entities.basic.Coordinate.*;

public enum Direction {
    LEFT,
    RIGHT,
    UP,
    DOWN;

    private static final List<Direction> DIRECTIONS = List.of(UP, DOWN, LEFT, RIGHT);

    private static final Map<Direction, Coordinate> toCoordinateMap = Map.of(
            UP, UP_COORD,
            DOWN, DOWN_COORD,
            LEFT, LEFT_COORD,
            RIGHT, RIGHT_COORD
    );

    private static final Map<Direction, Direction> reverseMap = Map.of(
            UP, DOWN,
            DOWN, UP,
            LEFT, RIGHT,
            RIGHT, LEFT
    );

    private static final Map<Direction, String> toStringMap = Map.of(
            UP, "u",
            DOWN, "d",
            LEFT, "r",
            RIGHT, "l"
    );

    public static List<Direction> getDirections() {
        return DIRECTIONS;
    }

    public Coordinate toCoordinate() {
        return toCoordinateMap.get(this);
    }

    public Direction reverse() {
        return reverseMap.get(this);
    }

    public String toString() {
        return toStringMap.get(this);
    }
}
