package com.pingan.inject;

import android.icu.util.Output;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by yunyang on 2017/3/27.
 */

public class ProxyOutputStream extends OutputStream {
    OutputStream rawStream;
    Object address;

    public ProxyOutputStream(OutputStream stream, Object address) {
        rawStream = stream;
        this.address = address;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            rawStream.write(b);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        try {
            rawStream.write(b);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            rawStream.write(b, off, len);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            rawStream.flush();
            TimeDevice.getInstance().endRecord(address, 0);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            rawStream.close();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, 1);
            throw e;
        }
    }
}
