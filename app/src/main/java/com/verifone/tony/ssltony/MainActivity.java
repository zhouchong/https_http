package com.verifone.tony.ssltony;

import android.app.Activity;
import android.content.Context;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "------------tony";
    TextView txt;
    private String str = "";
    private Context context;

    private static String MSG = "POST /mjc/webtrans/VPB_lb HTTP/1.1\r\nHOST: 120.204.69.139:30000\r\nUser-Agent: Donjin Http 0.1\r\nCache-Control: no-cache\r\nContent-Type:x-ISO-TPDU/x-auth\r\nAccept: */*\r\nContent-Length: 93\r\n\r\n";
    private static String hexStr = "005b600601000060320043000108000020000000c0001600014930303030303031383130323331303036303531303030320011000004300030002953657175656e6365204e6f313633323639563332392d3039352d3030330003303132";

    //将调试信息显示到TextView
    private Handler handler = new Handler();
    private class myRunnable implements Runnable {
        String str;
        private myRunnable(String str) {
            this.str = str;
        }
        public void run() {
            txt.append(this.str);
            int offset=txt.getLineCount()*txt.getLineHeight();
            if (offset > txt.getHeight()) {
                txt.scrollTo(0, offset - txt.getHeight());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();

        //初始化第三方log库
        Logger.init("vrfn").hideThreadInfo();
        Logger.t(TAG).d(" ");

        setContentView(R.layout.activity_main);
        txt = (TextView) findViewById(R.id.textView);
        txt.setMovementMethod(ScrollingMovementMethod.getInstance());

        //清屏
        final Button button_clear = (Button) findViewById(R.id.button_clear);
        button_clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                txt.setText("");
                txt.scrollTo(0, 0);
            }
        });

        final ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                //Toast.makeText(MainActivity.this, "Checked=" + isChecked, Toast.LENGTH_SHORT).show();
                new toggleButton().execute(isChecked);
            }
        });

        final Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new button1Task().execute("button1Task");
            }
        });

        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new button2Task().execute("button2Task");
            }
        });

        final Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new button3Task().execute("button3Task");
            }
        });
    }

    private class toggleButton extends AsyncTask<Boolean, Void, Boolean> {
        protected Boolean doInBackground(Boolean... urls) {
            str = "Doing toggleButton ...\n";
            str += "param = " + urls[0] + "\n";
            handler.post(new myRunnable(str));

            if (urls[0]) {
                HttpsUtil.getInstance(context);
                str = "已连接\n";
                handler.post(new myRunnable(str));
                Logger.t(TAG).d(str);
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(MainActivity.this, "toggleButton Success", Toast.LENGTH_SHORT).show();
                str = "toggleButton Success\n";
                handler.post(new myRunnable(str));
            } else {
                Toast.makeText(MainActivity.this, "toggleButton Failed", Toast.LENGTH_SHORT).show();
                str = "toggleButton Failed\n";
                handler.post(new myRunnable(str));
            }
        }
    }

    private class button1Task extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... urls) {
            str = "Doing Button1Task ...\n";
            str += "param = " + urls[0] + "\n";
            handler.post(new myRunnable(str));
            //todo
            try {
                Logger.t(TAG).d("Send Len: " + hexStr2Bytes(hexStr).length);
                Logger.t(TAG).d(new BigInteger(1, hexStr2Bytes(hexStr)).toString(16));
                boolean ret = HttpsUtil.getInstance(context).SyncPost(hexStr2Bytes(hexStr));
                if (ret) {
                    str = "发送成功\n";
                    handler.post(new myRunnable(str));
                    Logger.t(TAG).d(str);
                } else {
                    str = "发送失败\n";
                    handler.post(new myRunnable(str));
                    Logger.t(TAG).d(str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(MainActivity.this, "Button1Task Success", Toast.LENGTH_SHORT).show();
                str = "Button1Task Success\n";
                handler.post(new myRunnable(str));
            } else {
                Toast.makeText(MainActivity.this, "Button1Task Failed", Toast.LENGTH_SHORT).show();
                str = "Button1Task Failed\n";
                handler.post(new myRunnable(str));
            }
        }
    }

    private class button2Task extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... urls) {
            str = "Doing Button2Task ...\n";
            str += "param = " + urls[0] + "\n";
            handler.post(new myRunnable(str));
            //todo
            String getStr = HttpsUtil.getInstance(context).SyncGet();
            if (getStr != null) {
                str = getStr;
                str += "\n接收成功\n";
                handler.post(new myRunnable(str));
                Logger.t(TAG).d( "接收成功: " + getStr);
            } else {
                Logger.t(TAG).e( "接收失败");
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(MainActivity.this, "Button2Task Success", Toast.LENGTH_SHORT).show();
                str = "Button2Task Success\n";
                handler.post(new myRunnable(str));
            } else {
                Toast.makeText(MainActivity.this, "Button2Task Failed", Toast.LENGTH_SHORT).show();
                str = "Button2Task Failed\n";
                handler.post(new myRunnable(str));
            }
        }
    }

    private class button3Task extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... urls) {
            str = "Doing Button3Task ...\n";
            str += "param = " + urls[0] + "\n";
            handler.post(new myRunnable(str));
            //todo
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(MainActivity.this, "Button3Task Success", Toast.LENGTH_SHORT).show();
                str = "Button3Task Success\n";
                handler.post(new myRunnable(str));
            } else {
                Toast.makeText(MainActivity.this, "Button3Task Failed", Toast.LENGTH_SHORT).show();
                str = "Button3Task Failed\n";
                handler.post(new myRunnable(str));
            }
        }
    }

    public static String hexStr2Str(String hexStr) {
        if (hexStr == null || hexStr.length() <= 0) {
            return null;
        }
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int n = str.indexOf(hexs[(2 * i)]) * 16;
            n += str.indexOf(hexs[(2 * i + 1)]);
            bytes[i] = ((byte) (n & 0xFF));
        }
        try {
            return new String(bytes, "ISO-8859-1");
        } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
        }
        return "";
    }

    public static byte[] hexStr2Bytes(String src) {
        if (src == null || src.length() <= 0) {
            return null;
        }
        try {
            int m = 0;
            int n = 0;
            if (src.length() % 2 != 0) {
                src = "0" + src;
            }
            int l = src.length() / 2;

            byte[] ret = new byte[l];
            for (int i = 0; i < l; i++) {
                m = i * 2 + 1;
                n = m + 1;
                ret[i] = Integer.decode("0x" + src.substring(i * 2, m) + src.substring(m, n)).byteValue();
            }
            return ret;
        } catch (Exception e) {
        }
        return null;
    }
}

