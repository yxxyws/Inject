package com.pingan.inject;

import android.util.Log;

import static org.junit.Assert.assertEquals;

/**
 * Created by yunyang on 2017/3/22.
 */

public class DemoA {
    final int a;
    int value;
    public DemoA(){
        value = 10;
        a = 10;
    }

    public void b(){
        Log.d("hehe","call origin b");
        assertEquals(value, 11);
    }

    public void c(){
        value = 11;
        //b();
        Log.d("hehe","call c");
    }

}

