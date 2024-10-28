package com.teamobi.mobiarmy2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LevelXpRequiredEntry {
    private short level;
    private int xp;
}