package com.bigdata.electricity.mock.util;

public class RandomOpt<T>{
    T value ;
    int weight;

    public RandomOpt( T value, int weight ){
        this.value=value ;
        this.weight=weight;
    }

    public T getValue() {
        return value;
    }

    public int getWeight() {
        return weight;
    }
}
