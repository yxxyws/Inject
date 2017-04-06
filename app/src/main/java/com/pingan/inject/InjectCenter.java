package com.pingan.inject;

import android.content.Context;

import java.lang.reflect.Field;
import java.net.URL;

/**
 * Created by yunyang on 2017/3/28.
 */

public class InjectCenter {
    public static void init(Context applicationContext){
        ProxyFactory.initOKhttp(applicationContext);
    }

    public static void recover(Context applicationContext){
        if(ProxyFactory.initSuccess) {
        }
    }

    public void initHttpUrlConnection(){
        Class urlClass = URL.class;
    }
}
