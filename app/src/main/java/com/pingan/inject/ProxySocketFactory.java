package com.pingan.inject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import okhttp3.Address;

/**
 * Created by yunyang on 2017/3/25.
 */

public class ProxySocketFactory extends SocketFactory {
    private final SocketFactory factory;
    Address address;

    public ProxySocketFactory(Object factory, Object address) {
        this.factory = (SocketFactory) factory;
        this.address = (Address) address;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        try {
            Socket socket = factory.createSocket(host, port);
            socket = new ProxySocket(socket, this.address);
            return socket;
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public Socket createSocket() throws IOException {
        try {
            Socket socket = factory.createSocket();
            socket = new ProxySocket(socket, this.address);
            return socket;
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws
            IOException, UnknownHostException {
        try {
            Socket socket = factory.createSocket(host, port, localHost, localPort);
            socket = new ProxySocket(socket, this.address);
            return socket;
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        try {
            Socket socket = factory.createSocket(host, port);
            socket = new ProxySocket(socket, this.address);
            return socket;
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
                               int localPort) throws IOException {
        try {
            Socket socket = factory.createSocket(address, port, localAddress, localPort);
            socket = new ProxySocket(socket, this.address);
            return socket;
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }
}
