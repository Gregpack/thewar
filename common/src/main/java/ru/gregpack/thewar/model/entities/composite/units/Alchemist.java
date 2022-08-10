package ru.gregpack.thewar.model.entities.composite.units;

import lombok.ToString;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.utils.PropertyUtil;

import java.util.Map;

@ToString(callSuper = true, includeFieldNames = false)
public class Alchemist extends Unit {

    private static final String propertyName = "alchemist";
    private final double splashDamage;

    public Alchemist() {
        super(propertyName);
        String prefix = defaultPropertyPrefix + "." + propertyName;
        Map<String, String> stats = PropertyUtil.getPropertyMap(prefix);
        this.splashDamage = Double.parseDouble(stats.getOrDefault("splashDamage", "0.3"));
    }

    public Alchemist(Coordinate position) {
        super(propertyName, position);
        String prefix = defaultPropertyPrefix + "." + propertyName;
        Map<String, String> stats = PropertyUtil.getPropertyMap(prefix);
        this.splashDamage = Double.parseDouble(stats.getOrDefault("splashDamage", "0.3"));
    }

    @Override
    protected void afterAttack(Unit attacked, GameState gameState) {
        gameState.getUnitService().getSurroundingUnits(attacked).forEach(enemy ->
                dealDamage(enemy, this.getAttack().getAttackPower() * splashDamage)
        );
    }
}
