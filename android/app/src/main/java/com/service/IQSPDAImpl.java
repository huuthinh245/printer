package com.service;

import android.graphics.Bitmap;
import android.os.RemoteException;

import com.qs.IQSService;

public class IQSPDAImpl extends IQSService.Stub{


    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    @Override
    public void openScan() throws RemoteException {

    }

    @Override
    public void printText(int size, int align, String text) throws RemoteException {

    }

    @Override
    public void printBitmap(int align, Bitmap bitmap) throws RemoteException {

    }

    @Override
    public void printBarCode(int align, int width, int height, String data) throws RemoteException {

    }

    @Override
    public void printQRCode(int width, int height, String data) throws RemoteException {

    }

    @Override
    public void sendCMD(byte[] list) throws RemoteException {

    }
}
