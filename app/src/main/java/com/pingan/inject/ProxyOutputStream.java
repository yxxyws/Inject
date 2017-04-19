package com.pingan.inject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

/**
 * Created by yunyang on 2017/3/27.
 */

public class ProxyOutputStream extends OutputStream {
    OutputStream rawStream;
    boolean isHttp2;

    public ProxyOutputStream(Object stream) {
        rawStream = (OutputStream) stream;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            rawStream.write(b);
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.OUPUT_IO,
                    "ProxyOutputStream_" + "write" + ":", e.getMessage());
            throw e;
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        try {
            rawStream.write(b);
            if (!isHttp2) {
                FunctionRecorder.getInstance().recordRequestBody(b, 0, b.length);
            }
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.OUPUT_IO,
                    "ProxyOutputStream_" + "write" + ":", e.getMessage());
            throw e;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            rawStream.write(b, off, len);
            if (!isHttp2) {
                FunctionRecorder.getInstance().recordRequestBody(b, off, len);
            }
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.OUPUT_IO,
                    "ProxyOutputStream_" + "write" + ":", e.getMessage());
            throw e;
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            rawStream.flush();
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.OUPUT_IO,
                    "ProxyOutputStream_" + "flush" + ":", e.getMessage());
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            rawStream.close();
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.OUPUT_IO,
                    "ProxyOutputStream_" + "close" + ":", e.getMessage());
            throw e;
        }
    }
}
