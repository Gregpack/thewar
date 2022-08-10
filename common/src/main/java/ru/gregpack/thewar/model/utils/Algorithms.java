package ru.gregpack.thewar.model.utils;

import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.Direction;
import ru.gregpack.thewar.model.entities.basic.FieldCell;

import java.util.*;

public class Algorithms {

    public static List<Direction> aStarAlgorithm(FieldCell[][] gameField, Coordinate from, Coordinate to) {
        final Map<Coordinate, Integer> pathFromStartCost = new HashMap<>();
        final Map<Coordinate, Direction> parentMap = new HashMap<>();
        Set<Coordinate> checkedCoords = new HashSet<>();
        PriorityQueue<Coordinate> coordsToCheck = new PriorityQueue<>((o1, o2) -> {
           int f1 = o1.manhattanRange(to) + pathFromStartCost.get(o1);
           int f2 = o2.manhattanRange(to) + pathFromStartCost.get(o2);
           return Integer.compare(f1, f2);
        });
        pathFromStartCost.put(from, 0);
        coordsToCheck.add(from);
        int fieldHeight = gameField.length;
        int fieldLength = gameField[0].length;
        while (!coordsToCheck.isEmpty()) {
            Coordinate current = coordsToCheck.poll();
            if (current.equals(to)) {
                Direction nextInPath = null;
                List<Direction> pathFromStart = new ArrayList<>();
                while (!current.equals(from)) {
                     nextInPath = parentMap.get(current);
                     pathFromStart.add(nextInPath.reverse());
                     current = current.add(nextInPath);
                }
                Collections.reverse(pathFromStart);
                return nextInPath == null ? null : pathFromStart;
            }
            checkedCoords.add(current);
            int currentCost = pathFromStartCost.get(current);
            for (Direction direction: Direction.getDirections()) {
                Coordinate next = current.add(direction);
                if ((isCellOutsideBounds(next, fieldLength, fieldHeight) || gameField[next.getY()][next.getX()].isOccupied())
                        && !next.equals(to)) {
                    continue;
                }
                int tentativeScore = currentCost + 1;
                if (!checkedCoords.contains(next) || tentativeScore < pathFromStartCost.getOrDefault(next, Integer.MAX_VALUE)) {
                    parentMap.put(next, direction.reverse());
                    pathFromStartCost.put(next, tentativeScore);
                    if (!coordsToCheck.contains(next)) {
                        coordsToCheck.add(next);
                    }
                }
            }
        }
        return null;
    }

    // very stupid algorithm
    public static Direction dumbPathingAlgorithm(FieldCell[][] gameField, Coordinate from, Coordinate to) {
        Coordinate difference = to.sub(from);
        if (difference.getX() != 0) {
            Direction toMove = difference.getX() > 0 ? Direction.RIGHT : Direction.LEFT;
            Coordinate nextCell = from.add(toMove);
            if (!gameField[nextCell.getY()][nextCell.getX()].isOccupied()) {
                return toMove;
            }
        }
        return difference.getY() >= 0 ? Direction.UP : Direction.DOWN;
    }

    public static boolean isCellOutsideBounds(Coordinate cell, int length, int height) {
        return cell.getX() < 0 || cell.getY() < 0 || cell.getX() >= length || cell.getY() >= height;
    }

}
