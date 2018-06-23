package com.example.xyzreader.handler;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * Created by Antonio Vitiello on 23/06/2018.
 */
public class WorkerHandler extends HandlerThread {
    private static WorkerHandler sInstance = new WorkerHandler();
    public static final int MSG_ID = 0;
    private Handler mWorkerHandler;
    private Handler mResponseHandler;


    public static void handle(Worker worker) {
        if (sInstance == null) {
            sInstance = new WorkerHandler();
        }
        sInstance.enqueue(worker);
    }

    private WorkerHandler() {
        super(WorkerHandler.class.getSimpleName());
        super.start();
        mWorkerHandler = new Handler(getLooper(), mHandlerCallback);
        mResponseHandler = new Handler();
        //   Same as:
        // mResponseHandler = new Handler(Looper.getMainLooper());
    }

    public void enqueue(Worker worker) {
        Message message = mWorkerHandler.obtainMessage(MSG_ID, worker);
        message.sendToTarget();
    }

    private Handler.Callback mHandlerCallback = msg -> {
        //process the request in a separate thread
        Worker worker = (Worker) msg.obj;
        worker.inBackground();

        //Post result in main.thread
        mResponseHandler.post(() -> worker.onMainThread());

        return true;
    };

    public static void quitNow(){
        if(sInstance != null){
            sInstance.quit();
        }
    }


    public interface OnReady {
        void onReady(Worker worker);
    }

}
