package com.pingan.inject;

import android.content.Context;

import java.net.URL;

/**
 * Created by yunyang on 2017/3/28.
 */

public class InjectCenter {

    public static void init(Context applicationContext){
        OKHttpInjectHandler.getHandler(20).initOKhttp(applicationContext);
        OKHttpInjectHandler.getHandler(30).initOKhttp(applicationContext);
    }

    public static void recover(Context applicationContext){
        ;
    }

    public void initHttpUrlConnection(){
        Class urlClass = URL.class;
    }
}
