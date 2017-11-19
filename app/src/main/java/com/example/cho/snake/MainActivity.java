package com.example.cho.snake;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private RelativeLayout rl;
    private GridLayout gl;
    private SnakeEngine sn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sn = new SnakeEngine();
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
    }
}
