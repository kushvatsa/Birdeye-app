package com.kushvatsa.birdeye.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

public class BirdEyeIntentService extends IntentService {

    public BirdEyeIntentService()
    { super("BirdEyeIntentService");

    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    BirdEyeSyncTask.syncCustData(this);
    }
}
