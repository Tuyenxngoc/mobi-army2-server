package com.teamobi.mobiarmy2.dao;

import java.util.List;

/**
 * @author tuyen
 */
public interface DAO<T> {

    void save(T t);

    void update(T t);

    T findById(int id);

    void deleteById(int id);

    List<T> findAll();

}