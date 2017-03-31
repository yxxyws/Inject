package com.pingan.inject;

import com.squareup.okhttp.*;

import java.io.IOException;



/**
 * Created by yunyang on 2017/3/23.
 */

public class NetUtils {

    public static void testOKHttpV3Execute(String url){
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        //Internal.instance = new ProxyInternalV2(Internal.instance);
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
//            @Override
//            public void onFailure(okhttp3.Request request, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Response response) throws IOException {
//                if(response.isSuccessful()){
//                    String msg = response.message();
//                }
//            }
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if(response.isSuccessful()){
                    String msg = response.message();
                }
            }
        });

    }

    public static void testOKHttpV2Execute(String url){
        OkHttpClient client = new OkHttpClient();
        //Internal.instance = new ProxyInternalV2(Internal.instance);
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()){
                    String msg = response.message();
                }
            }

        });

    }

    public void testHttpClientPost(){

    }
}
