package com.teamobi.mobiarmy2.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class ClanInfoDTO extends ClanDTO {
    private int exp;
    private int xpUpLevel;
    private String createdDate;
    private List<ClanItemDTO> items;
}