package ru.gregpack.thewar.model.entities.basic;


import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Coordinate {

    public static final Coordinate UP_COORD = new Coordinate(0, 1);
    public static final Coordinate DOWN_COORD = new Coordinate(0, -1);
    public static final Coordinate LEFT_COORD = new Coordinate(-1, 0);
    public static final Coordinate RIGHT_COORD = new Coordinate(1, 0);

    private int x;
    private int y;

    // important: in int coordinates euclid metric includes more coords starting from r >= 3

    // we use euclid metric for attack range
    public boolean isInRange(Coordinate second, int range) {
        double xMetric = Math.pow(second.x - x, 2);
        double yMetric = Math.pow(second.y - y, 2);
        return range * range >= xMetric + yMetric;
    }

    // we use manhattan metric for behaviour checks - units aggro and so on
    public boolean isInManhattanRange(Coordinate second, int range) {
        return range >= manhattanRange(second);
    }

    public int manhattanRange(Coordinate second) {
        int xDiff = Math.abs(second.x - x);
        int yDiff = Math.abs(second.y - y);
        return xDiff + yDiff;
    }

    public boolean areInNextCells(Coordinate second) {
        return Math.abs(this.x - second.x) <= 1 || Math.abs(this.y - second.y) <= 1;
    }

    public Coordinate multiply(int mult) {
        return new Coordinate(x * mult, y * mult);
    }

    public Coordinate add(Coordinate coord) {
        return new Coordinate(x + coord.x, y + coord.y);
    }

    public void addMutable(Coordinate coord) {
        this.x = x + coord.x;
        this.y = y + coord.y;
    }

    public Coordinate add(Direction side) {
        return this.add(side.toCoordinate());
    }

    public void addMutable(Direction side) {
        this.addMutable(side.toCoordinate());
    }

    public Coordinate sub(Coordinate coord) {
        return new Coordinate(x - coord.x, y - coord.y);
    }

    public Coordinate sub(int x, int y) {
        return new Coordinate(this.x - x, this.y - y);
    }

    public Coordinate nCellsToTheDirection(Direction side, int n) {
        return this.add(side.toCoordinate().multiply(n));
    }

    public Direction toDirection() {
        if (x == 0) {
            return y > 0 ? Direction.UP : Direction.DOWN;
        }
        return x > 0 ? Direction.RIGHT : Direction.LEFT;
    }
}
