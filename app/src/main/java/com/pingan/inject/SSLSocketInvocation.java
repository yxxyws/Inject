package com.pingan.inject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by yunyang on 2017/3/27.
 */

public class SSLSocketInvocation implements InvocationHandler {

    Object rawSocket;
    Object address;
    public SSLSocketInvocation(Object rawSocket, Object address){
        this.rawSocket = rawSocket;
        this.address = address;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (method.getName().equals("getInputStream")) {
                ;
            } else if (method.getName().equals("getOutputStream")) {
                ;
            }
            Object result = method.invoke(rawSocket, args);
            return result;
        }catch(Exception e){
            TimeDevice.getInstance().endRecord(address, TimeDevice.OTHER);
            throw e;
        }
    }
}
