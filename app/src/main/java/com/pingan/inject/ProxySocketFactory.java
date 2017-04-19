package com.pingan.inject;

import android.content.Context;

import com.android.dx.stock.ProxyBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

/**
 * Created by yunyang on 2017/3/25.
 */

public class ProxySocketFactory extends SocketFactory {
    private final SocketFactory factory;
    Context appContext;

    public ProxySocketFactory(Object factory, Context appContext) {
        this.factory = (SocketFactory) factory;
        this.appContext = appContext;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        try {
            Socket socket = factory.createSocket(host, port);
            socket = (Socket) getSocket(socket);
            return socket;
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.OTHER,
                    "ProxySocketFactory_" + "createSocket" + ":" , e.getMessage());
            throw e;
        }
    }

    @Override
    public Socket createSocket() throws IOException {
        try {
            Socket socket = factory.createSocket();
            socket = (Socket) getSocket(socket);
            return socket;
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.OTHER,
                    "ProxySocketFactory_" + "createSocket" + ":" , e.getMessage());
            throw e;
        }
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws
            IOException, UnknownHostException {
        try {
            Socket socket = factory.createSocket(host, port, localHost, localPort);
            socket = (Socket) getSocket(socket);
            return socket;
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.OTHER, "ProxySocketFactory_" + "createSocket" + ":"
                    , e.getMessage());
            throw e;
        }
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        try {
            Socket socket = factory.createSocket(host, port);
            socket = (Socket) getSocket(socket);
            return socket;
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.OTHER, "ProxySocketFactory_" + "createSocket" + ":"
                    , e.getMessage());
            throw e;
        }
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
                               int localPort) throws IOException {
        try {
            Socket socket = factory.createSocket(address, port, localAddress, localPort);
            socket = (Socket) getSocket(socket);
            return socket;
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.OTHER,
                    "ProxySocketFactory_" + "createSocket" + ":" , e.getMessage());
            throw e;
        }
    }

    private Object getSocket(Socket socket) throws IOException {
        SocketInvocationHandler handler = new SocketInvocationHandler(socket);
        Object result = null;
        result = ProxyBuilder.forClass(Socket.class)
                .dexCache(appContext.getDir("dx", Context.MODE_PRIVATE))
                .handler(handler)
                .build();
        return result;
    }

    private class SocketInvocationHandler implements InvocationHandler {
        Object rawSocket;

        SocketInvocationHandler(Object rawSocket) {
            this.rawSocket = rawSocket;
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
                                "SocketInvocationHandler_" + method.getName() + ":" , cause.getMessage());
                    } else
                        FunctionRecorder.getInstance().recordFail(FunctionRecorder.CONNECTION,
                                "SocketInvocationHandler_" + method.getName() + ":" , e.getMessage());
                    e.printStackTrace();
                    throw new IOException(e);
                }
            } else {
                try {
                    Object b = method.invoke(rawSocket, args);
                    return b;
                } catch (Exception e) {
                    FunctionRecorder.getInstance().recordFail(FunctionRecorder.CLOSE, "SocketInvocationHandler_" + method.getName() + ":"
                            , e.getMessage());
                    e.printStackTrace();
                    throw new IOException(e);
                }
            }
        }
    }
}
