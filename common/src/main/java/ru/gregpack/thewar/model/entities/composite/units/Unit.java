package ru.gregpack.thewar.model.entities.composite.units;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gregpack.thewar.model.GameState;
import ru.gregpack.thewar.model.UnitSubscriber;
import ru.gregpack.thewar.model.entities.basic.*;
import ru.gregpack.thewar.model.entities.basic.Armor.ArmorType;
import ru.gregpack.thewar.utils.PropertyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ToString(onlyExplicitlyIncluded = true, includeFieldNames = false)
@EqualsAndHashCode(callSuper = false)
public abstract class Unit extends Entity {
    private static final Logger logger = LogManager.getLogger(Unit.class.getName());

    protected static final String defaultPropertyPrefix = "logic.unit";

    public enum Behaviour {
        DEFAULT,
        BATTLE_EXPECTED,
        BATTLE
    }

    private final Map<Pair<Attack.AttackType, ArmorType>, Double> armorAttackMultiplierMap =
            Map.of(
                    Pair.of(Attack.AttackType.Normal, ArmorType.Light), 1.2,
                    Pair.of(Attack.AttackType.Normal, ArmorType.Normal), 1.0,
                    Pair.of(Attack.AttackType.Normal, ArmorType.Heavy), 0.8,
                    Pair.of(Attack.AttackType.Fire, ArmorType.Light), 1.1,
                    Pair.of(Attack.AttackType.Fire, ArmorType.Normal), 1.0,
                    Pair.of(Attack.AttackType.Fire, ArmorType.Heavy), 0.9,
                    Pair.of(Attack.AttackType.Sharp, ArmorType.Light), 1.4,
                    Pair.of(Attack.AttackType.Sharp, ArmorType.Normal), 0.6,
                    Pair.of(Attack.AttackType.Sharp, ArmorType.Heavy), 0.4
            );

    private final ArrayList<UnitSubscriber> subscribers = new ArrayList<>();
    private final ArrayList<UnitSubscriber> markedForDesubscribe = new ArrayList<>();

    @ToString.Include
    @Getter
    private double healthPoints;
    @Getter
    private final double totalHealthPoints;
    private final int speed;
    private final int battleRange;
    @Getter
    private final int moneyWorth;
    @ToString.Include
    @Getter
    private final Attack attack;
    @ToString.Include
    @Getter
    private final Armor armor;
    @ToString.Include
    @Getter
    private Behaviour currentBehaviour = Behaviour.DEFAULT;
    @Getter
    private Unit behaviourTriggerUnit = null;
    @Setter
    private Unit designatedTarget = null;
    @Setter
    private Direction designatedMovement = null;
    private int _attackCooldown = 0;
    private final int skillCooldown;
    private int _skillCooldown = 0;

    protected Unit(String propertyName) {
        String prefix = defaultPropertyPrefix + "." + propertyName;
        Map<String, String> stats = PropertyUtil.getPropertyMap(prefix);
        this.healthPoints = Double.parseDouble(stats.getOrDefault("health", "1.0"));
        this.totalHealthPoints = healthPoints;
        this.speed = Integer.parseInt(stats.getOrDefault("speed", "1"));
        this.moneyWorth = Integer.parseInt(stats.getOrDefault("moneyworth", "1"));
        this.battleRange = Integer.parseInt(stats.getOrDefault("battlerange", "0"));
        this.attack = new Attack();
        attack.setAttackPower(Integer.parseInt(stats.getOrDefault("attack", "0")));
        attack.setAttackRange(Integer.parseInt(stats.getOrDefault("range", "0")));
        attack.setAttackType(Attack.AttackType.valueOf(stats.getOrDefault("attacktype", "Normal")));
        attack.setAttackCooldown(Integer.parseInt(stats.getOrDefault("attackcooldown", "1")));
        this.armor = new Armor();
        armor.setArmorAmount(Integer.parseInt(stats.getOrDefault("armor", "0")));
        armor.setArmorType(Armor.ArmorType.valueOf(stats.getOrDefault("armortype", "Normal")));
        this.skillCooldown = Integer.parseInt(stats.getOrDefault("skillcooldown", "0"));
    }

