package com.pingan.inject;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.deps.guava.util.concurrent.ThreadFactoryBuilder;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.android.dx.stock.ProxyBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.internal.tls.SslClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void testInjectCenter() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        InjectCenter.init(appContext);
    }

    //@Test
    public void testProxyBuilder() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("b")) {
                    System.out.println("inject");
                    Method cMethod = DemoA.class.getMethod("c");
                    ProxyBuilder.callSuper(proxy, cMethod, args);
                }

                return ProxyBuilder.callSuper(proxy, method, args);
            }
        };
        DemoA a = null;
        try {
            a = ProxyBuilder.forClass(DemoA.class)
                    .dexCache(appContext.getDir("dx", Context.MODE_PRIVATE))
                    .handler(handler)
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        a.b();
    }

    //@Test
    public void testModiFinal() throws Exception {
        DemoA a = new DemoA();
        Field f = null;
        try {
            f = DemoA.class.getDeclaredField("a");
            f.setAccessible(true);
            System.out.println("value:" + f.getInt(a));
            f.setInt(a, 2);
            assertEquals(2, f.getInt(a));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOKHttpV20Http() throws Exception {
        Log.d("TimeDevice", "testOKHttpV20Http");
        Context appContext = InstrumentationRegistry.getTargetContext();
        OKHttpInjectHandler.getHandler(20).initOKhttp(appContext);

        String url = "http://www.baidu.com";
        NetTestUtils.testOKHttpV2Execute(url);
    }

    @Test
    public void testOKHttpV20Https() throws Exception {
        Log.d("TimeDevice", "testOKHttpV20Https");
        Context appContext = InstrumentationRegistry.getTargetContext();
        OKHttpInjectHandler.getHandler(20).initOKhttp(appContext);

        String url = "https://www.baidu.com";
        NetTestUtils.testOKHttpV2Execute(url);
    }

    @Test
    public void testOKHttpV30Http() throws Exception {
        Log.d("TimeDevice", "testOKHttpV30Http");
        Context appContext = InstrumentationRegistry.getTargetContext();
        OKHttpInjectHandler.getHandler(30).initOKhttp(appContext);

        String url = "http://pic1.win4000.com/pic/0/50/5ffd1286284.jpg";
        //MockWebServer server = new MockWebServer();
        //MockResponse response = new MockResponse().setResponseCode(200);
        //server.enqueue(response);
        //String url = "https://"+server.getHostName()+":"+server.getPort();

        NetTestUtils.testOKHttpV3Execute(url);
    }

    @Test
    public void testOKHttp30Https() throws Exception {
        Log.d("TimeDevice", "testOKHttpV30Https");
        Context appContext = InstrumentationRegistry.getTargetContext();
        OKHttpInjectHandler.getHandler(30).initOKhttp(appContext);

        String url = "https://www.baidu.com";
        NetTestUtils.testOKHttpV3Execute(url);
    }

    @Test
    public void testOKHttp30HttpTmall() throws Exception {
        Log.d("TimeDevice", "testOKHttpV30Https");
        Context appContext = InstrumentationRegistry.getTargetContext();
        OKHttpInjectHandler.getHandler(30).initOKhttp(appContext);
        Thread.sleep(1000);

        Thread tempt = new Thread(new Runnable() {
            @Override
            public void run() {
                //天猫用的是http2.0的协议
                String url = "https://www.tmall.com";
                try {
                    NetTestUtils.testOKHttpV3Execute(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        tempt.start();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://www.tmall.com/wow/brand/act/fashion?spm=875.7931836/B.2016004.4.QhqSBV&acm=lb-zebra-148799-667861.1003.4.1266918&scm=1003.4.lb-zebra-148799-667861.OTHER_14561841536074_1266918";
                try {
                    NetTestUtils.testOKHttpV3Execute(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

    @Test
    public void testHttpConnection() throws Exception {
        Log.d("TimeDevice", "testHttpConnection");
        Context appContext = InstrumentationRegistry.getTargetContext();
        HttpUrlConnectionInjectHandler.injectUrlFactory(appContext);
        //String url = "http://www.baidu.com";
        String url = "http://coolaf.com/tool/params?g=gtest&g2=gtest2";
        NetTestUtils.postHttpUrlConnectionRequest(url);
    }

    @Test
    public void testHttpUrlConnectionHttps() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        HttpUrlConnectionInjectHandler.injectUrlFactory(appContext);

        MockWebServer server = new MockWebServer();
//        server.enqueue(new MockResponse()
//                .setBody("Blocked!"));
//        final URL blockedURL = server.url("/").url();

        SslClient contextBuilder = SslClient.localhost();
        server.useHttps(contextBuilder.socketFactory, false);

        server.enqueue(new MockResponse()
                .setResponseCode(302)
                //.addHeader("Location: " + blockedURL)
                .setBody("This page has moved"));
//        URL destination = server.url("/").url();
//        try {
//            HttpsURLConnection httpsConnection = (HttpsURLConnection) destination.openConnection();
//            httpsConnection.setSSLSocketFactory(contextBuilder.socketFactory);
//            httpsConnection.getInputStream();
//        } catch (IOException expected) {
//            expected.printStackTrace();
//            fail("Connection was fail");
//        }
        NetTestUtils.postHttpsUrlConnectionRequest(server.url("/").url(), contextBuilder.socketFactory);
    }

    @Test
    public void testZhiHu() {
        Log.d("TimeDevice", "testOKHttpV30Https");
        Context appContext = InstrumentationRegistry.getTargetContext();
        OKHttpInjectHandler.getHandler(30).initOKhttp(appContext);

        String url = "https://www.zhihu.com/question/34650457";
        try {
            NetTestUtils.testOKHttpV3Execute(url);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
