package com.pingan.inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import okhttp3.Address;

/**
 * Created by yunyang on 2017/3/25.
 */

public class ProxySocket extends Socket {

    public Object address;
    public Socket realSocket;

    public ProxySocket(Object realSocket, Object address) {
        this.realSocket = (Socket) realSocket;
        this.address = address;
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        try {
            realSocket.connect(endpoint);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        try {
            realSocket.connect(endpoint, timeout);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        try {
            realSocket.bind(bindpoint);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public InetAddress getInetAddress() {
        return realSocket.getInetAddress();
    }

    @Override
    public InetAddress getLocalAddress() {
        return realSocket.getLocalAddress();
    }

    @Override
    public int getPort() {
        return realSocket.getPort();
    }

    @Override
    public int getLocalPort() {
        return realSocket.getLocalPort();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return realSocket.getRemoteSocketAddress();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return realSocket.getLocalSocketAddress();
    }

    @Override
    public SocketChannel getChannel() {
        return realSocket.getChannel();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            InputStream result = realSocket.getInputStream();
            if (!(result instanceof ProxyInputStream)) {
                return new ProxyInputStream(result, address);
            }
            return result;
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        try {
            OutputStream result = realSocket.getOutputStream();
            if (!(result instanceof ProxyOutputStream)) {
                return new ProxyOutputStream(result, address);
            }
            return result;
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        try {
            realSocket.setTcpNoDelay(on);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        try {
            return realSocket.getTcpNoDelay();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        try {
            realSocket.setSoLinger(on, linger);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public int getSoLinger() throws SocketException {
        try {
            return realSocket.getSoLinger();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        try {
            realSocket.sendUrgentData(data);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        try {
            realSocket.setOOBInline(on);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        try {
            return realSocket.getOOBInline();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        try {
            realSocket.setSoTimeout(timeout);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        try {
            return realSocket.getSoTimeout();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        try {
            realSocket.setSendBufferSize(size);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        try {
            return realSocket.getSendBufferSize();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        try {
            realSocket.setReceiveBufferSize(size);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        try {
            return realSocket.getReceiveBufferSize();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        try {
            realSocket.setKeepAlive(on);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        try {
            return realSocket.getKeepAlive();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        try {
            realSocket.setTrafficClass(tc);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public int getTrafficClass() throws SocketException {
        try {
            return realSocket.getTrafficClass();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        try {
            realSocket.setReuseAddress(on);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        try {
            return realSocket.getReuseAddress();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            realSocket.close();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void shutdownInput() throws IOException {
        try {
            realSocket.shutdownInput();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void shutdownOutput() throws IOException {
        try {
            realSocket.shutdownOutput();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public String toString() {
        return realSocket.toString();
    }

    @Override
    public boolean isConnected() {
        return realSocket.isConnected();
    }

    @Override
    public boolean isBound() {
        return realSocket.isBound();
    }

    @Override
    public boolean isClosed() {
        return realSocket.isClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return realSocket.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return realSocket.isOutputShutdown();
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency,
                                          int bandwidth) {
        realSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }
}
