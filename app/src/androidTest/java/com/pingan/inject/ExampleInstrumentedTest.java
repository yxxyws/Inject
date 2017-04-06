package com.pingan.inject;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import com.android.dx.stock.ProxyBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.pingan.inject", appContext.getPackageName());
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
                    ProxyBuilder.callSuper(proxy,cMethod, args);
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

    @Test
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
    public void testOKHttpV20(){
        ;
    }

    @Test
    public void testOKHttpV26(){
        ;
    }

    public void testOKHttp27(){
        ;
    }
}
