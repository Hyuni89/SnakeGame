package com.example.cho.snake;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cho.snake.connection.CombatManager;
import com.example.cho.snake.connection.DeviceListFragment;
import com.example.cho.snake.database.RecordInfo;
import com.example.cho.snake.database.ScoreDB;
import com.example.cho.snake.fragments.GameOverFragment;
import com.example.cho.snake.fragments.InputScoreFragment;
import com.example.cho.snake.fragments.ScoreBoardFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private TextView levelText;
    private TextView timeText;
    private TextView scoreText;
    private RelativeLayout rl;
    private GridLayout gl;
    private GridLayout rivalMap;
    private SnakeEngine sn;
    private Handler h;
    private Thread t;
    private FragmentManager fm;
    private Fragment fg;
    private FragmentTransaction ft;
    private ScoreDB db;
    private int deviceWidth;
    private int deviceHeight;
    private ImageView pauseButton;
    private ImageView combatButton;
    private boolean pause;
    private CombatManager cm;
    private boolean isCombat;
    private boolean isCombatReady;
    private boolean interruptEngine;
    private boolean isWinner;

    static final int NOTIFY_N = 0;
    static final int UPDATE_ELEMENT = 1;
    static final int UPDATE_TIME = 2;
    static final int UPDATE_LEVEL = 3;
    static final int UPDATE_SCORE = 4;
    static final int GAMEOVER = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        deviceWidth = dm.widthPixels;
        deviceHeight = dm.heightPixels;
        pause = false;
        isCombat = false;
        isCombatReady = false;
        interruptEngine = false;
        isWinner = false;

        db = new ScoreDB(this);
        h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == NOTIFY_N) {
                    int n = msg.arg1;
                    GridLayout target = (GridLayout)msg.obj;
                    target.removeAllViews();
                    target.setColumnCount(n);

                    for(int i = 0; i < n * n; i++) {
                        ImageView iv = new ImageView(MainActivity.this);
                        iv.setImageResource(R.drawable.cell);
                        iv.setLayoutParams(new GridLayout.LayoutParams());

//                        Resources r = getResources();
//                        int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
                        GridLayout.LayoutParams params = (GridLayout.LayoutParams)iv.getLayoutParams();
                        int px = (int)(deviceWidth * 0.6) / n;
                        if(target == rivalMap) {
                            px = (int)(deviceWidth * 0.2) / n;
                        }
                        params.width = px;
                        params.height = px;

                        target.addView(iv, params);
                    }
                } else if(msg.what == UPDATE_ELEMENT) {
                    GridLayout target = (GridLayout)msg.obj;
                    ImageView iv = (ImageView)target.getChildAt(msg.arg2);
                    if(iv != null) {
                        iv.setImageResource(msg.arg1);
                    }
                } else if(msg.what == UPDATE_TIME) {
                    timeText.setText(Integer.toString(msg.arg1));
                } else if(msg.what == UPDATE_LEVEL) {
                    levelText.setText("Level " + Integer.toString(sn.getLevel()));
                } else if(msg.what == UPDATE_SCORE) {
                    scoreText.setText("Score " + Integer.toString(msg.arg1));
                } else if(msg.what == GAMEOVER) {
                    fm = getFragmentManager();
                    if(!isCombat && db.isPut(sn.getScore())) {
                        fg = new InputScoreFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt("score", sn.getScore());
                        fg.setArguments(bundle);
                        ft = fm.beginTransaction();
                        ft.add(R.id.fragmentPosition, fg);
                        ft.commit();
                    } else {
                        showGameOverState();
                    }
                } else if(msg.what == cm.STATECONNECTED) {
                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                    clearFragment();
                    stopEngine();
                    restartGame();
                    isCombat = true;
                    isCombatReady = false;
                    cm.setRivalMap(rivalMap);
                    rivalMap.setVisibility(View.VISIBLE);
                } else if(msg.what == cm.STATECONNECTING) {
                    Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                    isCombat = false;
                    isCombatReady = false;
                    rivalMap.setVisibility(View.GONE);
                } else if(msg.what == cm.STATELISTEN) {
                    Toast.makeText(getApplicationContext(), "Listening", Toast.LENGTH_SHORT).show();
                    isCombat = false;
                    isCombatReady = false;
                    rivalMap.setVisibility(View.GONE);
                }
            }
        };
        sn = new SnakeEngine(h);
        t = new Thread(new SnakeRunnable());
        cm = new CombatManager(this, h, sn);
        cm.start();

        levelText = (TextView)findViewById(R.id.levelText);
        timeText = (TextView)findViewById(R.id.timeText);
        scoreText = (TextView)findViewById(R.id.scoreText);
        tv = (TextView)findViewById(R.id.gestureStatusText);
        gl = (GridLayout)findViewById(R.id.SnakeMap);
        rivalMap = (GridLayout)findViewById(R.id.rivalMap);
        rl = (RelativeLayout)findViewById(R.id.mainLayer);
        rl.setOnTouchListener(new TouchListener(this) {
            @Override
            public void onSwipeUp() {
                if(!isCombat || (isCombat && sn.getDirection() != 0)) {
                    tv.setText("Up");
                    sn.setDirection(1);
                }
            }

            @Override
            public void onSwipeDown() {
                if(!isCombat || (isCombat && sn.getDirection() != 0)) {
                    tv.setText("Down");
                    sn.setDirection(2);
                }
            }

            @Override
            public void onSwipeLeft() {
                if(!isCombat || (isCombat && sn.getDirection() != 0)) {
                    tv.setText("Left");
                    sn.setDirection(3);
                }
            }

            @Override
            public void onSwipeRight() {
                if(!isCombat || (isCombat && sn.getDirection() != 0)) {
                    tv.setText("Right");
                    sn.setDirection(4);
                }
            }

            @Override
            public void Cont() {
                if(isCombat && sn.getDirection() == 0) {
                    if(!isCombatReady) {
                        timeText.setText("Ready!!");
                        isCombatReady = true;
                    } else {
                        timeText.setText("Touch To Ready");
                        isCombatReady = false;
                    }
                    cm.setReady(isCombatReady);
                } else if(fg == null) {
                    restartGame();
                }
            }
        });
        pauseButton = (ImageView)findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isCombat) {
                    pause = !pause;
                    sn.setPause(pause);
                    if(pause) {
                        timeText.setText("Pause");
                    }
                }
            }
        });
        combatButton = (ImageView)findViewById(R.id.combatButton);
        combatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isCombat) {
                    stopEngine();
                    restartGame();
                    cm.initConnection();
                } else {
                    cm.stop();
                }
            }
        });

        t.start();
    }

    public void restartGame() {
        if(!sn.isAlive()) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            clearFragment();

            sn = new SnakeEngine(h);
            cm.setSnakeEngine(sn);
            isCombatReady = false;
            cm.setReady(isCombatReady);
            isWinner = false;
            t = new Thread(new SnakeRunnable());
            t.start();
        }
    }

    public void updateDB(String name, int score) {
        db.insert(name, score);
        clearFragment();
        showGameOverState();
    }

    public ArrayList<RecordInfo> getAll() {
        return db.getAll();
    }

    private void showGameOverState() {
        fg = new GameOverFragment();
        if(isCombat) ((GameOverFragment)fg).setCombatResult(isWinner);
        if(fm == null) fm = getFragmentManager();
        ft = fm.beginTransaction();
        ft.add(R.id.fragmentPosition, fg);
        ft.commit();
    }

    public void showScoreBoardState() {
        clearFragment();
        fg = new ScoreBoardFragment();
        if(fm == null) fm = getFragmentManager();
        ft = fm.beginTransaction();
        ft.add(R.id.fragmentPosition, fg);
        ft.commit();
    }

    public void showDevicesListState() {
        clearFragment();
        fg = new DeviceListFragment();
        if(fm == null) fm = getFragmentManager();
        ft = fm.beginTransaction();
        ft.add(R.id.fragmentPosition, fg);
        ft.commit();
    }

    public void clearFragment() {
        if(fg != null) {
            ft = fm.beginTransaction();
            ft.remove(fg);
            ft.commit();

            fg = null;
        }
    }

    public void show(GridLayout target, int[][] map) {
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
                msg.obj = target;
                msg.arg1 = iv;
                msg.arg2 = (i - 1) * n + (j - 1);
                h.sendMessage(msg);
            }
        }
    }

    public void initMap(GridLayout map, int mapSize) {
        Message msg = new Message();
        msg.what = NOTIFY_N;
        msg.arg1 = mapSize;
        msg.obj = map;
        h.sendMessage(msg);
    }

    private class SnakeRunnable implements Runnable {
        @Override
        public void run() {
            int level = -1;
            while(sn.isAlive() && !interruptEngine) {
                if(level != sn.getLevel()) {
                    initMap(gl, sn.getMap().length - 2);
                    level = sn.getLevel();
                    continue;
                }

                while(pause && !interruptEngine);

                sn.go();
                show(gl, sn.getMap());

                if(isCombat) {
                    byte[] data = new byte[CombatManager.BUFSIZE];
                    cm.sendMap(data);
                }

                Log.d("by cho", String.format("isCombat[%b], direction[%d], isReady[%b], rivalReady[%b]", isCombat, sn.getDirection(), isCombatReady, cm.getReady()));
                while(isCombat && sn.getDirection() == 0 && !interruptEngine) {
                    Log.d("by cho", String.format("isCombat[%b], direction[%d], isReady[%b], rivalReady[%b]", isCombat, sn.getDirection(), isCombatReady, cm.getReady()));
                    if(isCombatReady && cm.getReady()) {
                        isCombatReady = false;
                        sn.setDirection(1);
                        break;
                    }
                }
                cm.setReady(isCombatReady);

                try {
                    Thread.sleep((long)(1000 * sn.getGap()));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            if(interruptEngine) {
                interruptEngine = false;
            } else {
                Message msg = new Message();
                msg.what = GAMEOVER;
                h.sendMessage(msg);
            }

            if(!isWinner && isCombat && !interruptEngine) {
                isWinner = false;
                byte[] data = new byte[1];
                data[0] = (byte)3;
                cm.write(data);
            } else if(isWinner){
                Message msg = new Message();
                msg.what = GAMEOVER;
                h.sendMessage(msg);
            }
        }
    }

    private void stopEngine() {
        interruptEngine = true;
        pause = false;
        sn.kill();
    }

    public void setWinner() {
        isWinner = true;
        stopEngine();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case CombatManager.REQUEST_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK) {
                    cm.scanDevice();
                }
                break;
            case CombatManager.REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK) {
                    Log.d("by cho", "come here");
                    cm.getDeviceInfo(data);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        clearFragment();
    }
}