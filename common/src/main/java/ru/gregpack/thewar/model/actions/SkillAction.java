package ru.gregpack.thewar.model.actions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillAction extends Action {
    private int fromX;
    private int fromY;
    private int toX;
    private int toY;
}
