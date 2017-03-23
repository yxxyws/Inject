package com.pingan.inject;

import android.util.Log;

/**
 * Created by yunyang on 2017/3/22.
 */

public final class A {

    public void b(){
        Log.d("hehe","call origin b");
    }

    public void c(){
        b();
        Log.d("hehe","call c");
    }
}
