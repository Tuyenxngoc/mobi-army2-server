package com.teamobi.mobiarmy2.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class AccountDTO {
    private String accountId;
    private boolean isLock;
    private boolean isActive;
}
