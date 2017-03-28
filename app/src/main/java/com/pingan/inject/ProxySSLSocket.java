package com.pingan.inject;

import android.annotation.TargetApi;
import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import okhttp3.Address;

/**
 * Created by yunyang on 2017/3/26.
 */

public class ProxySSLSocket extends SSLSocket {
    public ProxySSLSocket(Object rawSocket, Object address) {
        this.rawSocket = (SSLSocket) rawSocket;
        this.address = (Address) address;
    }

    SSLSocket rawSocket;
    Address address;

    @Override
    public String[] getSupportedCipherSuites() {
        return rawSocket.getSupportedCipherSuites();
    }

    @Override
    public String[] getEnabledCipherSuites() {
        return rawSocket.getEnabledCipherSuites();
    }

    @Override
    public void setEnabledCipherSuites(String[] suites) {
        rawSocket.setEnabledCipherSuites(suites);
    }

    @Override
    public String[] getSupportedProtocols() {
        return rawSocket.getSupportedProtocols();
    }

    @Override
    public String[] getEnabledProtocols() {
        return rawSocket.getEnabledProtocols();
    }

    @Override
    public void setEnabledProtocols(String[] protocols) {
        rawSocket.setEnabledProtocols(protocols);
    }

    @Override
    public SSLSession getSession() {
        return rawSocket.getSession();
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        rawSocket.connect(endpoint);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        rawSocket.connect(endpoint, timeout);
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        rawSocket.bind(bindpoint);
    }

    @Override
    public InetAddress getInetAddress() {
        return rawSocket.getInetAddress();
    }

    @Override
    public InetAddress getLocalAddress() {
        return rawSocket.getLocalAddress();
    }

    @Override
    public int getPort() {
        return rawSocket.getPort();
    }

    @Override
    public int getLocalPort() {
        return rawSocket.getLocalPort();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return rawSocket.getRemoteSocketAddress();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return rawSocket.getLocalSocketAddress();
    }

    @Override
    public SocketChannel getChannel() {
        return rawSocket.getChannel();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return rawSocket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return rawSocket.getOutputStream();
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        rawSocket.setTcpNoDelay(on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return rawSocket.getTcpNoDelay();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        rawSocket.setSoLinger(on, linger);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return rawSocket.getSoLinger();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        rawSocket.sendUrgentData(data);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        rawSocket.setOOBInline(on);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return rawSocket.getOOBInline();
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        rawSocket.setSoTimeout(timeout);
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return rawSocket.getSoTimeout();
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        rawSocket.setSendBufferSize(size);
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return rawSocket.getSendBufferSize();
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        rawSocket.setReceiveBufferSize(size);
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return rawSocket.getReceiveBufferSize();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        rawSocket.setKeepAlive(on);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return rawSocket.getKeepAlive();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        rawSocket.setTrafficClass(tc);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return rawSocket.getTrafficClass();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        rawSocket.setReuseAddress(on);
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return rawSocket.getReuseAddress();
    }

    @Override
    public synchronized void close() throws IOException {
        rawSocket.close();
    }

    @Override
    public void shutdownInput() throws IOException {
        rawSocket.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        rawSocket.shutdownOutput();
    }

    @Override
    public int hashCode() {
        return rawSocket.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return rawSocket.equals(obj);
    }

    @Override
    public String toString() {
        return rawSocket.toString();
    }

    @Override
    public boolean isConnected() {
        return rawSocket.isConnected();
    }

    @Override
    public boolean isBound() {
        return rawSocket.isBound();
    }

    @Override
    public boolean isClosed() {
        return rawSocket.isClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return rawSocket.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return rawSocket.isOutputShutdown();
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        rawSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public SSLSession getHandshakeSession() {
        return rawSocket.getHandshakeSession();
    }

    @Override
    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
        rawSocket.addHandshakeCompletedListener(listener);
    }

    @Override
    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
        rawSocket.removeHandshakeCompletedListener(listener);
    }

    @Override
    public void startHandshake() throws IOException {
        rawSocket.startHandshake();
    }

    @Override
    public void setUseClientMode(boolean mode) {
        rawSocket.setUseClientMode(mode);
    }

    @Override
    public boolean getUseClientMode() {
        return rawSocket.getUseClientMode();
    }

    @Override
    public void setNeedClientAuth(boolean need) {
        rawSocket.setNeedClientAuth(need);
    }

    @Override
    public boolean getNeedClientAuth() {
        return rawSocket.getNeedClientAuth();
    }

    @Override
    public void setWantClientAuth(boolean want) {
        rawSocket.setWantClientAuth(want);
    }

    @Override
    public boolean getWantClientAuth() {
        return rawSocket.getWantClientAuth();
    }

    @Override
    public void setEnableSessionCreation(boolean flag) {
        rawSocket.setEnableSessionCreation(flag);
    }

    @Override
    public boolean getEnableSessionCreation() {
        return rawSocket.getEnableSessionCreation();
    }

    @Override
    public SSLParameters getSSLParameters() {
        return rawSocket.getSSLParameters();
    }

    @Override
    public void setSSLParameters(SSLParameters params) {
        rawSocket.setSSLParameters(params);
    }
}
