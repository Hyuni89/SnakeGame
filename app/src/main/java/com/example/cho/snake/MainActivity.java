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
    private TextView timeText;
    private RelativeLayout rl;
    private GridLayout gl;
    private SnakeEngine sn;
    private Handler h;
    static final int NOTIFY_N = 0;
    static final int UPDATE_ELEMENT = 1;
    static final int UPDATE_TIME = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == NOTIFY_N) {
                    int n = msg.arg1;
                    gl.removeAllViews();

                    for(int i = 0; i < n * n; i++) {
                        ImageView iv = new ImageView((Context)msg.obj);
                        iv.setImageResource(R.drawable.cell);
                        iv.setLayoutParams(new GridLayout.LayoutParams());

                        Resources r = getResources();
                        int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
                        GridLayout.LayoutParams params = (GridLayout.LayoutParams)iv.getLayoutParams();
                        params.width = px;
                        params.height = px;

                        gl.addView(iv, params);
                    }
                } else if(msg.what == UPDATE_ELEMENT) {
                    ImageView iv = (ImageView)gl.getChildAt(msg.arg2);
                    if(iv != null) {
                        iv.setImageResource(msg.arg1);
                    }
                } else if(msg.what == UPDATE_TIME) {
                    timeText.setText(Integer.toString(msg.arg1));
                }
            }
        };
        sn = new SnakeEngine();
        sn.setHandler(h);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int level = -1;
                while(sn.isAlive()) {
                    if(level != sn.getLevel()) {
                        initMap(sn.getMap().length - 2);
                        level = sn.getLevel();
                        continue;
                    }
                    sn.go();
                    show(sn.getMap());
                    try {
                        Thread.sleep((long)(1000 * sn.getGap()));
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        timeText = (TextView)findViewById(R.id.timeText);
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
                    case -1:
                        iv = R.drawable.block;
                        break;
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
                msg.what = UPDATE_ELEMENT;
                msg.arg1 = iv;
                msg.arg2 = (i - 1) * n + (j - 1);
                h.sendMessage(msg);
            }
        }
    }

    private void initMap(int mapSize) {
        Message msg = new Message();
        msg.what = NOTIFY_N;
        msg.arg1 = mapSize;
        msg.obj = this;
        h.sendMessage(msg);
    }
}