// IQSService.aidl
package com.qs;
import android.graphics.Bitmap;
// Declare any non-default types here with import statements

interface IQSService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void openScan();

    void printText(int size,int align,String text);

    void printBitmap(int align,in Bitmap bitmap);

    void printBarCode(int align, int width, int height, String data);

    void printQRCode(int width, int height,String data);

    void sendCMD(in byte[] list);

}
