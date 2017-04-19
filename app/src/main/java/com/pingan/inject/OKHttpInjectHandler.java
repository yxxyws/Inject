package com.pingan.inject;

import android.content.Context;

import com.android.dx.stock.ProxyBuilder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * Created by yunyang on 2017/3/28.
 */

public class OKHttpInjectHandler {
    private static HashMap<Integer, OKHttpInjectHandler> injectHandlers = new HashMap<>();

    public OKHttpInjectHandler(int version) {
        this.RealVersion = version;
    }

    public static OKHttpInjectHandler getHandler(int version) {
        synchronized (OKHttpInjectHandler.class) {
            OKHttpInjectHandler handler = injectHandlers.get(version);
            if (handler == null) {
                handler = new OKHttpInjectHandler(version);
                injectHandlers.put(version, handler);
            }
            return handler;
        }
    }

    private Class SSLFactoryClass;
    private Class SSLParametersImplClass;
    private Class SSLSocketClass;
    private Context appContext;

    private Class OKClientClass;
    private Class InternalClass;
    private Class AddressClass;
    private Object rawInternalInstance;
    private static boolean V2DNSInjected = false;

    public boolean initClientSuccess = false;

    private int OKHttpVersion = 0;//可以被认为的版本号，目前2.0~2.6为20， 2.7~3.6为30
    private int RealVersion = 0;//真实版本号

