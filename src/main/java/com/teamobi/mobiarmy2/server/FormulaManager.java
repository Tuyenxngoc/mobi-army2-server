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

    public static void addFormula(Formula formula) {
        Map<Byte, List<Formula>> innerMap = FORMULAS.computeIfAbsent(formula.getMaterial().getId(), k -> new HashMap<>());
        List<Formula> formulaList = innerMap.computeIfAbsent(formula.getCharacterId(), k -> new ArrayList<>());

        formulaList.add(formula);
    }
}
