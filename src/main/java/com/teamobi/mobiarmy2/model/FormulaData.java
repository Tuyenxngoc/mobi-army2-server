package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.entry.FormulaEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tuyen
 */
public class FormulaData {
    public static final Map<Byte, List<FormulaEntry>> FORMULA = new HashMap<>();

    public static void addFormulaEntry(FormulaEntry entry) {
        List<FormulaEntry> formulaEntries = FORMULA.computeIfAbsent(entry.getMaterialId(), k -> new ArrayList<>());
        formulaEntries.add(entry);
    }
}
