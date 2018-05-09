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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "------------tony";
    TextView txt;
    private String str = "";
    private Context context;

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
                HttpsUtil.getInstance(context).SyncPost("Hello World!");
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
                str += "\n";
                handler.post(new myRunnable(str));
                Logger.t(TAG).d( "SyncGet(): " + getStr);
            } else {
                Logger.t(TAG).e( "SyncGet() = NULL");
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
}

