package com.teamobi.mobiarmy2.dao;

public interface Dao<T> {

    void save(T t);

    void update(T t);

}