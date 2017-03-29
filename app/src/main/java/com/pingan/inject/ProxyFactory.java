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

/**
 * Created by yunyang on 2017/3/28.
 */

public class ProxyFactory {

    private static Class SSLFactoryClass;
    private static Class SSLParametersImplClass;
    private static Class SSLSocketClass;
    private static Context appContext;

    private static Class OKClientClass;
    private static Class InternalClass;
    private static Class AddressClass;

    public static boolean initSuccess = false;

    private static boolean initClass(){
        try{
            OKClientClass = Class.forName("okhttp3.OkHttpClient");
            InternalClass = Class.forName("okhttp3.internal.Internal");
            AddressClass = Class.forName("okhttp3.Address");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(OKClientClass != null){
            if(InternalClass == null){
                try{
                    InternalClass = Class.forName("okhttp3.Internal");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if(InternalClass != null) {
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
                        e.printStackTrace();
                    }
                }
            }
        }
        if(InternalClass != null && SSLFactoryClass!= null) {
            initSuccess = true;
            return true;
        }
        initSuccess = false;
        return false;
    }

    public static boolean initOKhttp(Context context){
        if(initClass()){
            try {
                Constructor constructor = OKClientClass.getConstructor();
                Object okhttpInstance = constructor.newInstance();
                Field field = InternalClass.getDeclaredField("instance");
                field.setAccessible(true);
                Object internalInstance = field.get(InternalClass);
                field.set(null, getProxyInternal(context, internalInstance));
                TimeDevice.getInstance();
                return true;
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

    public static Object getProxyInternal(Context context, Object target){
        try {
            InternalInvocation handler = new InternalInvocation(context, target);
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

    public static Object getSSLSocketFactoryProxy(Context context, Object target, Object address) {
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

        return null;
    }

    //为制定的target生成动态代理对象
    public static Object getDnsProxy(Context context, Object target, Object address) throws IllegalArgumentException {
        if (target != null) {
            //不能用dexmaker因为dnsclass不是class，是interface
            DnsInvocation handler = new DnsInvocation(target, address);
            return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                    target.getClass().getInterfaces(), handler);
        }

        return null;
    }

    private static class SSLSocketFactoryInvocation implements InvocationHandler {
        Object rawFactory;
        Object address;

        SSLSocketFactoryInvocation(Object rawFactory, Object address) {
            this.rawFactory = rawFactory;
            this.address = address;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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
                }catch(Exception e){
                    e.printStackTrace();
                    TimeDevice.getInstance().endRecord(address, TimeDevice.OTHER);
                }

            }
            return method.invoke(rawFactory, args);
        }
    }

    private static class SSLSocketInvocation implements InvocationHandler {
        Object rawSocket;
        Object address;

        SSLSocketInvocation(Object rawSocket, Object address) {
            this.rawSocket = rawSocket;
            this.address = address;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName() != "close") {
                try {
                    Object result = method.invoke(rawSocket, args);
                    if (method.getName() == "getInputStream") {
                        if (!(result instanceof ProxyInputStream)) {
                            ProxyInputStream stream = new ProxyInputStream(result, address);
                            return stream;
                        }
                    } else if (method.getName() == "getOutputStream") {
                        if (!(result instanceof ProxyOutputStream)) {
                            ProxyOutputStream stream = new ProxyOutputStream(result, address);
                            return stream;
                        }
                    }
                    return result;
                } catch (Exception e) {
                    TimeDevice.getInstance().endRecord(address, TimeDevice.CONNECTION);
                    throw new IOException(e);
                }
            } else {
                try {
                    TimeDevice.getInstance().endRecord(address, TimeDevice.CONNECTION);
                    Object b = method.invoke(rawSocket, args);
                    return b;
                } catch (Exception e) {
                    TimeDevice.getInstance().endRecord(address, TimeDevice.CLOSE);
                    throw new IOException(e);
                }
            }
        }
    }

    private static class DnsInvocation implements InvocationHandler {
        Object rawDns;
        Object address;

        public DnsInvocation(Object dns, Object address) {
            this.rawDns = dns;
            this.address = address;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if (method.getName().equals("lookup")) {
                    return method.invoke(rawDns, args);
                }
            } catch (Exception e) {
                TimeDevice.getInstance().endRecord(address, TimeDevice.DNS);
                throw new UnknownHostException(e.getCause().getMessage());
            }
            return method.invoke(rawDns, args);
        }
    }

    private static class InternalInvocation implements InvocationHandler {
        Context staticContext;
        Object rawInternal;

        InternalInvocation(Context staticContext, Object rawInternal){
            this.staticContext = staticContext;
            this.rawInternal = rawInternal;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(method.getName() == "get" && args.length == 4){
                try {
                    boolean hasStart = false;
                    Object address = args[1];
                    Object sslSocketFactoryObject = readFieldOrNull(address, "sslSocketFactory");
                    if(sslSocketFactoryObject == null) {
                        Field socketFactoryField = null;
                        socketFactoryField = AddressClass.getDeclaredField("socketFactory");
                        socketFactoryField.setAccessible(true);
                        Object socketFactoryObect = socketFactoryField.get(address);
                        if (socketFactoryObect != null && !(socketFactoryObect instanceof ProxySocketFactory)) {
                            ProxySocketFactory socketFactory = new ProxySocketFactory(socketFactoryObect, address);
                            socketFactoryField.set(address, socketFactory);
                            TimeDevice.getInstance().startRecord(address);
                            hasStart = true;
                        } else {
                            return method.invoke(rawInternal, args);
                        }
                    }

                    if(sslSocketFactoryObject != null && !sslSocketFactoryObject.getClass().getName().contains("Proxy")) {
                        Field sslSocketFactoryField = AddressClass.getDeclaredField("sslSocketFactory");
                        sslSocketFactoryField.setAccessible(true);
                        if (sslSocketFactoryObject != null) {
                            Object factory = ProxyFactory.getSSLSocketFactoryProxy(staticContext, sslSocketFactoryObject, address);
                            sslSocketFactoryField.set(address, factory);
                        }
                        TimeDevice.getInstance().startRecord(address);
                        hasStart = true;
                    }

                    if(hasStart) {
                        Field DNSField = null;
                        DNSField = AddressClass.getDeclaredField("dns");
                        DNSField.setAccessible(true);
                        Object dnsObject = DNSField.get(address);
                        if (dnsObject != null) {
                            Object newDnsObject = getDnsProxy(staticContext, dnsObject, address);
                            DNSField.set(address, newDnsObject);
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }catch (RuntimeException e){
                    e.printStackTrace();
                }
                return method.invoke(rawInternal, args);
            }
            return method.invoke(rawInternal, args);
        }

        static Object readFieldOrNull(Object instance, String fieldName) {
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
    }
}
