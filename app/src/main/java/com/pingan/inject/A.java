package com.pingan.inject;

import android.util.Log;

/**
 * Created by yunyang on 2017/3/22.
 */

public class A {

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
        Object a = new Object();
        AE ae = new AE(a);
        if(a == ae) {
            System.out.println("true");
        }
    }

    private class AE{
        Object b;

        AE(Object b){
            this.b = b;
        }
        @Override
        public boolean equals(Object obj) {
            return b.equals(obj);
        }

        @Override
        public int hashCode() {
            return b.hashCode();
        }
    }
}

