package com.pingan.inject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by yunyang on 2017/3/27.
 */

public class ProxyInputStream extends InputStream {
    InputStream rawInputStream;

    public void setRealThreadId(long realThreadId) {
        this.realThreadId = realThreadId;
    }

    //因为http2.0时，读返回包是线程异步的，所以需要额外同步的那个线程.
    long realThreadId;

    /**
     * 设置是否是HttpUrlConnection，如果为是，则不做statusLine分析，因为statusLine已经被剥离了。
     *
     * @param httpUrlConnection
     */
    public void setHttpConnection(boolean httpUrlConnection) {
        isHttpUrlConnection = httpUrlConnection;
    }

    boolean isHttpUrlConnection;
    Http2HeaderParser http2HeaderParser;

    public ProxyInputStream(Object stream) {
        rawInputStream = (InputStream) stream;
    }

    @Override
    public int read() throws IOException {
        try {
            int ret = rawInputStream.read();
            return ret;
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.INPUT_IO, "ProxyInputStream_" + "read" + ":"
                    , e.getMessage(), realThreadId);
            throw e;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        try {
            int ret = rawInputStream.read(b);
//            if (!hasGotStatus && b.length > 20) {
//                String responseString = new String(b);
//                StatusLine statusLine = StatusLine.parse(responseString);
//                if (statusLine != null) {
//                    FunctionRecorder.getInstance().recordSuccess(FunctionRecorder.INPUT_IO, Integer.toString(statusLine.code));
//                    hasGotStatus = true;
//                }
//            }
            return ret;
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.INPUT_IO, "ProxyInputStream_" + "read" + ":"
                    , e.getMessage(), realThreadId);
            throw e;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            int ret = rawInputStream.read(b, off, len);
            boolean hasGotStatus = FunctionRecorder.getInstance().hasGotStatusLine(realThreadId);
            boolean isHttp2 = false;
            if (realThreadId != 0 && realThreadId != Thread.currentThread().getId())
                isHttp2 = true;//TODO 粗略判断http2.0，可能有问题
            if (!hasGotStatus) {
                if (!isHttpUrlConnection && !isHttp2 && ret > 20) {
                    String responseString = new String(b, off, ret);
                    StatusLine statusLine = StatusLine.parse(responseString);
                    if (statusLine != null) {
                        FunctionRecorder.getInstance().recordSuccess(FunctionRecorder.GET_RETURN_CODE,
                                Integer.toString(statusLine.code), realThreadId);
                    } else {
                    }
                } else if (isHttp2 && ret >= 9) {
                    //http2 frame大于9个字节
                    if (http2HeaderParser == null)
                        http2HeaderParser = new Http2HeaderParser();
                    List<Header> headerList = http2HeaderParser.parse(b, off, ret);
                    if (headerList.size() > 0) {
                        for (Header head : headerList) {
                            if (head.name.equals(Header.RESPONSE_STATUS)) {
                                FunctionRecorder.getInstance().recordSuccess(FunctionRecorder.GET_RETURN_CODE,
                                        head.value.utf8(), realThreadId);
                            }
                        }
                    }
                }
            }
            return ret;
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.INPUT_IO,
                    "ProxyInputStream_" + "read" + ":", e.getMessage(), realThreadId);
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return rawInputStream.skip(n);
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.INPUT_IO,
                    "ProxyInputStream_" + "skip" + ":", e.getMessage());
            throw e;
        }
    }

    @Override
    public int available() throws IOException {
        try {
            return rawInputStream.available();
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.INPUT_IO,
                    "ProxyInputStream_" + "available" + ":", e.getMessage());
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            rawInputStream.close();
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.INPUT_IO,
                    "ProxyInputStream_" + "close" + ":", e.getMessage());
            throw e;
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            rawInputStream.mark(readlimit);
        } catch (Exception e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.INPUT_IO,
                    "ProxyInputStream_" + "mark" + ":", e.getMessage());
            throw e;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        try {
            rawInputStream.reset();
        } catch (IOException e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.INPUT_IO,
                    "ProxyInputStream_" + "reset" + ":", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean markSupported() {
        try {
            return rawInputStream.markSupported();
        } catch (Exception e) {
            FunctionRecorder.getInstance().recordFail(FunctionRecorder.INPUT_IO,
                    "ProxyInputStream_" + "markSupported" + ":", e.getMessage());
            throw e;
        }
    }
}
