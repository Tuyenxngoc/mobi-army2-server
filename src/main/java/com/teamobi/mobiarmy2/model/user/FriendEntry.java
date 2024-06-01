package com.teamobi.mobiarmy2.model.user;

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
    public int id;
    public String name;
    public int xu;
    public byte nvUsed;
    public short clanId;
    public byte online;
    public byte level;
    public byte levelPt;
    public short[] data;
}
