package com.teamobi.mobiarmy2.model.entry.user;

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
public class FriendEntry {
    private int id;
    private String name;
    private int xu;
    private byte nvUsed;
    private short clanId;
    private byte online;
    private byte level;
    private byte levelPt;
    private short[] data;
}
