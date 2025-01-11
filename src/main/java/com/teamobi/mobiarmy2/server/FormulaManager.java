package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.model.Formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tuyen
 */
public class FormulaManager {
    public static final Map<Byte, Map<Byte, List<Formula>>> FORMULAS = new HashMap<>();

    /**
     * Adds a FormulaEntry to the FORMULA map. The entry is organized using its MaterialId and CharacterId values.
     *
     * @param entry The FormulaEntry to be added.
     */
    public static void addFormulaEntry(Formula entry) {
        Map<Byte, List<Formula>> innerMap = FORMULAS.computeIfAbsent(entry.getMaterial().getId(), k -> new HashMap<>());
        List<Formula> formulaList = innerMap.computeIfAbsent(entry.getCharacterId(), k -> new ArrayList<>());

        //Add the entry to the list
        formulaList.add(entry);
    }
}
