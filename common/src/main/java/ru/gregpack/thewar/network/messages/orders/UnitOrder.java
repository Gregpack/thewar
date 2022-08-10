package ru.gregpack.thewar.network.messages.orders;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonSubTypes({
        @JsonSubTypes.Type(value = AttackOrder.class, name = "AttackOrder"),
        @JsonSubTypes.Type(value = MoveOrder.class, name = "MoveOrder"),
        @JsonSubTypes.Type(value = SkillOrder.class, name = "SkillOrder"),
        @JsonSubTypes.Type(value = StayStillOrder.class, name = "StayStillOrder")
}
)
@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, property = "type")
@JsonTypeName("UnitOrder")
public abstract class UnitOrder extends Order {
    @Getter
    private int unitId;

}
