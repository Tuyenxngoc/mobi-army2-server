package com.teamobi.mobiarmy2.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClanMembersResultDTO {
    private final byte page;
    private final List<ClanMemDTO> members;
}