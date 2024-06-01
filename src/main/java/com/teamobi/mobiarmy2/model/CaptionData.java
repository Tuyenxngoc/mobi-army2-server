package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class CaptionData {

    @Getter
    @Setter
    public static class CaptionEntry {
        private byte level;
        private String caption;
    }

    public static final List<CaptionEntry> CAPTION_ENTRIES = new ArrayList<>();

}
