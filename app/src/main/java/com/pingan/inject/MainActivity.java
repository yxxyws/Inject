package com.pingan.inject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.android.dx.stock.ProxyBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.internal.Internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by yunyang on 2017/3/22.
 */

public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //ProxyBuilder<A> builder;

                InvocationHandler handler = new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("b")) {
                            // Chosen by fair dice roll, guaranteed to be random.
                            System.out.println("inject");
                        }
                        return ProxyBuilder.callSuper(proxy, method, args);
                    }
                };
                Internal internalProxy = null;
                try {
                    internalProxy = ProxyBuilder.forClass(Internal.class)
                            .dexCache(MainActivity.this.getDir("dx", Context.MODE_PRIVATE))
                            .handler(handler)
                            .build();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //internalProxy.b();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OkHttpClient client = new OkHttpClient();


                NetUtils.testOKHttpExecute();
            }
        });
    }
}
