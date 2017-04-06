package com.pingan.inject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

import com.android.dx.command.Main;
import com.android.dx.stock.ProxyBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

//import com.squareup.okhttp.OkHttpClient;

/**
 * Created by yunyang on 2017/3/22.
 */

public class MainActivity extends Activity {
    A a = new A();
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
                            System.out.println("inject");
                        }
                        return ProxyBuilder.callSuper(proxy, method, args);
                    }
                };
                A a = null;
                try {
                    a = ProxyBuilder.forClass(A.class)
                            .dexCache(MainActivity.this.getDir("dx", Context.MODE_PRIVATE))
                            .handler(handler)
                            .build();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                a.c();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InjectCenter.init(MainActivity.this.getApplicationContext());

                EditText text = (EditText)findViewById(R.id.url_text);
                NetUtils.testOKHttpV2Execute(text.getText().toString());
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Field f = null;
                try {
                    f = A.class.getDeclaredField("value");
                    f.setAccessible(true);
                    System.out.println("value:" + f.getInt(a));
                    f.setInt(a, 2);
                    System.out.println("value:" + f.getInt(a));
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //OkHttpClient oldClient = new OkHttpClient();
                //Internal.instance = new ProxyInternal(MainActivity.this.getApplication(), Internal.instance);
                InjectCenter.init(MainActivity.this.getApplicationContext());

                EditText text = (EditText)findViewById(R.id.url_text);
                NetUtils.testOKHttpV3Execute(text.getText().toString());
            }
        });

        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text = (EditText)findViewById(R.id.url_text);
                final String url = text.getText().toString();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpUrlConnectionProxyFactory.injectUrlFactory(MainActivity.this.getApplicationContext());

                        try {
                            new URL("http://www.pingan.com");
                            new URL("https://www.pingan.com");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        NetUtils.postHttpUrlConnectionRequest(url);
                    }
                });
                t.start();
            }
        });

    }
}
