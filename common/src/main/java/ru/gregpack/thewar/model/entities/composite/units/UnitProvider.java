package ru.gregpack.thewar.model.entities.composite.units;

@FunctionalInterface
public interface UnitProvider<T extends Unit> {

    T createUnit();

}
