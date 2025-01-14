package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.AccountDTO;

/**
 * @author tuyen
 */
public interface IAccountDAO {
    AccountDTO findByUsernameAndPassword(String username, String password);

    boolean existsByAccountIdAndPassword(String accountId, String password);

    void changePassword(String accountId, String newPass);
}
