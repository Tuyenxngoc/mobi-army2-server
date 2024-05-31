package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuyen
 */
public class PaymentData {

    @Getter
    @Setter
    public static class PaymentEntry {
        private String id;
        private String info;
        private String url;
        private String mssContent;
        private String mssTo;
    }

    public static final Map<String, PaymentEntry> PAYMENT_ENTRY_MAP = new HashMap<>();

}
