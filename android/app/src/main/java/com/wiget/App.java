package com.wiget;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import android.zyapi.CommonApi;
import com.facebook.react.ReactNativeHost;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App extends Application {

    private static String mCurDev1 = "";

    private static int mComFd = -1;
    static CommonApi mCommonApi;

    static App instance = null;

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

    public static StringBuffer sb1 = new StringBuffer();

    // SCAN按键监听
    private ScanBroadcastReceiver scanBroadcastReceiver;

    Handler h;

    public App() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        // mDb = Database.getInstance(this);
        // 初始
        init();

        // 实例化MediaPlayer
        // player = MediaPlayer.create(getApplicationContext(), R.raw.beep);
    }

    public  void init() {

        initGPIO();
        openGPIO();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mComFd > 0) {
                    open();
                    isOpen = true;
                    readData();
//					Toast.makeText(getApplicationContext(), "扫描头初始化中", 0).show();
                    //默认开启黑标
                    App.send(new byte[] { 0x1F, 0x1B, 0x1F, (byte) 0x80, 0x04,
                            0x05, 0x06, 0x66 });

//////						 //调节打印浓度为39，十六进制为0x27
//						App.send(new byte[] { 0x1b, 0x23, 0x23, 0x53,0x54, 0x44, 0x50,
//								 0x27});
                } else {
                    isOpen = false;
                }
            }
        }, 2000);


        // 利用Handler更新UI
        h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    if (msg.obj != null) {
                        String str ="" + msg.obj;
                        if(str.trim().equals("start")){

                        }else{;
//						tv.setText(str);

                            if(str.length()<=0){
                            }else if(str.contains("")){
                            }else{

                                Intent intentBroadcast = new Intent();
//						 Intent intentBroadcast1 = new Intent();
                                intentBroadcast.setAction("com.qs.scancode");
//						 intentBroadcast1.setAction("com.zkc.scancode");

                                intentBroadcast.putExtra("code", str.trim());
//						 intentBroadcast1.putExtra("code", str.trim());

                                Log.e("", "扫描信息：" + str);
                                sendBroadcast(intentBroadcast);

                                player.start();

                            }

                        }
                    }
                }
            }
        };

        scanBroadcastReceiver = new ScanBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ismart.intent.scandown");
        this.registerReceiver(scanBroadcastReceiver, intentFilter);
    }

    /**
     * 读数据线程
     */
    private void readData() {
        new Thread() {
            public void run() {
                while (isOpen) {
                    int ret = 0;
                    byte[] buf = new byte[MAX_RECV_BUF_SIZE + 1];
                    ret = mCommonApi.readComEx(mComFd, buf, MAX_RECV_BUF_SIZE,
                            0, 0);
                    if (ret <= 0) {
                        Log.d("", "read failed!!!! ret:" + ret);
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        continue;
                    } else {
                        Log.e("", "1read success:");
                    }
                    recv = new byte[ret];
                    System.arraycopy(buf, 0, recv, 0, ret);

                    try {
                        strRead = new String(recv, "GBK");
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    StringBuffer sb = new StringBuffer();
                    for(int i = 0;i<recv.length;i++){
                        if(recv[i]==0x0D){
                            sb.append("\n");
                        }else{
                            sb.append((char)recv[i]);
                        }
                    }

                    String s = sb.toString();
                    if (strRead != null) {
                        Message msg = handler.obtainMessage(SHOW_RECV_DATA);
                        msg.obj = s;
                        msg.sendToTarget();
                    }
                }
            }
        }.start();
    }

    boolean iscanScan = false;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_RECV_DATA:
                    String barCodeStr1 = (String) msg.obj;
                    if (barCodeStr1.trim() != "") {
                        if (isOpen) {
                            if (!barCodeStr1.trim().contains("##55")) {
                                if(!barCodeStr1.trim().equals("start")){

//							Message m = new Message();
//							m.what = 0x123;
//							m.obj=barCodeStr1;
//							h.sendMessage(m);

                                    sb1.append(barCodeStr1);

                                    num=1;
                                    mHanlder.removeCallbacks(run_getData);
                                    mHanlder.post(run_getData);

                                }
                            }
                        }
                    }
                    break;
            }
        };
    };

    int num=1;
    Handler mHanlder=new Handler();
    Runnable run_getData=new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if(num>1){
                num=1;
                mHanlder.removeCallbacks(run_getData);
                Message m = new Message();
                m.what = 0x123;
                Log.e("iiiiiii", "发送GET请求");
                try {
                    m.obj=sb1.toString();
                    Log.e("返回信息：", "" + m.obj);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                h.sendMessage(m);
            }else{
                num++;
                mHanlder.postDelayed(run_getData, 50);
            }
        }
    };

    // 进入App拉高55和56脚
    public static void open() {
        /**
         * 1、拉高55,56脚电平(APP->Printer) 1B 23 23 XXXX 其中XXXX为ASCII码:56UP 即1B 23 23
         * 35 36 55 50 单片机收到拉高55,56脚电平
         */
        // 进来就拉高55和56脚
        App.send(new byte[] { 0x1B, 0x23, 0x23, 0x35, 0x36, 0x55, 0x50 });

    }

    // 执行扫描，即拉低74、75脚后再拉高
    public static  void openScan() {
        /**
         * 3、拉低55脚电平(APP->Printer) 1B 23 23 XXXX 其中XXXX为ASCII码:55DN 即1B 23 23 35
         * 35 44 4E
         */
        // 进来就拉高55和56脚
        // App.send(new byte[]{0x1B,0x23,0x23,0x35,0x36,0x55,0x50});
        // 扫描时候拉低55脚
//		App.send(new byte[] { 0x1B, 0x23, 0x23, 0x35, 0x35, 0x44, 0x4E });

        //发送指令
        App.send(new byte[] { 0x1B, 0x23, 0x23, 0x35, 0x35, 0x44, 0x4E});

        //清空数据
        sb1.setLength(0);

        //拉低GPIO口
//		mCommonApi.setGpioDir(74, 1);
//		mCommonApi.setGpioOut(74, 0);
//		mCommonApi.setGpioDir(75, 1);
//		mCommonApi.setGpioOut(75, 0);
//
//		new Handler().postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				//拉低GPIO口，点亮扫描头
//				mCommonApi.setGpioDir(74, 1);
//				mCommonApi.setGpioOut(74, 1);
//				mCommonApi.setGpioDir(75, 1);
//				mCommonApi.setGpioOut(75, 1);
//
//			}
//		}, 200);
//
    }

    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public String getCurDevice() {
        return mCurDev1;
    }

    public static void setCurDevice(String mCurDev) {
        mCurDev1 = mCurDev;
    }

    // 其他地方引用mCommonApi变量
    public static CommonApi getCommonApi() {
        return mCommonApi;
    }

    public static void initGPIO() {
        // TODO Auto-generated method stub
        mCommonApi = new CommonApi();
        //5501为MT1,408为 MT3
        mComFd = mCommonApi.openCom("/dev/ttyMT3", 115200, 8, 'N', 1);

        if (mComFd > 0) {
            Toast.makeText(instance, "init success", 0).show();
        }
    }

    public static void openGPIO() {

        mCommonApi.setGpioDir(58, 0);
        mCommonApi.getGpioIn(58);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

                mCommonApi.setGpioDir(58, 1);
                mCommonApi.setGpioOut(58, 1);

                // 黑标临界电压
//				App.send(new byte[] { 0x1b, 0x23, 0x23, 0x53, 0x42, 0x43, 0x56,
//						0x14, 0x05 });

            }
        }, 1000);

    }

    Handler mHandler = new Handler();
    Runnable mRun = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            isCanSend = true;
        }
    };


    public String byteToString(byte[] b, int size) {
        byte high, low;
        byte maskHigh = (byte) 0xf0;
        byte maskLow = 0x0f;

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < size; i++) {
            high = (byte) ((b[i] & maskHigh) >> 4);
            low = (byte) (b[i] & maskLow);
            buf.append(findHex(high));
            buf.append(findHex(low));
            buf.append(" ");
        }
        return buf.toString();
    }

    private char findHex(byte b) {
        int t = new Byte(b).intValue();
        t = t < 0 ? t + 16 : t;
        if ((0 <= t) && (t <= 9)) {
            return (char) (t + '0');
        }
        return (char) (t - 10 + 'A');
    }

    /**
     * 查看一个字符串是否可以转换为数字
     *
     * @param str
     *            字符串
     * @return true 可以; false 不可以
     */
    public static boolean isStr2Num(String str) {
        Pattern pattern = Pattern.compile("^[0-9]*$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 发送数据
     */
    public static void send(byte[] data) {
        if (data == null)
            return;
        if (mComFd > 0) {
            mCommonApi.writeCom(mComFd, data, data.length);
        }
    }

    private static boolean isMessyCode(String strName) {
        try {
            Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
            Matcher m = p.matcher(strName);
            String after = m.replaceAll("");
            String temp = after.replaceAll("\\p{P}", "");
            char[] ch = temp.trim().toCharArray();

            int length = (ch != null) ? ch.length : 0;
            for (int i = 0; i < length; i++) {
                char c = ch[i];
                if (!Character.isLetterOrDigit(c)) {
                    String str = "" + ch[i];
                    if (!str.matches("[\u4e00-\u9fa5]+")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    public String deleteErr(String str_VarMboxRead) {

        String b = str_VarMboxRead.replace("�", "");
        b = b.replace("", "");

        return b.trim();
    }

    public static void closeCommonApi() {
        mCommonApi.setGpioOut(58, 0);
        mCommonApi.closeCom(mComFd);
        Toast.makeText(instance, "退出应用", Toast.LENGTH_SHORT).show();
    }

    /**
     * 打印文字
     *
     */
    public static void printText(int size,int align,String text){
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
        try {
            send((text+"\n").getBytes("GBK"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 打印图片
     * @param align
     * @param bitmap
     */
    public static void printBitmap(int align,Bitmap bitmap){
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

        byte[] b=draw2PxPoint(bitmap);

        send(b);

    }

    /**
     * 打印一维码
     * @param align
     * @param width
     * @param height
     * @param data
     */
    public static void printBarCode(int align, int width, int height, String data){
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

        Bitmap mBitmap = BarcodeCreater.creatBarcode(getInstance(),
                data, width, height, true, 1);
        byte[] printData = draw2PxPoint1(mBitmap);
        send(printData);
    }

    /**
     * 打印二维码
     * @param align
     //* @param width
     * @param height
     * @param data
     */
    public static void printQRCode(int align, int height, String data){
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

        Bitmap mBitmap = BarcodeCreater.encode2dAsBitmap(data, height,
                height, 2);
        byte[] printData1 = draw2PxPoint(mBitmap);
        send(printData1);

    }

    // SCAN按键的监听
    class ScanBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            openScan();
        }
    }
    /*************************************************************************
     * 假设一个240*240的图片，分辨率设为24, 共分10行打印 每一行,是一个 240*24 的点阵, 每一列有24个点,存储在3个byte里面。
     * 每个byte存储8个像素点信息。因为只有黑白两色，所以对应为1的位是黑色，对应为0的位是白色
     **************************************************************************/
    /**
     * 把一张Bitmap图片转化为打印机可以打印的字节流
     *
     * @param bmp
     * @return
     */
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


    /*************************************************************************
     * 假设一个240*240的图片，分辨率设为24, 共分10行打印 每一行,是一个 240*24 的点阵, 每一列有24个点,存储在3个byte里面。
     * 每个byte存储8个像素点信息。因为只有黑白两色，所以对应为1的位是黑色，对应为0的位是白色
     **************************************************************************/
    /**
     * 把一张Bitmap图片转化为打印机可以打印的字节流
     *
     * @param bmp
     * @return
     */
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

    /**
     * 灰度图片黑白化，黑色是1，白色是0
     *
     * @param x
     *            横坐标
     * @param y
     *            纵坐标
     * @param bit
     *            位图
     * @return
     */
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

    /**
     * 图片灰度的转化
     */
    private static int RGB2Gray(int r, int g, int b) {
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b); // 灰度转化公式
        return gray;
    }

}