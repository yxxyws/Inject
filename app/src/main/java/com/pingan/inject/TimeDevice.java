package com.pingan.inject;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.Address;

/**
 * Created by yunyang on 2017/3/27.
 */

public class TimeDevice {

    private static TimeDevice instance;

    private TimeDevice() {
    }

    Handler mHandler;
    HashMap<String, NetRecordStruct> recordMap = new HashMap<>();

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    ;

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
                NetRecordStruct last = instance.recordMap.get(struct.threadName);
                if (struct.startOrEnd) {
                    if (last != null) {
                        throw new RuntimeException("check code, start and end not match");
                    } else
                        instance.recordMap.put(struct.threadName, struct);
                } else {
                    if (last != null) {
                        Log.d("TimeDevice", (struct.endState == 0 ? "success" : "failed") + " cost time " + (struct.timeStamp - last.timeStamp) + "ms  " + struct.toString());
                        instance.recordMap.remove(struct.threadName);
                    } else {
                        Log.d("TimeDevice", struct.toString());
                    }
                }
            }

        }
    };

    public void startRecord(Object obj) {
        String threadName = Thread.currentThread().getName();
        long time = System.currentTimeMillis();
        Message message = mHandler.obtainMessage();
        Object address = obj;
        message.obj = new NetRecordStruct(true, address.toString(), threadName, time, 0);
        mHandler.sendMessage(message);
    }

    public void endRecord(Object obj, int endState) {
        String threadName = Thread.currentThread().getName();
        long time = System.currentTimeMillis();
        Message message = mHandler.obtainMessage();
        Address address = (Address) obj;
        message.obj = new NetRecordStruct(false, address.toString(), threadName, time, endState);
        mHandler.sendMessage(message);
    }

    private class NetRecordStruct {
        String url;
        String threadName;
        long timeStamp;
        boolean startOrEnd;
        int endState;

        public NetRecordStruct(boolean startOrEnd, String url, String threadName, long timestamp, int endState) {
            this.url = url;
            this.threadName = threadName;
            this.timeStamp = timestamp;
            this.startOrEnd = startOrEnd;
            this.endState = endState;
        }

        @Override
        public String toString() {
            return threadName + "---" + url + "---" + (startOrEnd ? "start" : "end") + "---" + timeStamp;
        }
    }
}
