package com.pingan.inject;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.FormBody;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Created by yunyang on 2017/3/23.
 */

public class NetTestUtils {

    public static void testOKHttpV3Execute(String url) throws Exception {
        okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder().readTimeout(1000, TimeUnit.SECONDS)
                .writeTimeout(100000, TimeUnit.SECONDS)
                .connectTimeout(100000, TimeUnit.SECONDS);
        okhttp3.OkHttpClient client = builder.build();

        //okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.RequestBody body = new FormBody.Builder().add("p", "ptest").add("p", "test2").build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).put(body).build();

        //okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        okhttp3.Response response = client.newCall(request).execute();
        assertEquals(response.code(), 200);
//                enqueue(new okhttp3.Callback() {
////            @Override
////            public void onFailure(okhttp3.Request request, IOException e) {
////                e.printStackTrace();
////            }
////
////            @Override
////            public void onResponse(Response response) throws IOException {
////                if(response.isSuccessful()){
////                    String msg = response.message();
////                }
////            }
//            @Override
//            public void onFailure(okhttp3.Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
//                if(response.isSuccessful()){
//                    String msg = response.message();
//                }
//            }
//        });

    }

    public static void testOKHttpV2Execute(String url) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).execute();
    }

    public static String postHttpUrlConnectionRequest(String urlString) throws Exception {
        HttpURLConnection httpURLConnection = null;
        String message = "";
        try {
            URL url = new URL(urlString);

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(10 * 1000);
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            String body = "p=" + URLEncoder.encode("ptest") + "&p=" + URLEncoder.encode("test2");
            httpURLConnection.setRequestProperty("Cache-Control", "max-age=0");
            // 设置文件类型
            httpURLConnection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            //设置conn可以写请求的内容
            httpURLConnection.setDoOutput(true);
            httpURLConnection.getOutputStream().write(body.getBytes());
            InputStream inputStream = httpURLConnection.getInputStream();
            String llr = httpURLConnection.getRequestProperty("Content-Type");

            if (httpURLConnection.getResponseCode() == 200) {
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                message = sb.toString();
            }
            return message;
        } catch (MalformedURLException e) {
            fail();
            e.printStackTrace();
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            return message;
        }
    }

    public static String postHttpsUrlConnectionRequest(URL url, SSLSocketFactory factory) {
        HttpsURLConnection httpURLConnection = null;
        String message = "";
        try {

            httpURLConnection = (HttpsURLConnection) url.openConnection();
            if (factory != null) {
                httpURLConnection.setSSLSocketFactory(factory);
            }
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(10 * 1000);
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            String body = "p=" + URLEncoder.encode("ptest") + "&p=" + URLEncoder.encode("test2");
            httpURLConnection.setRequestProperty("Cache-Control", "max-age=0");
            // 设置文件类型
            httpURLConnection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            //设置conn可以写请求的内容
            httpURLConnection.setDoOutput(true);
            httpURLConnection.getOutputStream().write(body.getBytes());
            InputStream inputStream = httpURLConnection.getInputStream();
            String llr = httpURLConnection.getRequestProperty("Content-Type");

            if (httpURLConnection.getResponseCode() == 200) {
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                message = sb.toString();
            }
            return message;
        } catch (MalformedURLException e) {
            fail();
            e.printStackTrace();
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            return message;
        }
    }


}
