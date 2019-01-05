package com.qs408;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;
import android.zyapi.CommonApi;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.wiget.BarcodeCreater;

import java.io.UnsupportedEncodingException;

public class PrintModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static String mCurDev1 = "";

    private static int mComFd = -1;
    static CommonApi mCommonApi;


    public static boolean isCanprint = false;

    public static boolean isCanSend = true;

    public static boolean temHigh = false;

    private final int MAX_RECV_BUF_SIZE = 1024;
    private boolean isOpen = false;
    private MediaPlayer player;
    private final static int SHOW_RECV_DATA = 1;
    private byte[] recv;
    private String strRead;
    public static boolean isScanDomn = false;
    // GreenOnReceiver greenOnReceiver;
    private static String pin_1 = "55";// 一维
    private static String pin_2 = "56";// 二维
    private Context context;
    public static StringBuffer sb1 = new StringBuffer();

    // SCAN按键监听
    private ScanBroadcastReceiver scanBroadcastReceiver;
    // MBroadcastReceiver mBroadcastReceiver;

    Handler h;

    public PrintModule(ReactApplicationContext reactContext) {
        super(reactContext);
        context=reactContext;
    }

    @Override
    public String getName() {
        return "PrintModule";
    }

    @ReactMethod
    private void init() {
        mCommonApi = new CommonApi();
        //5501为MT1,408为 MT3
        mComFd = mCommonApi.openCom("/dev/ttyMT3", 115200, 8, 'N', 1);

        if (mComFd > 0) {
            Toast.makeText(getReactApplicationContext(), "init success", Toast.LENGTH_SHORT).show();
        }
//        mBroadcastReceiver = new MBroadcastReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("NOPAPER");
//        context.registerReceiver(mBroadcastReceiver, intentFilter);
    }
    @ReactMethod
    public void printText(int size,int align,String text){
        switch (align) {
            case 0:
                send(new byte[] { 0x1b, 0x61, 0x00 });
                break;
            case 1:
                send(new byte[] { 0x1b, 0x61, 0x01 });
                break;
            case 2:
                send(new byte[] { 0x1b, 0x61, 0x02 });
                break;

            default:
                break;
        }
        switch (size) {
            case 1:
                send(new byte[] { 0x1D, 0x21, 0x00 });
                break;
            case 2:
                send(new byte[] { 0x1D, 0x12, 0x11 });
                break;

            default:
                break;
        }
        //打印
        send(new byte[]{0x1B,0x23,0x23,0x53,0x4C,0x41,0x4E,0x29});
        try {
            send((text+"\n").getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @ReactMethod
    public  void printBitmap(String text){

        StringBuffer sb=new StringBuffer();
//        sb.append("CÔNG TY CỔ PHẦN ĐẦU TƯ \n");
//        sb.append("HẠ TẦNG QUẢNG NAM\n");
//        sb.append("90 Phan Bội Châu Tam Kỳ QN.VN\t\t\t\t\t\t\t\t\t\t\n");
//        sb.append("Điện thoại: "+ "0235355 411\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n");
//        sb.append("Ký hiêu："+text+"\t\t\t\t\t MST："+"432434\t\t\t\t\t\n");
//        sb.append("VÉ XE BUÝT THEO LƯỢT \n");
//        sb.append("NV: ĐÀO CÔNG DANH\n");
//        sb.append("GIÁ: 5000 vnd/Lượt\n");
//        sb.append("Giá vé đã bao gồm bảo hiểm khách hang \n");
//        sb.append("16/11/2017\t\t 17:32:11 \n");
//        sb.append(" IN TẠI: CÔNG TY CỔ PHẦN ĐẦU TƯ HẠ TẦNG QUẢNG NAM \n\n\n\n");
        sb.append("CÔNG TY CỔ PHẦN ĐẦU TƯ HẠ TẦNG QUẢNG NAM\n");
        sb.append("90 Phan Bội Châu, P.Tân Thạnh, Tam Kỳ, Quảng Nam\n");
        sb.append("ĐT: "+ "02353.555.111\t\t\t\t\t\tMST："+"4000806573 \n");
        sb.append(("Mẫu số："+"01VEDB2/005"+"\t\t\t\t\t Ký hiệu："+"QN/18T\t\t\t\t\t\n"));
        sb.append("Số vé: 0029813 \n");
        sb.append("\t\t\t\t\tVÉ XE BUÝT THEO LƯỢT \n");
        sb.append("\t\t\t\t\t (Liên 2: Giao cho khách hàng)\n");
        sb.append("Tuyến số: 33\t\t Trạm: Tam Kỳ\n");
        sb.append("NV: ĐÀO CÔNG DANH\n");
        sb.append("Giá vé: 5000 vnd/Lượt\n");
        sb.append("Giá vé đã bao gồm bảo hiểm khách hàng \n");
        sb.append("16/11/2017\t\t 17:32:11 \n");
        sb.append(" IN TẠI: CÔNG TY CỔ PHẦN ĐẦU TƯ HẠ TẦNG QUẢNG NAM \n\n\n\n");
        Bitmap bitmap = textAsBitmap(sb.toString(), 23.0f);
        byte[] b=draw2PxPoint(bitmap);
        send(b);
    }
    @ReactMethod
    public void printBill(String a) throws UnsupportedEncodingException {
        Log.d("mComFd", String.valueOf(mComFd));
        StringBuffer sb=new StringBuffer();
        sb.append("\n giá："+a+"\n");
        sb.append(" nhà："+a+"\n");
        sb.append(" địa chỉ \t："+a+"\n时间："+a+"\n");
        sb.append(" nhà đất："+a+"\n");
        sb.append(" điện thoại："+a+"\n");
        sb.append(" acd："+a+"\n\n\n\n\n\n");

        // send(new byte[] { 0x1b, 0x61, 0x00 });
        send(new byte[]{0x1B,0x23,0x23,0x53,0x4C,0x41,0x4E,0x29});
        send((sb.toString()+"\n").getBytes("UTF-8"));
        // printText(sb.toString());
    }
    @ReactMethod
    public  void printBarCode(int align, int width, int height, String data){
        switch (align) {
            case 0:
                send(new byte[] { 0x1b, 0x61, 0x00 });
                break;
            case 1:
                send(new byte[] { 0x1b, 0x61, 0x01 });
                break;
            case 2:
                send(new byte[] { 0x1b, 0x61, 0x02 });
                break;

            default:
                break;
        }

        Bitmap mBitmap = BarcodeCreater.creatBarcode(getReactApplicationContext(),
                data, width, height, true, 1);
        byte[] printData = draw2PxPoint1(mBitmap);
        send(printData);
    }

    @ReactMethod
    public void printWithMergeBitMap(String company, String address, String phone,
                                     String mst, String ms, String kh, String sv,String kv, String ts,
                                     String tram, String nv, String gia, String ngay){
        Log.d("print", "printWithMergeBitMap");
        String str1 = company;
        String str5 = kv.toUpperCase();
        String date = "\tIn ngày: " + ngay;
        StringBuffer sb = new StringBuffer();
        sb.append("\t"+address+"\n");
        sb.append("\tĐT: "+ phone +"\t\t\t\t\t\tMST："+ mst +"\n" );
        sb.append(("\tMẫu số："+ms+"\t\t\t\t\t Ký hiệu："+kh+"\t\t\t\t\t\n"));
        sb.append("\tSố vé: "+sv+"\t");
        String merge_str = sb.toString();

        StringBuffer sb1 = new StringBuffer();
        // sb1.append("\t\t\t\t\t\t (Liên 2: Giao cho khách hàng)\n");
        sb1.append("\t"+"Tuyến số: "+ts+"\t\t\tTrạm: " + tram +"\n");
        sb1.append("\tNV: "+nv+"\n");
        // sb1.append(" Trạm: " + tram +"\n");
        sb1.append("\tGiá vé: "+gia+ " VNĐ/Lượt");
        // sb1.append("\t(Giá vé đã bao gồm bảo hiểm hành khách)");
        String merge_str1= sb1.toString();

        StringBuffer sb2 = new StringBuffer();
        sb2.append("\tIN TẠI: "+company+"\n\n\n");

        String merge_str2= sb2.toString();


        StringBuffer sup1 = new StringBuffer();
        sup1.append("(Liên 2: Giao cho khách hàng)");
        Bitmap btm_sup1 = textAsBitmap2(sup1.toString(), 550, 20);

        StringBuffer sup2 = new StringBuffer();
        sup2.append("\t(Giá vé đã bao gồm bảo hiểm hành khách)");
        Bitmap btm_sup2 = textAsBitmap2(sup2.toString(), 550, 20);

        Bitmap btm_name1 = textAsBitmap2(str1.toUpperCase(), 550, 24);
        Bitmap btm_name2 = textAsBitmap1(merge_str, 550, 20);
        Bitmap btm_name1_2 = twoBtmap2One(btm_name1, btm_name2);




        Bitmap btm_name3 = textAsBitmap2(str5, 550, 30);
        Bitmap btm_merge_sup1 = twoBtmap2One(btm_name3,  btm_sup1);
        Bitmap str_bitmap = twoBtmap2One(btm_name1_2, btm_merge_sup1);

        Bitmap btm_merge_str1 = textAsBitmap1(merge_str1, 550, 26);
        Bitmap btm_merge_sup2 = twoBtmap2One(btm_merge_str1,  btm_sup2);
        Bitmap btm_merge_str2 = twoBtmap2One(str_bitmap, btm_merge_sup2);

        Bitmap btm_sb3 = textAsBitmap1(date, 550, 25);
        Bitmap btm_merge_str3 = twoBtmap2One(btm_merge_str2, btm_sb3);

        Bitmap btm_name4 = textAsBitmap1(merge_str2, 550, 20);
        Bitmap btm_merge_str4 = twoBtmap2One(btm_merge_str3, btm_name4);
        str_bitmap = newBitmap(btm_merge_str4);
        byte[] b=draw2PxPoint(str_bitmap);
        send(b);

    }

    private Bitmap newBitmap(Bitmap bit1) {
        int width = bit1.getWidth();
        int height = bit1.getHeight();
        // ??????????Bitmap(???????),???????????????????????????????????
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // ??bitmap???�?????????,??????????????????????????
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bit1, 0, 0, null);
        // ??canvas????????????????
        // setBitmapBorder(canvas);
        return bitmap;
    }

    @Override
    public void onHostResume() {

    }


    @Override
    public void onHostPause() {

    }


    @Override
    public void onHostDestroy() {
        mCommonApi.setGpioOut(58, 0);
        mCommonApi.closeCom(mComFd);
        // context.unregisterReceiver(mBroadcastReceiver);
    }

    class ScanBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // openScan();
        }
    }

    public static void send(byte[] data) {
        if (data == null)
            return;
        if (mComFd > 0) {
            int a = mCommonApi.writeCom(mComFd, data, data.length);
            Log.d("status print",String.valueOf(a));
        }

    }

    /**
     * 文字转图片
     * @param text 将要生成图片的内容
     * @param textSize 文字大小
     * @return
     */
    public static Bitmap textAsBitmap(String text, float textSize) {

//        sb.append(" nhà："+text+"\n");
//        sb.append(" địa chỉ \t："+text+"\t\t\t mst："+text+"\n");
//        sb.append(" nhà đất："+text+"\n");
//        sb.append(" điện thoại："+text+"\n");
//        sb.append(" acd："+text+"\n\n\n\n\n\n");
        String a= "adsa";
        TextPaint textPaint = new TextPaint();
        TextPaint textPaint1 = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint1.setTextSize(25);
        textPaint.setTextSize(textSize);

        StaticLayout layout = new StaticLayout(text, textPaint, 550,
                Layout.Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth() + 20,
                layout.getHeight() + 20, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 50, bitmap.getHeight(),null );
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);

        layout.draw(canvas);
        Log.d("textAsBitmap",
                String.format("1:%d %d", layout.getWidth(), layout.getHeight()));
        return bitmap;
    }

    public Bitmap twoBtmap2One1(Bitmap bitmap1, Bitmap bitmap2) {
        Bitmap bitmap3 = Bitmap.createBitmap(
                bitmap1.getWidth() + bitmap2.getWidth(), bitmap2.getHeight(),
                bitmap1.getConfig());
        Canvas canvas = new Canvas(bitmap3);
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        canvas.drawBitmap(bitmap2, bitmap1.getWidth(), 0, null);
        return bitmap3;
    }
    public Bitmap twoBtmap2One(Bitmap bitmap1, Bitmap bitmap2) {
        Bitmap bitmap3 = Bitmap.createBitmap(bitmap1.getWidth(),
                bitmap1.getHeight() + bitmap2.getHeight(), bitmap1.getConfig());
        Canvas canvas = new Canvas(bitmap3);
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        canvas.drawBitmap(bitmap2, 0, bitmap1.getHeight(), null);
        return bitmap3;
    }
    public static Bitmap textAsBitmap1(String text, int width, float textSize) {

        TextPaint textPaint = new TextPaint();

        textPaint.setColor(Color.BLACK);

        textPaint.setTextSize(textSize);

        StaticLayout layout = new StaticLayout(text, textPaint, width,
                Layout.Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth(),
                layout.getHeight() +5, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);

        layout.draw(canvas);
        return bitmap;

    }

    public static Bitmap textAsBitmap2(String text, int width, float textSize) {

        TextPaint textPaint = new TextPaint();

        textPaint.setColor(Color.BLACK);

        textPaint.setTextSize(textSize);

        StaticLayout layout = new StaticLayout(text, textPaint, width,
                Layout.Alignment.ALIGN_CENTER, 1.3f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth(),
                layout.getHeight() +7, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);

        layout.draw(canvas);
        return bitmap;

    }



    public static byte[] draw2PxPoint(Bitmap bmp) {
        // 用来存储转换后的 bitmap 数据。为什么要再加1000，这是为了应对当图片高度无法
        // 整除24时的情况。比如bitmap 分辨率为 240 * 250，占用 7500 byte，
        // 但是实际上要存储11行数据，每一行需要 24 * 240 / 8 =720byte 的空间。再加上一些指令存储的开销，
        // 所以多申请 1000byte 的空间是稳妥的，不然运行时会抛出数组访问越界的异常。
        int size = bmp.getWidth() * bmp.getHeight() / 8 + 2200;
        byte[] data = new byte[size];
        int k = 0;
        // 设置行距为0的指令
        data[k++] = 0x1B;
        data[k++] = 0x33;
        data[k++] = 0x00;
        // 逐行打印
        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
            // 打印图片的指令
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33;
            data[k++] = (byte) (bmp.getWidth() % 256); // nL
            data[k++] = (byte) (bmp.getWidth() / 256); // nH
            // 对于每一行，逐列打印
            for (int i = 0; i < bmp.getWidth(); i++) {
                // 每一列24个像素点，分为3个字节存储
                for (int m = 0; m < 3; m++) {
                    // 每个字节表示8个像素点，0表示白色，1表示黑色
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
                        if(k<size){
                            data[k] += data[k] + b;
                        }
//						data[k] = (byte) (data[k]+ data[k] + b);
                    }
                    k++;
                }
            }
            if(k<size){
                data[k++] = 10;// 换行
            }
        }
        return data;
    }
    public static byte px2Byte(int x, int y, Bitmap bit) {
        if (x < bit.getWidth() && y < bit.getHeight()) {
            byte b;
            int pixel = bit.getPixel(x, y);
            int red = (pixel & 0x00ff0000) >> 16; // 取高两位
            int green = (pixel & 0x0000ff00) >> 8; // 取中两位
            int blue = pixel & 0x000000ff; // 取低两位
            int gray = RGB2Gray(red, green, blue);
            if (gray < 128) {
                b = 1;
            } else {
                b = 0;
            }
            return b;
        }
        return 0;
    }

    private static int RGB2Gray(int r, int g, int b) {
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b); // 灰度转化公式
        return gray;
    }

    public static byte[] draw2PxPoint1(Bitmap bmp) {
        // 用来存储转换后的 bitmap 数据。为什么要再加1000，这是为了应对当图片高度无法
        // 整除24时的情况。比如bitmap 分辨率为 240 * 250，占用 7500 byte，
        // 但是实际上要存储11行数据，每一行需要 24 * 240 / 8 =720byte 的空间。再加上一些指令存储的开销，
        // 所以多申请 1000byte 的空间是稳妥的，不然运行时会抛出数组访问越界的异常。
        int size = bmp.getWidth() * bmp.getHeight() / 8 + 1600;
        byte[] data = new byte[size];
        int k = 0;
        // 设置行距为0的指令
        data[k++] = 0x1B;
        data[k++] = 0x33;
        data[k++] = 0x00;
        // 逐行打印
        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
            // 打印图片的指令
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33;
            data[k++] = (byte) (bmp.getWidth() % 256); // nL
            data[k++] = (byte) (bmp.getWidth() / 256); // nH
            // 对于每一行，逐列打印
            for (int i = 0; i < bmp.getWidth(); i++) {
                // 每一列24个像素点，分为3个字节存储
                for (int m = 0; m < 3; m++) {
                    // 每个字节表示8个像素点，0表示白色，1表示黑色
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
                        if(k<size){
                            data[k] += data[k] + b;
                        }
//						data[k] = (byte) (data[k]+ data[k] + b);
                    }
                    k++;
                }
            }
            if(k<size){
                data[k++] = 10;// 换行
            }
        }
        return data;
    }
    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // ?????????
        int width = bm.getWidth();
        int height = bm.getHeight();
        // ???????????
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // ???????????matrix????
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // ?�??�???
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        return newbm;
    }

//    class MBroadcastReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // TODO Auto-generated method stub
//            Log.d("no paper", "no paper");
//
//        }
//    }
}

