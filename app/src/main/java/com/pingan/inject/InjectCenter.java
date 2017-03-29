package com.pingan.inject;

import android.content.Context;

import java.lang.reflect.Field;

/**
 * Created by yunyang on 2017/3/28.
 */

public class InjectCenter {
    public static void init(Context applicationContext){
        ProxyFactory.initOKhttp(applicationContext);
    }
}
