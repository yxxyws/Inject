package com.pingan.inject;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yangyun980 on 17/4/10.
 */

public class FunctionRecorder {
    public static final int START = 0;
    public static final int DNS = 1;
    public static final int CONNECTION = 2;
    public static final int OUPUT_IO = 3;
    public static final int INPUT_IO = 4;
    public static final int CLOSE = 5;
    public static final int OTHER = 6;
    public static final int GET_RETURN_CODE = 7;

    public static final String[] statusWords = {"START", "DNS", "CONNECT", "REQUEST", "RESPONSE",
            "CLOSE", "OTHER", "GET_RETURN_CODE"};

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler mHandler;
    HashMap<Long, NetRecordStruct> startRecordMap = new HashMap<>();
    HashMap<Long, NetRecordStruct> cacheMap = new HashMap<>();
    private static FunctionRecorder instance;

    public List<HttpRecordConfig> getConfigList() {
        return configList;
    }

    public void setConfigList(List<HttpRecordConfig> configList) {
        this.configList = configList;
    }

    private List<HttpRecordConfig> configList;

    private FunctionRecorder() {
    }

    public static FunctionRecorder getInstance() {
        if (instance == null) {
            synchronized (FunctionRecorder.class) {
                if (instance == null) {
                    instance = new FunctionRecorder();
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            instance.mHandler = new FunctionRecorder.InnerHandler();
                            Looper.loop();
                        }
                    });
                }
            }
        }
        return instance;
    }

    private static class InnerHandler extends Handler {
        public void handleMessage(Message msg) {
            NetRecordStruct struct = (NetRecordStruct) msg.obj;

            synchronized (instance.startRecordMap) {
                NetRecordStruct last = instance.startRecordMap.get(struct.threadId);
                // 先做域名过滤
                if (struct.status == START) {
                    if (!instance.isInRecordList(struct.url)) {
                        return;
                    }
                } else {
                    if (last == null) {
                        last = instance.cacheMap.get(struct.threadId);
                    }
                    if (last == null)
                        return;
                }

                Log.d("TimeDevice", struct.toString());
                if (struct.status == START) {
                    if (last == null) {
                        instance.startRecordMap.put(struct.threadId, struct);
                    } else {
                        throw new RuntimeException("check code, twice start in one thread");
                    }
                } else if (struct.isEnd) {
                    if (last == null) {
                        last = instance.cacheMap.get(struct.threadId);
                    } else {
                        instance.startRecordMap.remove(struct.threadId);
                        if (instance.cacheMap.get(struct.threadId) != null) {
                            ;//TODO 需要把cache里的转为正式记录，此外还有cache里一定时间的东西自动转记录
                        }
                        instance.cacheMap.put(struct.threadId, struct);
                    }

                    while (last.nextRecord != null) {
                        last = last.nextRecord;
                    }
                    last.nextRecord = struct;
                } else {
                    if (last == null) {
                        last = instance.cacheMap.get(struct.threadId);
                    }
                    while (last.nextRecord != null) {
                        last = last.nextRecord;
                    }
                    last.nextRecord = struct;
                }
            }
        }
    }

    public boolean hasGotStatusLine(long realThreadId) {
        long threadId = realThreadId;
        if (realThreadId == 0) {
            threadId = Thread.currentThread().getId();
        }
        synchronized (instance.startRecordMap) {
            NetRecordStruct last = instance.startRecordMap.get(threadId);
            // 先做域名过滤
            if (last == null) {
                last = instance.cacheMap.get(threadId);
            }
            if (last == null)
                return false;

            while (last != null) {
                if (last.status == GET_RETURN_CODE) {
                    return true;
                }
                last = last.nextRecord;
            }
        }

        return false;
    }

    /**
     * 请求起始点记录
     *
     * @param address 网址
     */
    public void startRecord(Object address) {
        String addressStr = getString(address);

        //Log.d("TimeDevice", "startRecord");
        long threadId = Thread.currentThread().getId();
        long time = System.currentTimeMillis();
        Message message = mHandler.obtainMessage();
        message.obj = new NetRecordStruct(addressStr, threadId, time, START, false, false, null, null);
        mHandler.sendMessage(message);
    }

    /**
     * 中间点记录，为了跟踪得不到结束点而记录，查bug用
     *
     * @param status
     * @param data
     */
    public void record(int status, String data) {
        record(status, data, 0);
    }

    /**
     * 中间点记录，为了跟踪得不到结束点而记录，查bug用
     *
     * @param status
     * @param data
     */
    public void record(int status, String data, long realThreadId) {
        //Log.d("TimeDevice", "record");
        long threadId = realThreadId;
        if (realThreadId == 0) {
            threadId = Thread.currentThread().getId();
        }
        long time = System.currentTimeMillis();
        Message message = mHandler.obtainMessage();
        message.obj = new NetRecordStruct(null, threadId, time, status, false, false, data, null);
        mHandler.sendMessage(message);
    }

    /**
     * @param b
     * @param off
     * @param len
     */
    public void recordRequestBody(byte[] b, int off, int len) {
        recordRequestBody(b, off, len, 0);
    }

    /**
     * @param b
     * @param off
     * @param len
     * @param realThreadId
     */
    public void recordRequestBody(byte[] b, int off, int len, long realThreadId) {
        long threadId = realThreadId;
        if (realThreadId == 0) {
            threadId = Thread.currentThread().getId();
        }
        synchronized (instance.startRecordMap) {
            NetRecordStruct last = instance.startRecordMap.get(threadId);
            // 先做域名过滤
            if (last == null) {
                last = instance.cacheMap.get(threadId);
            }
            if (last == null)
                return;
        }
        String requestBodyStr = new String(b, off, len);
        String[] ss = requestBodyStr.split("\r\n\r\n");
        try {
            if (ss.length > 1) {
                requestBodyStr = URLDecoder.decode(ss[1], "utf-8");
            } else {
                requestBodyStr = URLDecoder.decode(ss[0], "utf-8");
            }
            record(OUPUT_IO, requestBodyStr, realThreadId);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**
     * 请求成功结束点记录
     *
     * @param status 成功可能有多次记录，以status区分
     * @param data   返回的code，或者不知道code时，为方法名
     */
    public void recordSuccess(int status, String data) {
        recordSuccess(status, data, 0);
    }

    /**
     * 请求成功结束点记录
     *
     * @param status 成功可能有多次记录，以status区分
     * @param data   返回的code，或者不知道code时，为方法名
     */
    public void recordSuccess(int status, String data, long realThreadId) {
        //Log.d("TimeDevice", "recordSuccess");
        long threadId = realThreadId;
        if (realThreadId == 0) {
            threadId = Thread.currentThread().getId();
        }
        long time = System.currentTimeMillis();
        Message message = mHandler.obtainMessage();
        message.obj = new NetRecordStruct(null, threadId, time, status, true, false, data, null);
        mHandler.sendMessage(message);
    }

    /**
     * 请求失败结束点记录，有可能发生多次，例如io异常，close紧接着异常，以第一次的为准
     *
     * @param status 失败在哪一步
     * @param data   如果是发生异常，则为方法名，如果服务器返回，则为code
     */
    public void recordFail(int status, String data, String detail) {
        recordFail(status, data, detail, 0);
    }

    /**
     * 请求失败结束点记录，有可能发生多次，例如io异常，close紧接着异常，以第一次的为准
     *
     * @param status 失败在哪一步
     * @param data   如果是发生异常，则为方法名，如果服务器返回，则为code
     */
    public void recordFail(int status, String data, String detail, long realThreadId) {
        //Log.d("TimeDevice", "recordFail");
        long threadId = realThreadId;
        if (realThreadId == 0) {
            threadId = Thread.currentThread().getId();
        }
        long time = System.currentTimeMillis();
        Message message = mHandler.obtainMessage();
        message.obj = new NetRecordStruct(null, threadId, time, status, true, true, data, detail);
        mHandler.sendMessage(message);
    }

    public static String getString(Object address) {
        if (address != null) {
            if (address instanceof String) {
                return (String) address;
            } else if (address instanceof URL) {
                return address.toString();
            } else {
                String className = address.getClass().getName();
                if (className.contains("Address")) {
                    if (className.contains("okhttp3")) {
                        try {
                            Method m = address.getClass().getMethod("url");
                            Object url = m.invoke(address);
                            if (url != null)
                                return url.toString();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Method m = address.getClass().getMethod("getUriHost");
                            Object url = m.invoke(address);
                            Method tempM = address.getClass().getMethod("getUriPort");
                            Object port = tempM.invoke(address);
                            if (url != null)
                                return url + ":" + port;
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return "";
    }

    private class NetRecordStruct {
        String url;
        long threadId;
        long timeStamp;
        int status;
        boolean isEnd;
        boolean isErrorEnd;
        String data;
        String detail;
        NetRecordStruct nextRecord;

        public NetRecordStruct(String url,
                               long threadId,
                               long timeStamp,
                               int status,
                               boolean isEnd,
                               boolean isErrorEnd,
                               String data, String detail) {
            this.url = url;
            this.threadId = threadId;
            this.timeStamp = timeStamp;
            this.status = status;
            this.isEnd = isEnd;
            this.isErrorEnd = isErrorEnd;
            this.data = data;
            this.detail = detail;
        }


        @Override
        public String toString() {
            if (status == START) {
                return threadId + "--- START ---" + url + " --- " + timeStamp;
            }
            if (isEnd) {
                return threadId + "---" + statusWords[status] + "---" + (isErrorEnd ?
                        "fail" : "success") + "---" + data + "---" + timeStamp;
            } else {
                return threadId + "---" + statusWords[status] + "---" + data + "---" + timeStamp;
            }
        }
    }

    private boolean isInRecordList(String url) {
        if (configList == null || configList.size() == 0)
            return true;
        for (int i = 0; i < configList.size(); i++) {
            String configUrl = configList.get(i).url;
            if (configUrl != null && url.contains(configUrl)) {
                return true;
            }
        }
        return false;
    }
}
