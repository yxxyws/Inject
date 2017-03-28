package com.pingan.inject;

import com.android.dx.stock.ProxyBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.Address;

/**
 * Created by yunyang on 2017/3/27.
 */

public class ProxySSLSocketFactory2 extends SSLSocketFactory {
    SSLSocketFactory factory;
    Address address;
    public ProxySSLSocketFactory2(Object factory, Object address){
        this.factory = (SSLSocketFactory)factory;
        this.address = (Address)address;
    }

    @Override
    public Socket createSocket() throws IOException {
        Socket socket = factory.createSocket();
        socket = new ProxySSLSocket(socket, this.address);
        return socket;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        Socket socket = factory.createSocket(s, host, port, autoClose);
        socket = new ProxySSLSocket(socket, this.address);
        return socket;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket socket = factory.createSocket(host, port);
        socket = new ProxySSLSocket(socket, this.address);
        return socket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        Socket socket = factory.createSocket(host, port, localHost, localPort);
        socket = new ProxySSLSocket(socket, this.address);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket socket = factory.createSocket(host, port);
        socket = new ProxySSLSocket(socket, this.address);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket socket = factory.createSocket(address, port, localAddress, localPort);
        socket = new ProxySSLSocket(socket, this.address);
        return socket;
    }

    public Socket getProxySSLSocket(Socket sslSocket){

        return null;
    }

    class SSLSocketInvocationHandler implements InvocationHandler{
        Object rawSocket;
        SSLSocketInvocationHandler(Object rawSocket){
            this.rawSocket = rawSocket;

            try {
                Class<?> sslParametersClass;
                try {
                    sslParametersClass = Class.forName("com.android.org.conscrypt.SSLParametersImpl");
                } catch (ClassNotFoundException e) {
                    // Older platform before being unbundled.
                    sslParametersClass = Class.forName(
                            "org.apache.harmony.xnet.provider.jsse.SSLParametersImpl");
                }
            } catch (ClassNotFoundException ignored) {
                // This isn't an Android runtime.
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("b")) {
                System.out.println("inject");
            }
            return ProxyBuilder.callSuper(proxy, method, args);
        }
    }
}
