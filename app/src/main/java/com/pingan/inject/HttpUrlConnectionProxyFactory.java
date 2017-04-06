package com.pingan.inject;

import android.content.Context;

import com.android.dx.stock.ProxyBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Hashtable;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by yunyang on 2017/4/1.
 */

public class HttpUrlConnectionProxyFactory {
    static Context appContext;
    static Field streamHandlerField;

    public static void injectUrlFactory(Context context) {
        TimeDevice.getInstance();
        if (streamHandlerField == null) {
            try {
                streamHandlerField = URL.class.getDeclaredField("streamHandler");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            streamHandlerField.setAccessible(true);
        }
        if (streamHandlerField == null)
            return;
        if (appContext == null) {
            appContext = context;
        }
        Field handlersField = null;
        Field[] fields = URL.class.getDeclaredFields();
        try {
            for (Field f : fields) {
                if (f.getType() == Hashtable.class) {
                    handlersField = f;
                    handlersField.setAccessible(true);
                    Object handlersObject = handlersField.get(URL.class);
                    if (!handlersObject.getClass().getName().contains("Proxy"))
                        handlersField.set(null, getHashTabelProxy(handlersObject));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object getHashTabelProxy(Object target) throws IOException {
        HashtableInvocation handler = new HashtableInvocation(target);
        Object result = ProxyBuilder.forClass(Hashtable.class)
                .dexCache(appContext.getDir("dx", Context.MODE_PRIVATE))
                .handler(handler)
                .build();
        return result;
    }

    private static class HashtableInvocation implements InvocationHandler {
        Object table;

        HashtableInvocation(Object table) {
            this.table = table;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName() == "put" && args.length == 2 && ("https".equals(args[0]) || "http".equals(args[0]))) {
                Object streamHandler = args[1];
                if (streamHandler != null && streamHandler instanceof URLStreamHandler
                        && !ProxyBuilder.isProxyClass(streamHandler.getClass())) {
                    URLStreamHandlerInvocation handler = new URLStreamHandlerInvocation(streamHandler);
                    Object result = ProxyBuilder.forClass(URLStreamHandler.class)
                            .dexCache(appContext.getDir("dx", Context.MODE_PRIVATE))
                            .handler(handler)
                            .build();
                    args[1] = result;
                }
            }
            method.setAccessible(true);
            Object result = method.invoke(table, args);
            return result;
        }
    }

    private static class URLStreamHandlerInvocation implements InvocationHandler {
        Object rawURLStreamHandler;

        URLStreamHandlerInvocation(Object rawURLStreamHandler) {
            this.rawURLStreamHandler = rawURLStreamHandler;

        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if (method.getName() == "openConnection") {
                    if (args.length >= 1 && args[0] != null) {
                        method.setAccessible(true);
                        Object urlConnection = method.invoke(rawURLStreamHandler, args);
                        TimeDevice.getInstance().startRecord(args[0]);
                        URLConnectionInvocation handler = new URLConnectionInvocation(urlConnection);

                        Class targetClass = URLConnection.class;
                        if (urlConnection instanceof HttpsURLConnection) {
                            targetClass = HttpsURLConnection.class;
                        } else if (urlConnection instanceof HttpURLConnection) {
                            targetClass = HttpURLConnection.class;
                        }

                        Object result = ProxyBuilder.forClass(targetClass)
                                .dexCache(appContext.getDir("dx", Context.MODE_PRIVATE))
                                .handler(handler).constructorArgTypes(URL.class)
                                .constructorArgValues(args[0])
                                .build();
                        return result;
                    }
                }

                //handler里有诸如 if (this != url.streamHandler)这类的校验
                Object proxyHandler = null;
                if (args.length > 0) {
                    //TODO 兼容性问题可能
                    if (args[0] instanceof URL) {
                        proxyHandler = streamHandlerField.get(args[0]);
                        streamHandlerField.set(args[0], rawURLStreamHandler);
                    }
                }
                method.setAccessible(true);
                Object result = method.invoke(rawURLStreamHandler, args);
                if (proxyHandler != null && args.length > 0) {
                    if (args[0] instanceof URL) {
                        streamHandlerField.set(args[0], proxyHandler);
                    }
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private static class URLConnectionInvocation implements InvocationHandler {
        Object rawURLConnection;

        URLConnectionInvocation(Object rawURLConnection) {
            this.rawURLConnection = rawURLConnection;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                method.setAccessible(true);
                Object result = method.invoke(rawURLConnection, args);
                if (method.getName() == "getInputStream") {
                    if (!(result instanceof ProxyInputStream)) {
                        ProxyInputStream stream = new ProxyInputStream(result, null);
                        return stream;
                    }
                } else if (method.getName() == "getOutputStream") {
                    if (!(result instanceof ProxyOutputStream)) {
                        ProxyOutputStream stream = new ProxyOutputStream(result, null);
                        return stream;
                    }
                } else if (method.getName() == "getResponseCode") {
                    if (result.equals(200)) {
                        TimeDevice.getInstance().endRecord(null, TimeDevice.NORMAL);
                    } else {
                        TimeDevice.getInstance().endRecord(null, TimeDevice.ERROR_CODE, result);
                    }
                }
                return result;
            } catch (Exception e) {
                throw e;
            }
        }
    }

}
