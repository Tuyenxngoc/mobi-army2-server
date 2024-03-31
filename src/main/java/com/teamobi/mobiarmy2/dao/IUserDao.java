package com.teamobi.mobiarmy2.dao;

import com.teamobi.mobiarmy2.model.User;

public interface IUserDao {

    User findByUsernameAndPassword(String username, String password);

}
