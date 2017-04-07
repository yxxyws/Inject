package com.pingan.inject;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

//import com.squareup.okhttp.OkHttpClient;

/**
 * Created by yunyang on 2017/3/22.
 */

public class TestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                InjectCenter.init(TestActivity.this.getApplicationContext());
//
//                EditText text = (EditText)findViewById(R.id.url_text);
//                NetTestUtils.testOKHttpV2Execute(text.getText().toString());
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                InjectCenter.init(TestActivity.this.getApplicationContext());
//
//                EditText text = (EditText)findViewById(R.id.url_text);
//                NetTestUtils.testOKHttpV3Execute(text.getText().toString());
            }
        });

        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                EditText text = (EditText)findViewById(R.id.url_text);
//                final String url = text.getText().toString();
//                Thread t = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        HttpUrlConnectionInjectHandler.injectUrlFactory(TestActivity.this.getApplicationContext());
//
//                        NetTestUtils.postHttpUrlConnectionRequest(url);
//                    }
//                });
//                t.start();
            }
        });

    }
}
