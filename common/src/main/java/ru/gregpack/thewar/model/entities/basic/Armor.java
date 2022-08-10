package ru.gregpack.thewar.model.entities.basic;

import lombok.*;

@Getter
@Setter
@ToString(includeFieldNames = false)
@EqualsAndHashCode
public class Armor {
    public enum ArmorType {
        Light,
        Normal,
        Heavy
    }
    public Armor() {}
    private int armorAmount;
    private ArmorType armorType;
}