    protected Unit(String propertyName, Coordinate position) {
        this(propertyName);
        this.setPosition(position);
    }

    protected void afterAttack(Unit attacked, GameState gameState) {
    }

    public void callSkill(SkillActivator skillActivator) {
        if (_skillCooldown > 0) {
            return;
        }
        useSkill(skillActivator);
        _skillCooldown = skillCooldown;
        for (UnitSubscriber s : subscribers) {
            s.onSkill(this, skillActivator);
        }
    }

    protected void useSkill(SkillActivator skillActivator) {}

    private void attackUnit(Unit enemy, GameState gameState) {
        if (!enemy.isInRange(this, attack.getAttackRange()) &&
                !enemy.getPosition().areInNextCells(this.getPosition())) {
            return;
        }
        if (_attackCooldown != 0) {
            return;
        }
        dealDamage(enemy, attack.getAttackPower());
        afterAttack(enemy, gameState);
        _attackCooldown = attack.getAttackCooldown();
    }

    protected final void dealDamage(Unit enemy, double damage) {
        double armorAttackMultiplier = armorAttackMultiplierMap.getOrDefault(
                Pair.of(attack.getAttackType(), enemy.getArmor().getArmorType()),
                1.0
        );
        double attackPower = damage * armorAttackMultiplier - enemy.armor.getArmorAmount();
        enemy.setHealthPoints(enemy.getHealthPoints() - attackPower);
        if (logger.isDebugEnabled()) {
            logger.debug("Unit {} attacking {} for {}!", this, enemy, attackPower);
        }
        if (!enemy.isAlive()) {
            if (enemy.equals(behaviourTriggerUnit)) {
                behaviourTriggerUnit = null;
                currentBehaviour = Behaviour.DEFAULT;
            }
        }
        for (UnitSubscriber s : subscribers) {
            s.onAttack(this, enemy);
        }
    }

    public void setHealthPoints(double healthPoints) {
        this.healthPoints = healthPoints < 0 ? 0 : healthPoints;
    }

    private boolean isAlive() {
        return healthPoints > 0;
    }

    private void move(List<Direction> defaultDirection) {
        for (int i = 0; i < speed && i < defaultDirection.size(); i++) {
            singleCellMove(defaultDirection.get(i));
        }
        designatedMovement = null;
    }
    private void move(Direction defaultDirection) {
        Direction moveDirection = designatedMovement == null ? defaultDirection : designatedMovement;
        for (int i = 0; i < speed; i++) {
            singleCellMove(moveDirection);
        }
        designatedMovement = null;
    }

    private void singleCellMove(Direction direction) {
        Coordinate oldPosition = this.getPosition();
        Coordinate newPosition = this.getPosition().add(direction);
        //this.setPosition(this.getPosition().add(direction));
        doMove(oldPosition, newPosition, direction);
    }

    protected void doMove(Coordinate oldPosition, Coordinate newPosition, Direction direction) {
        for (UnitSubscriber s : subscribers) {
            s.onMove(this, oldPosition, newPosition);
        }
        for (UnitSubscriber unitSubscriber : markedForDesubscribe) {
            subscribers.remove(unitSubscriber);
        }
        markedForDesubscribe.clear();

    }

    private boolean isInManhattanRange(Unit unit, int range) {
        if (unit == null) {
            return false;
        }
        return this.getPosition().isInManhattanRange(unit.getPosition(), range);
    }

    private boolean isInRange(Unit unit, int range) {
        if (unit == null) {
            return false;
        }
        return this.getPosition().isInRange(unit.getPosition(), range);
    }

