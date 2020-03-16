package com.bigdata.electricity.mock.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomOptionGroup<T> {

    int totalWeight=0;

    List<RandomOpt> optList=new ArrayList();

    public   RandomOptionGroup(RandomOpt<T>... opts) {
        for (RandomOpt opt : opts) {
            totalWeight+=opt.getWeight();
            for (int i = 0; i <opt.getWeight() ; i++) {
                optList.add(opt);
            }

        }
    }

    public RandomOpt<T> getRandomOpt() {
        int i = new Random().nextInt(totalWeight);
        return optList.get(i);
    }


    public static void main(String[] args) {
        RandomOpt[] opts= {new RandomOpt("zhang3",20),new RandomOpt("li4",30),new RandomOpt("wang5",50)};
        RandomOptionGroup randomOptionGroup = new RandomOptionGroup(opts);
        for (int i = 0; i <10 ; i++) {
            System.out.println(randomOptionGroup.getRandomOpt().getValue());
        }

    }





}
