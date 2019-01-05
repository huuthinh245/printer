package com.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class QSService extends Service {

    //�����ӿ�
    private Binder iQSpda=new IQSPDAImpl();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iQSpda;
    }

}
