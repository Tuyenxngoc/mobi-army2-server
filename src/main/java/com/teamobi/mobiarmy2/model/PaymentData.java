package com.teamobi.mobiarmy2.model;

import java.util.ArrayList;

/**
 * @author tuyen
 */
public class PaymentData {

    public static class Payment {
        public String id;
        public String info;
        public String url;
        public String mssContent;
        public String mssTo;
    }

    public static ArrayList<Payment> payments = new ArrayList<>();

}
