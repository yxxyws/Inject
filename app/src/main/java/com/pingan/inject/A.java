package com.pingan.inject;

import android.util.Log;

/**
 * Created by yunyang on 2017/3/22.
 */

public final class A {

    final int value;
    A(){
        value = 10;
    }

    public void b(){
        Log.d("hehe","call origin b");
    }

    public void c(){
        b();
        Log.d("hehe","call c");
    }
}
