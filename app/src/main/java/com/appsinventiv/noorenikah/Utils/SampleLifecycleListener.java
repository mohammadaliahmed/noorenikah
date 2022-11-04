package com.appsinventiv.noorenikah.Utils;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class SampleLifecycleListener  implements LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        CommonUtils.sendCustomerStatus("Online");

    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onDestroy() {

        CommonUtils.sendCustomerStatus("" + System.currentTimeMillis());


    }
}