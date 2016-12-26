package com.ndhunju.dailyjournal.database;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dhunju on 9/18/2015.
 * DAO define generic CRUD operation
 */
interface IGenericDAO<T, ID extends Serializable> {

    //CRUD
    long create(T t);
    T find(ID id);
    List<T> findAll();
    long update(T t);
    void delete(T t);
}
