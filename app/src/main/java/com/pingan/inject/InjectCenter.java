package com.pingan.inject;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by yunyang on 2017/3/28.
 */

public class InjectCenter {

    /**
     * 注入类的初始化方法
     *
     * @param applicationContext 这里必须用application，因为会用静态存起来
     */
    public static void init(Context applicationContext) {
        try {
            InputStream stream = applicationContext.getAssets().open("config.xml");
            ConfigManager manager = new ConfigManager();
            manager.readConfig(stream);
            FunctionRecorder.getInstance().setConfigList(manager.recordConfigList);
            OKHttpInjectHandler.getHandler(20).initOKhttp(applicationContext);
            OKHttpInjectHandler.getHandler(30).initOKhttp(applicationContext);
            HttpUrlConnectionInjectHandler.injectUrlFactory(applicationContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recover(Context applicationContext) {
        ;
    }

    public void initHttpUrlConnection() {
        Class urlClass = URL.class;
    }
}
