package com.pingan.inject;

import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Address;

/**
 * Created by yunyang on 2017/3/27.
 */

public class ProxyInputStream extends InputStream {
    InputStream rawInputStream;
    Object address;

    public ProxyInputStream(InputStream stream, Object address) {
        rawInputStream = stream;
        this.address = address;
    }

    @Override
    public int read() throws IOException {
        try {
            return rawInputStream.read();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        try {
            return rawInputStream.read(b);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            return rawInputStream.read(b, off, len);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return rawInputStream.skip(n);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public int available() throws IOException {
        try {
            return rawInputStream.available();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            rawInputStream.close();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            rawInputStream.mark(readlimit);
        } catch (Exception e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        try {
            rawInputStream.reset();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public boolean markSupported() {
        try {
            return rawInputStream.markSupported();
        } catch (Exception e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }
}
