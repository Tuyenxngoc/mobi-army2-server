package com.teamobi.mobiarmy2.dao;

/**
 * @author tuyen
 */
public interface Dao<T> {

    void save(T t);

    void update(T t);

}