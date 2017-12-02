package com.example.cho.snake;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by cho on 17. 11. 19.
 */

public class SnakeEngine {

    private int n;
    private int[][] map;
    private int level;
    private int direction;
    private double timeGap;
    private int limitTime;
    private ArrayList<point> body;
    private point head;
    private boolean isAlive;
    private Thread timeThread;
    private Thread itemThread;
    private Handler h;
    private Message msg;
    private int score;
    enum ScoreType {
        INIT, TIME, APPEND
    }

    private final int LIMITTIME = 30;
    private final double TIMEGAP = 0.5;


    public SnakeEngine(Handler h) {
        body = new ArrayList<point>();
        head = new point(5, 5);
        body.add(head);
        this.h = h;
        setScore(ScoreType.INIT);
        setLevel(1);
    }

    public SnakeEngine(Handler h, int l) {
        this.h = h;
        setScore(ScoreType.INIT);
        setLevel(l);
    }

    private void setLevel(int l) {
        level = l;
        n = 9 + level;
        map = new int[n + 2][n + 2];
        for(int i = 0; i < n + 2; i++) {
            for(int j = 0; j < n + 2; j++) {
                if(i == 0 || i == n + 1) map[i][j] = -1;
                if(j == 0 || j == n + 1) map[i][j] = -1;
            }
        }
        direction = 0;
        timeGap = Math.pow(TIMEGAP, level - 1);
        limitTime = LIMITTIME;
        isAlive = true;
        timeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(limitTime > 0 && isAlive) {
                    try {
                        Thread.sleep(1000);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    limitTime -= 1;
                    setScore(ScoreType.TIME);
                    msg = new Message();
                    msg.what = MainActivity.UPDATE_TIME;
                    msg.arg1 = limitTime;
                    h.sendMessage(msg);
                }

                if(isAlive) {
                    setLevel(level + 1);
                }
            }
        });
        itemThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                while(isAlive && limitTime > 0) {
                    try {
                        Thread.sleep(random.nextInt(10) * 1000);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    int x, y;
                    do {
                        x = random.nextInt(n) + 1;
                        y = random.nextInt(n) + 1;
                    } while(map[y][x] != 0 || ((direction == 1 || direction == 2) && x == head.x) || ((direction == 3 || direction == 4) && y == head.y));

                    if(random.nextBoolean()) {
                        map[y][x] = 3;      // append
                    } else {
                        map[y][x] = -1;     // block
                    }
                }
            }
        });

        for(int i = 0; i < body.size(); i++) {
            body.get(i).x = n / 2;
            body.get(i).y = n / 2;
        }
        head.x = n / 2;
        head.y = n / 2;
        map[head.y][head.x] = 1;

        msg = new Message();
        msg.what = MainActivity.UPDATE_LEVEL;
        h.sendMessage(msg);
        msg = new Message();
        msg.what = MainActivity.UPDATE_TIME;
        msg.arg1 = limitTime;
        h.sendMessage(msg);
    }

    public int getLevel() {
        return level;
    }

    public void setDirection(int swipe) {
        // swipe        1:up 2:down, 3:left, 4:right
        // direction    1:up 2:down, 3:left, 4:right
        if(direction == 0) {
            direction = swipe;
            timeThread.start();
            itemThread.start();
        } else {
            switch(swipe) {
                case 1:
                case 2:
                    switch(direction) {
                        case 1: break;
                        case 2: break;
                        case 3: direction = swipe; break;
                        case 4: direction = swipe; break;
                    }
                    break;
                case 3:
                case 4:
                    switch(direction) {
                        case 1: direction = swipe; break;
                        case 2: direction = swipe; break;
                        case 3: break;
                        case 4: break;
                    }
                    break;
            }
        }
    }

    public void go() {
        int tx = body.get(body.size() - 1).x;
        int ty = body.get(body.size() - 1).y;
        switch(direction) {
            case 1:
                head.y -= 1;
                body.add(0, new point(head));
                body.remove(body.size() - 1);
                break;
            case 2:
                head.y += 1;
                body.add(0, new point(head));
                body.remove(body.size() - 1);
                break;
            case 3:
                head.x -= 1;
                body.add(0, new point(head));
                body.remove(body.size() - 1);
                break;
            case 4:
                head.x += 1;
                body.add(0, new point(head));
                body.remove(body.size() - 1);
                break;
            default:
                return;
        }
        map[ty][tx] = 0;

        switch(map[head.y][head.x]) {
            case -1:    // block
            case 1:     // head
            case 2:     // body
                isAlive = false;
                return;
            case 3:     // append
                body.add(new point(body.get(body.size() - 1).x, body.get(body.size() - 1).y));
                setScore(ScoreType.APPEND);
            default:
                for(int i = 0; i < body.size(); i++) {
                    map[body.get(i).y][body.get(i).x] = 2;
                }
                map[head.y][head.x] = 1;
        }
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public double getGap() {
        return timeGap;
    }

    public int[][] getMap() {
        return map;
    }

    synchronized private void setScore(ScoreType type) {
        if(type == ScoreType.TIME) {
            score += 1;
        } else if(type == ScoreType.APPEND) {
            score += 100;
        } else if(type == ScoreType.INIT) {
            score = 0;
        }

        msg = new Message();
        msg.what = MainActivity.UPDATE_SCORE;
        msg.arg1 = score;
        h.sendMessage(msg);
    }

    class point {
        public int x;
        public int y;
        point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        point(point p) {
            x = p.x;
            y = p.y;
        }
    }
}
