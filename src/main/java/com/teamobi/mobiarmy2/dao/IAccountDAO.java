package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.dto.AccountDTO;

public interface IAccountDAO {
    AccountDTO findByUsernameAndPassword(String username, String password);
}
