package com.pingan.inject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by yunyang on 2017/3/23.
 */

public class NetUtils {

    public static void testOKHttpExecute(){
        OkHttpClient client = new OkHttpClient();
        String url = "https://www.baidu.com";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    String msg = response.message();
                }
            }
        });

    }

    public void testHttpClientPost(){

    }
}
