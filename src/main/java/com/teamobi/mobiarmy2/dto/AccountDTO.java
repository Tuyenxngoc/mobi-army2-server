package com.teamobi.mobiarmy2.dto;

import com.teamobi.mobiarmy2.constant.AccountStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDTO {
    private String accountId;
    private AccountStatus status;
}
