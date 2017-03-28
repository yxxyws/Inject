package com.pingan.inject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.Address;

/**
 * Created by yunyang on 2017/3/26.
 */

public class ProxySSLSocketFactory extends SSLSocketFactory {
    SSLSocketFactory factory;
    Address address;
    public ProxySSLSocketFactory(Object factory, Object address){
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
}
