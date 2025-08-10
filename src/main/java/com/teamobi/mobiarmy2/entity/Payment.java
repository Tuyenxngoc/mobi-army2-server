package com.teamobi.mobiarmy2.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private String id;
    private String info;
    private String url;
    private String mssContent;
    private String mssTo;
}
