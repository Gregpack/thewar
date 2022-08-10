package ru.gregpack.thewar.model.entities.composite.units;

import lombok.ToString;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.model.entities.basic.Direction;

@ToString(callSuper = true, includeFieldNames = false)
public class Cavalry extends Unit {

    private static final String propertyName = "cavalry";
    private Direction lastDirection = null;
    private int accelerationLength;

    public Cavalry() {
        super(propertyName);
    }

    public Cavalry(Coordinate position) {
        super(propertyName, position);
    }

    @Override
    protected void doMove(Coordinate oldPosition, Coordinate newPosition, Direction direction) {
        super.doMove(oldPosition, newPosition, direction);
        if (!direction.equals(lastDirection)) {
            lastDirection = direction;
            accelerationLength = 0;
            return;
        }
        accelerationLength++;
    }

    @Override
    protected void afterAttack(Unit attacked, GameState gameState) {
        if (accelerationLength >= 3) {
            Direction toEnemy = attacked.getPosition().sub(this.getPosition()).toDirection();
            if (toEnemy.equals(lastDirection)) {
                double multiplier = (accelerationLength - 2) * 0.1;
                dealDamage(attacked, this.getAttack().getAttackPower() * multiplier);
            }
        }
        accelerationLength = 0;
        lastDirection = null;
    }

}
