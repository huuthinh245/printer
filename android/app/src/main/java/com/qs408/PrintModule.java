package com.qs408;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;
import android.zyapi.CommonApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
//import com.wiget.BarcodeCreater;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;



public class PrintModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static String mCurDev1 = "";
    private static int mComFd = -1;
    static CommonApi mCommonApi;
    private static  final Layout.Alignment BOLD_NORMAL = Layout.Alignment.ALIGN_NORMAL;
    private static  final Layout.Alignment BOLD_CENTER = Layout.Alignment.ALIGN_CENTER;

    public static boolean isCanprint = false;
    public static boolean isSuccess = false;
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
    MBroadcastReceiver mBroadcastReceiver;
    PrintBroadcastReceiver printBroadcastReceiver;

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
    private void init(){
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
                    // Toast.makeText(getApplicationContext(), "扫描头初始化中",
                    // 0).show();
                    // 默认开启黑标
                    send(new byte[] { 0x1F, 0x1B, 0x1F, (byte) 0x80, 0x04,
                            0x05, 0x06, 0x66 });

                    // //// //调节打印浓度为39，十六进制为0x27
                    // App.send(new byte[] { 0x1b, 0x23, 0x23, 0x53,0x54, 0x44,
                    // 0x50,
                    // 0x27});
                } else {
                    isOpen = false;
                }
            }
        }, 2000);
        mBroadcastReceiver = new MBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("NOPAPER");
        context.registerReceiver(mBroadcastReceiver, intentFilter);

        printBroadcastReceiver = new PrintBroadcastReceiver();
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction("PRINTSUCCESS");
        context.registerReceiver(printBroadcastReceiver, intentFilter1);
    }

    public static void open() {
        /**
         * 1、拉高55,56脚电平(APP->Printer) 1B 23 23 XXXX 其中XXXX为ASCII码:56UP 即1B 23 23
         * 35 36 55 50 单片机收到拉高55,56脚电平
         */
        // 进来就拉高55和56脚
        send(new byte[] { 0x1B, 0x23, 0x23, 0x35, 0x36, 0x55, 0x50 });

    }

    public  void initGPIO() {
        mCommonApi = new CommonApi();
        //5501为MT1,408为 MT3
        mComFd = mCommonApi.openCom("/dev/ttyMT3", 115200, 8, 'N', 1);
        if (mComFd > 0) {
            isOpen = true;
            Toast.makeText(getReactApplicationContext(), "init success", Toast.LENGTH_SHORT).show();
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
                // App.send(new byte[] { 0x1b, 0x23, 0x23, 0x53, 0x42, 0x43,
                // 0x56,
                // 0x14, 0x05 });

            }
        }, 1000);

    }


    @ReactMethod
    public void printTicket(String company, String address, String phone,
                                     String mst, String ms, String kh, String sv,String kv, String ts,
                                     String tram, String nv, String gia, String ngay){
        String str1 = company;
        String str5 = kv.toUpperCase();
        String date = "In ngày: " + ngay;
        StringBuffer sb = new StringBuffer();
        sb.append(address+"\n");
        sb.append("ĐT: "+ phone +"\t\t\t\t\t\t\t MST："+ mst +"\n" );
        sb.append(("Mẫu số："+ms+"\t\t\t\t\t\t Ký hiệu："+kh+"\t\t\t\t\t\n"));
        sb.append("Số vé: "+sv+"\t");
        String merge_str = sb.toString();

        StringBuffer sb1 = new StringBuffer();
        // sb1.append("\t\t\t\t\t\t (Liên 2: Giao cho khách hàng)\n");
        sb1.append("Tuyến số: "+ts+"\t\t\tTrạm: " + tram +"\n");
        sb1.append("NV: "+nv+"\n");
        // sb1.append(" Trạm: " + tram +"\n");
        sb1.append("Giá vé: "+gia+ " VNĐ/Lượt");
        // sb1.append("\t(Giá vé đã bao gồm bảo hiểm hành khách)");
        String merge_str1= sb1.toString();

        StringBuffer sb2 = new StringBuffer();
        sb2.append("In tại: "+company+"\n");
        sb2.append("MST: "+mst +"\n");

        String merge_str2= sb2.toString();


        StringBuffer sup1 = new StringBuffer();
        sup1.append("(Liên 2: Giao cho khách hàng)");
        Bitmap btm_sup1 = textAsBitmap2(sup1.toString(), 600, 20);

        StringBuffer sup2 = new StringBuffer();
        sup2.append("\t(Giá vé đã bao gồm bảo hiểm hành khách)");
        Bitmap btm_sup2 = textAsBitmap2(sup2.toString(), 600, 20);

        Bitmap btm_name1 = textAsBitmap1(str1.toUpperCase(), 600, 22);
        Bitmap btm_name2 = textAsBitmap1(merge_str, 600, 20);
        Bitmap btm_name1_2 = twoBtmap2One(btm_name1, btm_name2);




        Bitmap btm_name3 = textAsBitmapBold(str5, 600, 30, BOLD_CENTER);
        Bitmap btm_merge_sup1 = twoBtmap2One(btm_name3,  btm_sup1);
        Bitmap str_bitmap = twoBtmap2One(btm_name1_2, btm_merge_sup1);

        Bitmap btm_merge_str1 = textAsBitmap1(merge_str1, 600, 26);
        Bitmap btm_merge_sup2 = twoBtmap2One(btm_merge_str1,  btm_sup2);
        Bitmap btm_merge_str2 = twoBtmap2One(str_bitmap, btm_merge_sup2);

        Bitmap btm_sb3 = textAsBitmap1(date, 600, 25);
        Bitmap btm_merge_str3 = twoBtmap2One(btm_merge_str2, btm_sb3);

        Bitmap btm_name4 = textAsBitmap1(merge_str2, 600, 18);
        Bitmap btm_merge_str4 = twoBtmap2One(btm_merge_str3, btm_name4);
        str_bitmap = newBitmap(btm_merge_str4);
        final  byte[] b=draw2PxPoint(str_bitmap);
        send(new byte[]{0x1D,0x23,0x53,(byte)0xD1,0x7A,(byte)0xF8,0x4d});
        send(new byte[] { 0x1d, 0x61, 0x00 });
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (isCanprint) {
                    send(b);
                    send(new byte[]{0x1d,0x0c});

                    //��ӡ5�����з���˺ֽ������ֽ���ж�ʹ�ÿ��У�
                    send(new byte[] { 0x0a, 0x0a,0x0a,0x0a});
                    send(new byte[]{0x1D, 0x23, 0x45});
                    // promise.resolve(isSuccess);

                }
            }
        }, 500);

    }
    @ReactMethod
    public void printTotal(
            String company, String address, String phone,
            String mst, String kh,
            String nvBv, String nvLx, String sx,
            String napthe, String quetthe, String totals, String ticket, String timeDn,
            String hours, String day, final Promise promise
    ) throws JSONException {
//        String a = "[{ id: 24000, name: 24 }, { id: 2500, name: 24 }]";
        StringBuffer sbBigTt = new StringBuffer();
        sbBigTt.append("TỔNG KẾT");

        StringBuffer sbTitle = new StringBuffer();
        sbTitle.append(company+ "\n");
        sbTitle.append(address+"\n");
        sbTitle.append("ĐT: "+ phone +"\n");
        sbTitle.append("Ký hiệu："+kh+"\t\t\t\t\t\t\t" +"MST："+ mst+ "\n");

        Bitmap btm_title = textAsBitmap1(sbTitle.toString(), 600, 20);
        Bitmap btm_big_title = textAsBitmapBold(sbBigTt.toString(), 600, 30, BOLD_CENTER);
        Bitmap btm_merge_all_title = twoBtmap2One(btm_title, btm_big_title);

        StringBuffer sbNv = new StringBuffer();

        sbNv.append("Tên nv bán vé: " + nvBv +"\n");
        sbNv.append("Tên nv lái xe: " + nvLx + "\n");
        sbNv.append("Số xe: " + sx + "\n");

        Bitmap btm_nv = textAsBitmap1(sbNv.toString(), 600, 23);
        Bitmap merge_title_nv = twoBtmap2One(btm_merge_all_title, btm_nv);

        StringBuffer sbCol1 = new StringBuffer();
        sbCol1.append("Mệnh giá");
        StringBuffer sbCol2 = new StringBuffer();
        sbCol2.append("Số lượng");


        Bitmap btm_col1 = textAsBitmap1(sbCol1.toString(),400, 25);
        Bitmap btm_col2 = textAsBitmap1(sbCol2.toString(),200, 25);
        Bitmap merge_btm_col_1_2 = twoBtmap2One1(btm_col1, btm_col2);
        StringBuffer sb_line = new StringBuffer();
        sb_line.append("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Bitmap btm_line = textAsBitmap1(sb_line.toString(), 600, 20);

        Bitmap btm_merge_col_line = twoBtmap2One(merge_btm_col_1_2, btm_line);
        StringBuffer sbBottomCol1 = new StringBuffer();
        StringBuffer sbBottomCol2 = new StringBuffer();
        sbBottomCol1.append("Nạp thẻ:\n");
        sbBottomCol1.append("Quẹt thẻ:");

        sbBottomCol2.append(napthe + "\n");
        sbBottomCol2.append("-" + quetthe + "");

        Bitmap btm_col1_bottom = textAsBitmap1(sbBottomCol1.toString(),400, 20);
        Bitmap btm_col2_bottom = textAsBitmap1(sbBottomCol2.toString(),200, 20);

        Bitmap merge_btm_col_bottom = twoBtmap2One1(btm_col1_bottom, btm_col2_bottom);
        Bitmap btm_merge_col_line_2 = twoBtmap2One(btm_line, merge_btm_col_bottom);

        StringBuffer sbPrice = new StringBuffer();
        StringBuffer sbCount = new StringBuffer();
        JSONArray jsonResponse = new JSONArray(ticket);

        for (int i=0;i<jsonResponse.length();i++) {
            if(i == jsonResponse.length() -1){
                sbPrice.append(jsonResponse.getJSONObject(i).getString("price").toString()+ " VNĐ");
                sbCount.append(jsonResponse.getJSONObject(i).getString("qty").toString() +"");
            }else{
                sbPrice.append(jsonResponse.getJSONObject(i).getString("price").toString()+ " VNĐ\n");
                sbCount.append(jsonResponse.getJSONObject(i).getString("qty").toString() +"\n");
            }
        }
        // send(new byte[] { 0x1b, 0x61, 0x01 });


        Bitmap bitmap = textAsBitmap1(sbPrice.toString(),400, 20);
        Bitmap bitmap1 = textAsBitmap1(sbCount.toString(),200, 20);
        Bitmap merge_btm = twoBtmap2One1(bitmap, bitmap1);
        Bitmap merge_btm_col_line_1 = twoBtmap2One(btm_merge_col_line, merge_btm);
        Bitmap merge_btm_all_line = twoBtmap2One(merge_btm_col_line_1, btm_merge_col_line_2);

        StringBuffer sbThu = new StringBuffer();
        sbThu.append("Thu:\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + totals + " VNĐ");

        Bitmap btm_thu = textAsBitmapBold(sbThu.toString(), 600, 26, BOLD_NORMAL);
        Bitmap btm_merge_content_thu = twoBtmap2One(merge_btm_all_line, btm_thu);

        Bitmap btm_merge_header_content = twoBtmap2One(merge_title_nv, btm_merge_content_thu);

        StringBuffer sbFooter = new StringBuffer();
        sbFooter.append("Đăng nhập lúc: "+timeDn + "\n");
        sbFooter.append("In lúc: "+ hours + "\tNgày: "+ day +"\n");
        sbFooter.append("In tại: 90 Phan Bội Châu Tam Kỳ, QN\n\n\n");
//        sbFooter.append("MST: "+ mst+"\n\n\n\n");

        Bitmap btm_footer = textAsBitmap1(sbFooter.toString(), 600, 20);
        Bitmap btm_merge_header_content_footer = twoBtmap2One(btm_merge_header_content, btm_footer);
        btm_merge_header_content_footer =  newBitmap(btm_merge_header_content_footer);
        final byte[] b=draw2PxPoint(btm_merge_header_content_footer);
        isSuccess = false;
        send(new byte[] { 0x1d, 0x61, 0x00 });
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (isCanprint) {
                    send(b);
                    send(new byte[]{0x1d,0x0c});

                    //��ӡ5�����з���˺ֽ������ֽ���ж�ʹ�ÿ��У�
                    send(new byte[] { 0x0a, 0x0a,0x0a,0x0a});
                    promise.resolve(isCanprint);

                }else {
                    promise.resolve(isCanprint);
                }
            }
        }, 500);
    }
    @ReactMethod
    public void printTest(
            String company, String address, String phone,
            String mst, String kh,
            String nvBv, String nvLx, String sx
    ) throws JSONException {
        String a = "[{ id: 24000, name: 24 }, { id: 2500, name: 24 }]";
        StringBuffer sbBigTt = new StringBuffer();
        sbBigTt.append("TỔNG KẾT");

        StringBuffer sbTitle = new StringBuffer();
        sbTitle.append("\t\t\t"+company+ "\n");
        sbTitle.append(address+"\n");
        sbTitle.append("ĐT: "+ phone +"\n");
        sbTitle.append("Ký hiệu："+kh+"\t\t\t\t\t" +"MST："+ mst+ "\n");

        Bitmap btm_title = textAsBitmap1(sbTitle.toString(), 550, 20);
        Bitmap btm_big_title = textAsBitmapBold(sbBigTt.toString(), 550, 30, BOLD_CENTER);
        Bitmap btm_merge_all_title = twoBtmap2One(btm_title, btm_big_title);

        StringBuffer sbNv = new StringBuffer();

        sbNv.append("Tên nhân viên bán vé \n");
        sbNv.append(nvBv + "\n");
        sbNv.append("Tên nhân viên lái xe \n");
        sbNv.append(nvLx + "\n");
        sbNv.append("Số xe: " + sx + "\n");

        Bitmap btm_nv = textAsBitmap1(sbNv.toString(), 600, 23);
        Bitmap merge_title_nv = twoBtmap2One(btm_merge_all_title, btm_nv);

        JSONArray jsonResponse = new JSONArray(a);
        StringBuffer sbPrice = new StringBuffer();
        sbPrice.append("Mệnh giá \n");
        StringBuffer sbCount = new StringBuffer();
        sbCount.append("Số lượng\n");
        for (int i=0;i<jsonResponse.length();i++) {
            sbPrice.append(jsonResponse.getJSONObject(i).getString("id").toString()+ " VNĐ\n");
            sbCount.append(jsonResponse.getJSONObject(i).getString("name").toString() + "\n");
        }
        // send(new byte[] { 0x1b, 0x61, 0x01 });
        sbPrice.append("Nạp thẻ\n");
        sbPrice.append("Quẹt thẻ");

        sbCount.append("323.000\n");
        sbCount.append("222.000");

        Bitmap bitmap = textAsBitmap1(sbPrice.toString(),400, 20);
        Bitmap bitmap1 = textAsBitmap1(sbCount.toString(),200, 20);
        Bitmap merge_btm = twoBtmap2One1(bitmap, bitmap1);

        StringBuffer sbThu = new StringBuffer();
        sbThu.append("Thu:\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t 230000 VNĐ");

        Bitmap btm_thu = textAsBitmapBold(sbThu.toString(), 600, 26, BOLD_NORMAL);
        Bitmap btm_merge_content_thu = twoBtmap2One(merge_btm, btm_thu);

        Bitmap btm_merge_header_content = twoBtmap2One(merge_title_nv, btm_merge_content_thu);

        StringBuffer sbFooter = new StringBuffer();
        sbFooter.append("Đăng nhập lúc 17:02:54 05-01-2019\n");
        sbFooter.append("In ngày: 17:03:05 Ngày 05-01-2019\n");
        sbFooter.append("In lúc 90 Phan Bội Châu Tam Kỳ, QN\n\n");

        Bitmap btm_footer = textAsBitmap1(sbFooter.toString(), 600, 20);
        Bitmap btm_merge_header_content_footer = twoBtmap2One(btm_merge_header_content, btm_footer);
        btm_merge_header_content_footer =  newBitmap(btm_merge_header_content_footer);
        final byte[] b=draw2PxPoint(btm_merge_header_content_footer);
        send(new byte[] { 0x1d, 0x61, 0x00 });
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (isCanprint) {
                    send(b);
                    send(new byte[]{0x1d,0x0c});

                    //��ӡ5�����з���˺ֽ������ֽ���ж�ʹ�ÿ��У�
                    send(new byte[] { 0x0a, 0x0a,0x0a,0x0a});

                }
            }
        }, 500);
    }

    @ReactMethod
    public void printCard(
            String company, String address, String phone,
            String mst, String kh,String mt, String nv,String price, String sum,
            String time, String date, final Promise promise
    ){
        String bigTitle = "HOÁ ĐƠN THANH TOÁN";
        StringBuffer sbTitle = new StringBuffer();
        sbTitle.append(company+"\n");
        sbTitle.append(""+address+"\n");
        sbTitle.append("ĐT: "+ phone +"\n");
        sbTitle.append("Ký hiệu："+kh+"\t\t\t\t"+ "MST: "+ mst + "\n");

        Bitmap btm_title = textAsBitmap1(sbTitle.toString(), 600, 20);
        Bitmap btm_bigTitle = textAsBitmapBold(bigTitle, 600, 30, BOLD_CENTER);
        Bitmap btm_merge_title_bigtTitle = twoBtmap2One(btm_title, btm_bigTitle);

        StringBuffer sb1 = new StringBuffer();
        sb1.append("Nạp thẻ trả trước\n");
        sb1.append("Mã thẻ:" +mt);

        Bitmap btm_sb1 = textAsBitmap1(sb1.toString(), 600, 25);
        Bitmap btm_merge_allTitle_sb1 = twoBtmap2One(btm_merge_title_bigtTitle, btm_sb1);

        Bitmap sub1_btm = textAsBitmap1("Giá tiền:", 200, 25);
        Bitmap btm_price = textAsBitmapBold(price+ " VNĐ", 400, 25, BOLD_NORMAL);
        Bitmap btm_merge_sub1_price = twoBtmap2One1(sub1_btm, btm_price);

        Bitmap sub2_btm = textAsBitmap1("NV:", 200, 25);
        Bitmap btm_nv = textAsBitmapBold(nv, 400, 25, BOLD_NORMAL);
        Bitmap btm_merge_sub2_price = twoBtmap2One1(sub2_btm, btm_nv);

        Bitmap btm_merge_sub1_sub2 = twoBtmap2One(btm_merge_sub1_price, btm_merge_sub2_price);

        Bitmap sub3_btm = textAsBitmap1("Tổng số :", 200, 25);
        Bitmap btm_sodu = textAsBitmapBold(sum + " VNĐ", 400, 25, BOLD_NORMAL);
        Bitmap btm_merge_sub3_sodu = twoBtmap2One1(sub3_btm, btm_sodu);

        Bitmap btm_merge_sub_1_2_3 = twoBtmap2One(btm_merge_sub1_sub2, btm_merge_sub3_sodu);
        Bitmap btm_merge_title_sub = twoBtmap2One(btm_merge_allTitle_sb1, btm_merge_sub_1_2_3);

        StringBuffer sb2 = new StringBuffer();
        sb2.append("Mua lúc: "+ time+ "\t Ngày: "+date);
        Bitmap btm_sub2 = textAsBitmap1(sb2.toString(), 600, 25);

        Bitmap btm_merge_all = twoBtmap2One(btm_merge_title_sub, btm_sub2);

        btm_merge_all = newBitmap(btm_merge_all);

        final  byte[] b=draw2PxPoint(btm_merge_all);
        send(new byte[] { 0x1d, 0x61, 0x00 });
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (isCanprint) {
                    send(b);
                    send(new byte[]{0x1d,0x0c});

                    //��ӡ5�����з���˺ֽ������ֽ���ж�ʹ�ÿ��У�
                    send(new byte[] { 0x0a, 0x0a,0x0a,0x0a});
                    promise.resolve(isCanprint);
                }else {
                    promise.resolve(isCanprint);
                }
            }
        }, 500);

    }
    public void printListen() {

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

    private void setBitmapBorder(Canvas canvas) {
        Rect rect = canvas.getClipBounds();
        Paint paint = new Paint();
        // ???�?????
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        // ???�????
        paint.setStrokeWidth(10);
        canvas.drawRect(rect, paint);
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
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        StaticLayout layout = new StaticLayout(text, textPaint, width,
                Layout.Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth(),
                layout.getHeight() +10, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);

        layout.draw(canvas);
        return bitmap;

    }
    public static Bitmap textAsBitmapBold(String text, int width, float textSize, Layout.Alignment position) {

        TextPaint textPaint = new TextPaint();

        textPaint.setColor(Color.BLACK);

        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        StaticLayout layout =                layout = new StaticLayout(text, textPaint, width,
                position, 1.3f, 0.0f, true);;

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

    public static Bitmap textAsBitmap3(String text, int width, float textSize) {

        TextPaint textPaint = new TextPaint();

        textPaint.setColor(Color.BLACK);

        textPaint.setTextSize(textSize);

        StaticLayout layout = new StaticLayout(text, textPaint, width,
                Layout.Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
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

    class MBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Toast.makeText(context, "no paper", Toast.LENGTH_LONG).show();

        }
    }

    class PrintBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            processPrint();

        }
    }
    private void readData() {
        Log.d("read data", "read data");
        new Thread() {
            public void run() {
                while (isOpen) {
                    Log.d("check data", "check data");
                    int ret = 0;
                    byte[] buf = new byte[MAX_RECV_BUF_SIZE + 1];
                    ret = mCommonApi.readComEx(mComFd, buf, MAX_RECV_BUF_SIZE,
                            0, 0);

                    Log.d("check ret", ret+ "");
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

                    String str = byteToString(buf, buf.length);
                    Log.e("no paper", "abcde:" + str);

                    if (str.contains("14 00 0C 0F")) {
                        isCanprint = false;
                        isSuccess = false;
                        Log.d("no paper", "no paper");
                        Intent mIntent = new Intent("NOPAPER");
                        context.sendBroadcast(mIntent);
                    }else {
                        isCanprint = true;
                    }
                    if(str.contains("4D")){ //1D 42 45 D1 7A F8
                        Log.e("print success", "" + str);
                        isCanprint = true;
                        isSuccess = true;
                        Intent i = new Intent("PRINTSUCCESS");
                        context.sendBroadcast(i);
                    }
                }
            }
        }.start();
    }


    public void processPrint() {
        WritableMap params;
        params = Arguments.createMap();
        params.putString("status", "success");

        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("PRINT_PROCESS", params);

    }
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
}


