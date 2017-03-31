package com.pingan.inject;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by yunyang on 2017/3/27.
 */

public class ProxyOutputStream extends OutputStream {
    OutputStream rawStream;
    Object address;

    public ProxyOutputStream(Object stream, Object address) {
        rawStream = (OutputStream)stream;
        this.address = address;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            rawStream.write(b);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, TimeDevice.OUPUT_IO);
            throw e;
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        try {
            rawStream.write(b);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, TimeDevice.OUPUT_IO);
            throw e;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            rawStream.write(b, off, len);
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, TimeDevice.OUPUT_IO);
            throw e;
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            rawStream.flush();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, TimeDevice.OUPUT_IO);
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            rawStream.close();
        } catch (IOException e) {
            TimeDevice.getInstance().endRecord(address, TimeDevice.OUPUT_IO);
            throw e;
        }
    }
}
