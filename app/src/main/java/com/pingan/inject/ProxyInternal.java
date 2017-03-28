package com.pingan.inject;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;

import okhttp3.Address;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.Internal;
import okhttp3.internal.cache.InternalCache;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.connection.RouteDatabase;
import okhttp3.internal.connection.StreamAllocation;

/**
 * |"||||||||||||||||||||||||||||||||||||||||||
 * Created by yunyang on 2017/3/25.
 */

public class ProxyInternal extends Internal {
    Internal internal;

    public ProxyInternal(Internal internal) {
        this.internal = internal;
        TimeDevice.getInstance();
    }

    @Override
    public void addLenient(Headers.Builder builder, String line) {
        internal.addLenient(builder, line);
    }

    @Override
    public void addLenient(Headers.Builder builder, String name, String value) {
        internal.addLenient(builder, name, value);
    }

    @Override
    public void setCache(OkHttpClient.Builder builder, InternalCache internalCache) {
        internal.setCache(builder, internalCache);
    }

    @Override
    public RealConnection get(ConnectionPool pool, Address address, StreamAllocation streamAllocation, Route route) {
        try {
            Field socketFactoryField = null;
            socketFactoryField = Address.class.getDeclaredField("socketFactory");
            socketFactoryField.setAccessible(true);
            Object socketFactoryObect = socketFactoryField.get(address);
            if(socketFactoryObect != null && !(socketFactoryObect instanceof ProxySocketFactory) ) {
                ProxySocketFactory socketFactory = new ProxySocketFactory(socketFactoryObect, address);
                socketFactoryField.set(address, socketFactory);
                TimeDevice.getInstance().startRecord(address);
            }
            Field sslSocketFactoryField = Address.class.getDeclaredField("sslSocketFactory");
            sslSocketFactoryField.setAccessible(true);
            Object sslSocketFactoryObject = sslSocketFactoryField.get(address);
            if(sslSocketFactoryObject != null && !(sslSocketFactoryObject instanceof ProxySSLSocketFactory)) {
                ProxySSLSocketFactory factory = new ProxySSLSocketFactory(sslSocketFactoryObject, address);
                sslSocketFactoryField.set(address, factory);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        return internal.get(pool, address, streamAllocation, route);
    }

    @Override
    public boolean equalsNonHost(Address a, Address b) {
        return internal.equalsNonHost(a, b);
    }

    @Override
    public Socket deduplicate(ConnectionPool pool, Address address, StreamAllocation streamAllocation) {
        return internal.deduplicate(pool, address, streamAllocation);
    }

    @Override
    public void put(ConnectionPool pool, RealConnection connection) {
        internal.put(pool, connection);
    }

    @Override
    public boolean connectionBecameIdle(ConnectionPool pool, RealConnection connection) {
        return internal.connectionBecameIdle(pool, connection);
    }

    @Override
    public RouteDatabase routeDatabase(ConnectionPool connectionPool) {
        return internal.routeDatabase(connectionPool);
    }

    @Override
    public int code(Response.Builder responseBuilder) {
        return internal.code(responseBuilder);
    }

    @Override
    public void apply(ConnectionSpec tlsConfiguration, SSLSocket sslSocket, boolean isFallback) {
        internal.apply(tlsConfiguration, sslSocket, isFallback);
    }

    @Override
    public HttpUrl getHttpUrlChecked(String url) throws MalformedURLException, UnknownHostException {
        return internal.getHttpUrlChecked(url);
    }

    @Override
    public StreamAllocation streamAllocation(Call call) {
        return internal.streamAllocation(call);
    }

    @Override
    public Call newWebSocketCall(OkHttpClient client, Request request) {
        return internal.newWebSocketCall(client, request);
    }
}
