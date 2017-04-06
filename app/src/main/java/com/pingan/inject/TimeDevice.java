package com.pingan.inject;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yunyang on 2017/3/27.
 */

public class TimeDevice {

    public static final int NORMAL = 0;
    public static final int CONNECTION = 1;
    public static final int DNS = 2;
    public static final int INPUT_IO = 3;
    public static final int OUPUT_IO = 4;
    public static final int CLOSE = 5;
    public static final int OTHER = -1;
    public static final int ERROR_CODE = 6;

    private static TimeDevice instance;

    private TimeDevice() {
    }

    Handler mHandler;
    HashMap<Long, NetRecordStruct> recordMap = new HashMap<>();
    HashMap<Long, NetRecordStruct> cacheMap = new HashMap<>();

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static TimeDevice getInstance() {
        if (instance == null) {
            synchronized (TimeDevice.class) {
                if (instance == null) {
                    instance = new TimeDevice();
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            instance.mHandler = new InnerHandler();
                            Looper.loop();
                        }
                    });
                }
            }
        }
        return instance;
    }

    private static class InnerHandler extends Handler{
        public void handleMessage(Message msg) {
            NetRecordStruct struct = (NetRecordStruct) msg.obj;
            synchronized (instance.recordMap) {
                NetRecordStruct last = instance.recordMap.get(struct.threadId);
                if (struct.startOrEnd) {
                    if (last != null) {
                        throw new RuntimeException("check code, start and end not match");
                    } else {
                        instance.recordMap.put(struct.threadId, struct);
                        instance.cacheMap.remove(struct.threadId);
                        Log.d("TimeDevice", "start " + struct.toString());
                    }
                } else {
                    if (last != null) {
                        Log.d("TimeDevice", (struct.endState == 0 ? "success" : "failed") + " cost time " + (struct.timeStamp - last.timeStamp) + "ms  " + last.toString());
                        instance.recordMap.remove(struct.threadId);
                        instance.cacheMap.put(struct.threadId, struct);
                    } else {
                        last = instance.cacheMap.get(struct.threadId);
                        if (last!=null) {
                            Log.d("TimeDevice", (struct.endState == 0 ? "success" : "failed") + " cost time " + (struct.timeStamp - last.timeStamp) + "ms  " + last.toString());
                        }
                    }
                }
            }

        }
    };

    public void startRecord(Object obj) {
        long threadId = Thread.currentThread().getId();
        long time = System.currentTimeMillis();
        Message message = mHandler.obtainMessage();
        Object address = obj;
        message.obj = new NetRecordStruct(true, address.toString(), threadId, time, 0, null);
        mHandler.sendMessage(message);
    }

    public void endRecord(Object obj, int endState) {
        endRecord(obj, endState, null);
    }

        public void endRecord(Object obj, int endState, Object data) {
        long threadId = Thread.currentThread().getId();
        long time = System.currentTimeMillis();
        Message message = mHandler.obtainMessage();
        if(obj != null) {
            message.obj = new NetRecordStruct(false, obj.toString(), threadId, time, endState, data);
        }else{
            message.obj = new NetRecordStruct(false, "", threadId, time, endState, data);
        }
        mHandler.sendMessage(message);
    }

    private class NetRecordStruct {
        String url;
        long threadId;
        long timeStamp;
        boolean startOrEnd;
        int endState;
        Object data;

        public NetRecordStruct(boolean startOrEnd, String url, long threadId, long timestamp, int endState, Object data) {
            this.url = url;
            this.threadId = threadId;
            this.timeStamp = timestamp;
            this.startOrEnd = startOrEnd;
            this.endState = endState;
            this.data = data;
        }

        @Override
        public String toString() {
            return threadId + "---" + url + "---" + (startOrEnd ? "start" : "end") + "---" + timeStamp;
        }
    }
}