    private boolean initClass() {
        if (RealVersion < 30) {
            try {
                OKClientClass = Class.forName("com.squareup.okhttp.OkHttpClient");
                InternalClass = Class.forName("com.squareup.okhttp.internal.Internal");
                Method[] methods = InternalClass.getMethods();
                for (Method m : methods) {
                    //特别针对okhttp 2.7的情况，其代码和okhttp3.x类似，但用的是2.x的包名
                    if (m.getName() == "get") {
                        OKHttpVersion = 30;
                        break;
                    }
                }
                if (OKHttpVersion == 30) {
                    AddressClass = Class.forName("com.squareup.okhttp.Address");
                } else {
                    //2.6及以下版本用不到AddressClass
                    OKHttpVersion = 20;
                }
            } catch (ClassNotFoundException e) {
                //e.printStackTrace();
            }
        } else {
            try {
                OKClientClass = Class.forName("okhttp3.OkHttpClient");
                InternalClass = Class.forName("okhttp3.internal.Internal");
                AddressClass = Class.forName("okhttp3.Address");
                OKHttpVersion = 30;
            } catch (ClassNotFoundException e) {
                //e.printStackTrace();
            }
        }

        if (OKClientClass == null) {
            return false;
        }

        if (OKClientClass != null) {
            if (InternalClass == null) {
                try {
                    InternalClass = Class.forName("okhttp3.Internal");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (InternalClass != null) {
                try {
                    SSLFactoryClass = Class.forName(
                            "com.android.org.conscrypt.OpenSSLSocketFactoryImpl");
                    SSLParametersImplClass = Class.forName("com.android.org.conscrypt.SSLParametersImpl");
                    SSLSocketClass = Class.forName("com.android.org.conscrypt.OpenSSLSocketImpl");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (SSLFactoryClass == null) {
                    try {
                        SSLFactoryClass = Class.forName("org.apache.harmony.xnet.provider.jsse.OpenSSLSocketFactoryImpl");
                        SSLParametersImplClass = Class.forName("org.apache.harmony.xnet.provider.jsse.SSLParametersImpl");
                        SSLSocketClass = Class.forName("org.apache.harmony.xnet.provider.jsse.OpenSSLSocketImpl");
                    } catch (ClassNotFoundException e) {
                        //e.printStackTrace();
                    }
                }
            }
        }
        if (InternalClass != null && SSLFactoryClass != null) {
            return true;
        }
        return false;
    }

    public boolean initOKhttp(Context context) {
        if (!initClientSuccess && initClass()) {
            try {
                Constructor constructor = OKClientClass.getConstructor();
                Object okhttpInstance = constructor.newInstance();
                Field field = InternalClass.getDeclaredField("instance");
                field.setAccessible(true);
                Object internalInstance = field.get(InternalClass);
                if (internalInstance != null && !internalInstance.getClass().getName().contains("Proxy")) {
                    rawInternalInstance = internalInstance;
                    if (OKHttpVersion == 30) {
                        field.set(null, getProxyInternalV3(context, internalInstance));
                    } else if (OKHttpVersion == 20) {
                        field.set(null, getProxyInternalV2(context, internalInstance));
                    } else {
                        return false;
                    }
                    FunctionRecorder.getInstance();
                    initClientSuccess = true;
                    return true;
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean recoverOkHttp() {
        if (initClientSuccess) {
            try {
                Field field = InternalClass.getDeclaredField("instance");
                field.setAccessible(true);
                if (rawInternalInstance != null) {
                    field.set(null, rawInternalInstance);
                    return true;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            initClientSuccess = false;
        }
        return true;
    }

    public Object getProxyInternalV3(Context context, Object target) {
        try {
            InternalInvocationV3 handler = new InternalInvocationV3(context, target);
            if (appContext == null) {
                appContext = context;
            }
            Object result = ProxyBuilder.forClass(InternalClass)
                    .dexCache(context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(handler)
                    .build();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return target;
    }

    public Object getSSLSocketFactoryProxy(Context context, Object target, Object address) {
        SSLSocketFactoryInvocation handler = new SSLSocketFactoryInvocation(target, address);
        try {
            Object result = ProxyBuilder.forClass(SSLFactoryClass)
                    .dexCache(context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(handler)
                    .build();
            Field sslParametersField = target.getClass().getDeclaredField("sslParameters");
            sslParametersField.setAccessible(true);
            Object parameter = sslParametersField.get(target);
            sslParametersField.set(result, parameter);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return target;
    }

    //为制定的target生成动态代理对象
    public Object getDnsProxy(Context context, Object target) throws IllegalArgumentException {
        if (target != null) {
            //不能用dexmaker因为dnsclass不是class，是interface
            DnsInvocation handler = new DnsInvocation(target);
            return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                    target.getClass().getInterfaces(), handler);
        }

        return target;
    }

    private class SSLSocketFactoryInvocation implements InvocationHandler {
        Object rawFactory;
        Object address;

        SSLSocketFactoryInvocation(Object rawFactory, Object address) {
            this.rawFactory = rawFactory;
            this.address = address;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            method.setAccessible(true);
            if (method.getName().equals("createSocket")) {
                try {
                    Object sslSocket = method.invoke(rawFactory, args);
                    if (args.length == 4) {
                        SSLSocketInvocation handler = new SSLSocketInvocation(sslSocket, address);
                        Field sslParametersField = rawFactory.getClass().getDeclaredField("sslParameters");
                        sslParametersField.setAccessible(true);
                        Object parameter = sslParametersField.get(rawFactory);
                        Method cloneMethod = parameter.getClass().getDeclaredMethod("clone");
                        cloneMethod.setAccessible(true);
                        Object newParameter = cloneMethod.invoke(parameter);
                        Object result = ProxyBuilder.forClass(SSLSocketClass)
                                .dexCache(appContext.getDir("dx", Context.MODE_PRIVATE))
                                .handler(handler).constructorArgTypes(Socket.class, String.class, int.class,
                                        boolean.class, SSLParametersImplClass).
                                        constructorArgValues(args[0], args[1], args[2], args[3], newParameter)
                                .build();
                        return result;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        FunctionRecorder.getInstance().recordFail(FunctionRecorder.OTHER, "SSLSocketFactoryInvocation_"
                                + method.getName() + ":", cause.getMessage());
                    } else
                        FunctionRecorder.getInstance().recordFail(FunctionRecorder.OTHER, "SSLSocketFactoryInvocation_"
                                + method.getName() + ":", e.getMessage());
                    throw e;
                }
            }
            return method.invoke(rawFactory, args);
        }
    }

    private class SSLSocketInvocation implements InvocationHandler {
        Object rawSocket;
        Object address;

        SSLSocketInvocation(Object rawSocket, Object address) {
            this.rawSocket = rawSocket;
            this.address = address;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            method.setAccessible(true);
            if (method.getName() != "close") {
                try {
                    Object result = method.invoke(rawSocket, args);
                    if (method.getName() == "getInputStream") {
                        if (!(result instanceof ProxyInputStream)) {
                            ProxyInputStream stream = new ProxyInputStream(result);
                            stream.setRealThreadId(Thread.currentThread().getId());
                            return stream;
                        }
                    } else if (method.getName() == "getOutputStream") {
                        if (!(result instanceof ProxyOutputStream)) {
                            ProxyOutputStream stream = new ProxyOutputStream(result);
                            return stream;
                        }
                    }
                    return result;
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        FunctionRecorder.getInstance().recordFail(FunctionRecorder.CONNECTION,
                                "SSLSocketInvocation_" + method.getName() + ":", cause.getMessage());
                    } else {
                        FunctionRecorder.getInstance().recordFail(FunctionRecorder.CONNECTION,
                                "SSLSocketInvocation_" + method.getName() + ":", e.getMessage());
                    }
                    e.printStackTrace();
                    throw new IOException(e);
                }
            } else {
                try {
                    Object b = method.invoke(rawSocket, args);
                    FunctionRecorder.getInstance().record(FunctionRecorder.CLOSE, "SSLSocketInvocation_" + method.getName());
                    return b;
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        FunctionRecorder.getInstance().recordFail(FunctionRecorder.CLOSE,
                                "SSLSocketInvocation_" + method.getName() + ":", cause.getMessage());
                    } else
                        FunctionRecorder.getInstance().recordFail(FunctionRecorder.CLOSE,
                                "SSLSocketInvocation_" + method.getName() + ":", e.getMessage());
                    e.printStackTrace();
                    throw new IOException(e);
                }
            }
        }
    }

    private class DnsInvocation implements InvocationHandler {
        Object rawDns;
        String host;

        public DnsInvocation(Object dns) {
            this.rawDns = dns;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                method.setAccessible(true);
                //同个类方法名变来变去
                if (method.getName().equals("lookup") || method.getName().equals("getAllByName")
                        || method.getName().equals("resolveInetAddresses")) {
                    host = (String) args[0];
                    return method.invoke(rawDns, args);
                }
            } catch (Exception e) {
                Throwable cause = e.getCause();
                if (cause != null) {
                    FunctionRecorder.getInstance().recordFail(FunctionRecorder.DNS,
                            "DnsInvocation_" + method.getName() + ":", cause.getMessage());
                } else
                    FunctionRecorder.getInstance().recordFail(FunctionRecorder.DNS, "DnsInvocation_" +
                            method.getName() + ":", e.getMessage());
                e.printStackTrace();
                throw new UnknownHostException(e.getCause().getMessage());
            }
            return method.invoke(rawDns, args);
        }
    }

    private class InternalInvocationV3 implements InvocationHandler {
        Context staticContext;
        Object rawInternal;

        InternalInvocationV3(Context staticContext, Object rawInternal) {
            this.staticContext = staticContext;
            this.rawInternal = rawInternal;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            method.setAccessible(true);
            if (method.getName() == "get" && args.length >= 3) {
                try {
                    Object address = args[1];
                    Field socketFactoryField = null;
                    socketFactoryField = AddressClass.getDeclaredField("socketFactory");
                    socketFactoryField.setAccessible(true);
                    Object socketFactoryObect = socketFactoryField.get(address);
                    if (socketFactoryObect != null && !(socketFactoryObect instanceof ProxySocketFactory)) {
                        ProxySocketFactory socketFactory = new ProxySocketFactory(socketFactoryObect, staticContext);
                        socketFactoryField.set(address, socketFactory);
                        FunctionRecorder.getInstance().startRecord(address);
                    } else {
                        return method.invoke(rawInternal, args);
                    }

                    Object sslSocketFactoryObject = readFieldOrNull(address, "sslSocketFactory");
                    if (sslSocketFactoryObject != null) {
                        if (!sslSocketFactoryObject.getClass().getName().contains("Proxy")) {
                            Field sslSocketFactoryField = AddressClass.getDeclaredField("sslSocketFactory");
                            sslSocketFactoryField.setAccessible(true);
                            if (sslSocketFactoryObject != null) {
                                Object factory = getSSLSocketFactoryProxy(staticContext, sslSocketFactoryObject, address);
                                sslSocketFactoryField.set(address, factory);
                            }
                        } else
                            return method.invoke(rawInternal, args);
                    }
                    Field DNSField = null;
                    DNSField = AddressClass.getDeclaredField("dns");
                    DNSField.setAccessible(true);
                    Object dnsObject = DNSField.get(address);
                    if (dnsObject != null) {
                        Object newDnsObject = getDnsProxy(staticContext, dnsObject);
                        DNSField.set(address, newDnsObject);
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return method.invoke(rawInternal, args);
            }else if(method.getName() == "put"){
                Object obj = args[1];
            }
            return method.invoke(rawInternal, args);
        }
    }

    Object readFieldOrNull(Object instance, String fieldName) {
        Field field = null;
        try {
            field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object resultObject = field.get(instance);
            return resultObject;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Object getProxyInternalV2(Context context, Object target) {
        try {
            InternalInvocationV2 handler = new InternalInvocationV2(context, target);
            if (appContext == null) {
                appContext = context;
            }
            Object result = ProxyBuilder.forClass(InternalClass)
                    .dexCache(context.getDir("dx", Context.MODE_PRIVATE))
                    .handler(handler)
                    .build();


            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return target;
    }

    private class InternalInvocationV2 implements InvocationHandler {
        Context context;
        Object rawInternal;
        private String urlString;

        InternalInvocationV2(Context context, Object rawInternal) {
            this.context = context;
            this.rawInternal = rawInternal;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            method.setAccessible(true);
            if (method.getName() == "internalCache" && args.length == 1) {
                Object okhttpClient = args[0];
                try {
                    if (!V2DNSInjected) {
                        //2.x的实现不一样，我们只好去改DNS.DEFAULT
                        Class DNSClass = null;
                        //2.0
                        try {
                            DNSClass = Class.forName("com.squareup.okhttp.internal.Dns");
                        } catch (ClassNotFoundException e) {
                            //e.printStackTrace();
                        }

                        // 2.6
                        if (DNSClass == null) {
                            try {
                                DNSClass = Class.forName("com.squareup.okhttp.Dns");
                            } catch (ClassNotFoundException e) {
                                //e.printStackTrace();
                            }
                        }

                        if (DNSClass == null) {
                            try {
                                DNSClass = Class.forName("com.squareup.okhttp.internal.Network");
                            } catch (ClassNotFoundException e) {
                                //e.printStackTrace();
                            }
                        }
                        Field defaultFieldDNS = DNSClass.getField("DEFAULT");
                        if (!defaultFieldDNS.getClass().isAnonymousClass()) {
                            defaultFieldDNS.setAccessible(true);
                            defaultFieldDNS.set(null, getDnsProxy(context, defaultFieldDNS.get(DNSClass)));
                        }
                        V2DNSInjected = true;
                    }
                    Field socketFactoryField = null;
                    socketFactoryField = okhttpClient.getClass().getDeclaredField("socketFactory");
                    socketFactoryField.setAccessible(true);
                    Object socketFactoryObect = socketFactoryField.get(okhttpClient);
                    if (socketFactoryObect != null && !(socketFactoryObect instanceof ProxySocketFactory)) {
                        ProxySocketFactory socketFactory = new ProxySocketFactory(socketFactoryObect, appContext);
                        socketFactoryField.set(okhttpClient, socketFactory);

                        Object sslSocketFactoryObject = readFieldOrNull(okhttpClient, "sslSocketFactory");
                        if (sslSocketFactoryObject != null && !sslSocketFactoryObject.getClass().getName().contains("Proxy")) {
                            Field sslSocketFactoryField = okhttpClient.getClass().getDeclaredField("sslSocketFactory");
                            sslSocketFactoryField.setAccessible(true);
                            if (sslSocketFactoryObject != null) {
                                Object factory = getSSLSocketFactoryProxy(context, sslSocketFactoryObject, null);
                                sslSocketFactoryField.set(okhttpClient, factory);
                            }
                        }

                        Field internalCacheField = okhttpClient.getClass().getDeclaredField("internalCache");
                        internalCacheField.setAccessible(true);
                        Object internalCacheObject = internalCacheField.get(okhttpClient);
                        internalCacheField.set(okhttpClient, getInternalCacheProxy(context, internalCacheObject));
                    }

                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (method.getName() == "newTransport") {
                Object newTransport = method.invoke(rawInternal, args);
                TransportInvocationHandler handler = new TransportInvocationHandler(newTransport);
                Object result = Proxy.newProxyInstance(newTransport.getClass().getClassLoader(),
                        newTransport.getClass().getInterfaces(), handler);
                return result;
            }
            return method.invoke(rawInternal, args);
        }
    }

    public Object getInternalCacheProxy(Context context, Object target) {
        try {
            InternalCacheInvocation handler = new InternalCacheInvocation(target);
            Class targetClass = Class.forName("com.squareup.okhttp.internal.InternalCache");
            Object result = Proxy.newProxyInstance(targetClass.getClassLoader(),
                    new Class[]{targetClass}, handler);
            return result;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return target;
    }

    private class InternalCacheInvocation implements InvocationHandler {
        Object rawInternalCache;

        InternalCacheInvocation(Object rawInternalCache) {
            this.rawInternalCache = rawInternalCache;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                method.setAccessible(true);
                if (method.getName() == "get") {
                    Object request = args[0];
                    //// TODO: 17/4/11 检查okhttp兼容版本
                    Method urlStringMethod = request.getClass().getDeclaredMethod("urlString");
                    urlStringMethod.setAccessible(true);
                    Object urlString = urlStringMethod.invoke(request);
                    if (urlString != null) {
                        FunctionRecorder.getInstance().startRecord(urlString.toString());
                    }
                }
                if (rawInternalCache != null) {
                    return method.invoke(rawInternalCache, args);
                } else
                    return null;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private class TransportInvocationHandler implements InvocationHandler {
        Object rawTransport;

        TransportInvocationHandler(Object rawTransport) {
            this.rawTransport = rawTransport;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                method.setAccessible(true);
                Object result = method.invoke(rawTransport, args);

                if (method.getName() == "getTransferStream") {
                    FunctionRecorder.getInstance().record(FunctionRecorder.INPUT_IO, "TransportInvocationHandler_" + method.getName());
                } else if (method.getName() == "openResponseBody") {
                    // 2.20至2.60的版本才有的方法
                    FunctionRecorder.getInstance().record(FunctionRecorder.INPUT_IO, "TransportInvocationHandler_" + method.getName());
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                Throwable cause = e.getCause();
                if (cause != null) {
                    FunctionRecorder.getInstance().recordFail(FunctionRecorder.INPUT_IO,
                            "TransportInvocationHandler_" + method.getName() + ":", cause.getMessage());
                } else
                    FunctionRecorder.getInstance().recordFail(FunctionRecorder.INPUT_IO,
                            "TransportInvocationHandler_" + method.getName() + ":", e.getMessage());
                throw e;
            }
        }
    }
}
