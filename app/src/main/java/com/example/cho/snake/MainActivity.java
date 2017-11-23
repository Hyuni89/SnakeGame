package com.example.cho.snake;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private RelativeLayout rl;
    private GridLayout gl;
    private SnakeEngine sn;
    private Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 0) {
                    gl.removeAllViews();
                } else {
                    ImageView iv = new ImageView((Context)msg.obj);
                    iv.setImageResource(msg.arg1);
                    iv.setLayoutParams(new GridLayout.LayoutParams());

                    Resources r = getResources();
                    int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
                    GridLayout.LayoutParams params = (GridLayout.LayoutParams)iv.getLayoutParams();
                    params.width = px;
                    params.height = px;

//                gl.setColumnCount(msg.arg2);
//                gl.setRowCount(msg.arg2);
                    gl.addView(iv, params);
                }
            }
        };
        sn = new SnakeEngine();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(sn.isAlive()) {
                    sn.go();
                    clear();
                    show(sn.getMap());
                    try {
                        Thread.sleep((long)(1000 * sn.getGap()));
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        tv = (TextView)findViewById(R.id.gestureStatusText);
        gl = (GridLayout)findViewById(R.id.SnakeMap);
        rl = (RelativeLayout)findViewById(R.id.mainLayer);
        rl.setOnTouchListener(new TouchListener(this) {
            @Override
            public void onSwipeUp() {
                tv.setText("Up");
                sn.setDirection(1);
            }

            @Override
            public void onSwipeDown() {
                tv.setText("Down");
                sn.setDirection(2);
            }

            @Override
            public void onSwipeLeft() {
                tv.setText("Left");
                sn.setDirection(3);
            }

            @Override
            public void onSwipeRight() {
                tv.setText("Right");
                sn.setDirection(4);
            }
        });

        t.start();
    }

    private void show(int[][] map) {
        int n = map.length - 2;
        int iv = 0;

        for(int i = 1; i <= n; i++) {
            for(int j = 1; j <= n; j++) {
                switch(map[i][j]) {
                    case 0:
                        iv = R.drawable.cell;
                        break;
                    case 1:
                        iv = R.drawable.head;
                        break;
                    case 2:
                        iv = R.drawable.body;
                        break;
                    case 3:
                        iv = R.drawable.append;
                        break;
                    default:
                }

                Message msg = new Message();
                msg.what = 1;
                msg.arg1 = iv;
                msg.arg2 = n;
                msg.obj = this;
                h.sendMessage(msg);
            }
        }
    }

    private void clear() {
        h.sendEmptyMessage(0);
    }
}