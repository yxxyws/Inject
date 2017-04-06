package com.pingan.inject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InjectCenter.init(MainActivity.this.getApplicationContext());

                EditText text = (EditText)findViewById(R.id.url_text);
                NetTestUtils.testOKHttpV2Execute(text.getText().toString());
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //OkHttpClient oldClient = new OkHttpClient();
                //Internal.instance = new ProxyInternal(MainActivity.this.getApplication(), Internal.instance);
                InjectCenter.init(MainActivity.this.getApplicationContext());

                EditText text = (EditText)findViewById(R.id.url_text);
                NetTestUtils.testOKHttpV3Execute(text.getText().toString());
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
                        NetTestUtils.postHttpUrlConnectionRequest(url);
                    }
                });
                t.start();
            }
        });

    }
}