    public void act(GameState gameState) {
        cooldowns();
        behaviourState(gameState);
        switch (currentBehaviour) {
            case DEFAULT:
            case BATTLE_EXPECTED:
                Coordinate oldPosition = getPosition();
                move(gameState.getUnitService().getUnitDefaultDirection(getId()));
                if (oldPosition.equals(getPosition())) {
                    move((int) (Math.random() * 2) == 0 ? Direction.DOWN : Direction.UP);
                }
                break;
            case BATTLE: {
                Unit enemyTarget;
                if (getDesignatedTarget() != null) {
                    enemyTarget = getDesignatedTarget();
                } else {
                    enemyTarget = gameState.getUnitService().findClosestEnemyUnitInRange(this, battleRange);
                }
                if (enemyTarget == null) {
                    currentBehaviour = Behaviour.DEFAULT;
                    move(gameState.getUnitService().getUnitDefaultDirection(getId()));
                    return;
                }
                if (this.isInRange(enemyTarget, this.attack.getAttackRange())) {
                    designatedTarget = enemyTarget;
                    this.attackUnit(enemyTarget, gameState);
                    return;
                }
                List<Direction> moveToTargetDirection = gameState.getUnitService().nextMoveToTarget(this, enemyTarget.getPosition());
                if (moveToTargetDirection != null) {
                    move(moveToTargetDirection);
                }
            }
        }
    }

    private int getBattlePreparationRange() {
        return 2 * battleRange;
    }

    private void behaviourState(GameState gameState) {
        switch (currentBehaviour) {
            case DEFAULT: {
                // enemy in battle range
                Unit unit = gameState.getUnitService().findClosestEnemyUnitInRange(this, battleRange);
                if (unit != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unit {} now in battle with {}!", this, unit);
                    }
                    behaviourTriggerUnit = unit;
                    currentBehaviour = Behaviour.BATTLE;
                    return;
                }
                // enemy in prep range
                unit = gameState.getUnitService().findClosestEnemyUnitInRange(this, getBattlePreparationRange());
                if (unit != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unit {} prepares for battle with {}!", this, unit);
                    }
                    behaviourTriggerUnit = unit;
                    currentBehaviour = Behaviour.BATTLE_EXPECTED;
                }
                return;
            }
            case BATTLE_EXPECTED: {
                // enemy dead OR enemy got out of prep range
                if (!behaviourTriggerUnit.isAlive() || !isInManhattanRange(behaviourTriggerUnit, getBattlePreparationRange())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unit {} no longer prepares to battle with {}! Looking for new target",
                                this, behaviourTriggerUnit);
                    }
                    currentBehaviour = Behaviour.DEFAULT;
                    behaviourTriggerUnit = null;
                    behaviourState(gameState);
                    return;
                }
                // enemy alive AND entered battle range
                if (behaviourTriggerUnit.isAlive() && isInManhattanRange(behaviourTriggerUnit, battleRange)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unit {} now in battle with {}!", this, behaviourTriggerUnit);
                    }
                    currentBehaviour = Behaviour.BATTLE;
                }
                return;
            }
            case BATTLE: {
                // enemy dead OR enemy got out of battle range
                if (!behaviourTriggerUnit.isAlive() || (!isInManhattanRange(behaviourTriggerUnit, battleRange))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unit {} no longer in battle with {}! Looking for new target",
                                this, behaviourTriggerUnit);
                    }
                    currentBehaviour = Behaviour.DEFAULT;
                    behaviourTriggerUnit = null;
                    behaviourState(gameState);
                }
            }
        }
    }

    public void subscribe(UnitSubscriber unitSubscriber) {
        this.subscribers.add(unitSubscriber);
    }

    public void desubscribe(UnitSubscriber unitSubscriber) {
        markedForDesubscribe.add(unitSubscriber);
    }

    private void cooldowns() {
        if (_attackCooldown > 0) {
            _attackCooldown--;
        }
        if (_skillCooldown > 0) {
            _skillCooldown--;
        }
    }

    private Unit getDesignatedTarget() {
        if (designatedTarget != null && !designatedTarget.isAlive()) {
            designatedTarget = null;
        }
        return designatedTarget;
    }
}
