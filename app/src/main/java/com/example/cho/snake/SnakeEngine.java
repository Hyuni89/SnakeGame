package com.example.cho.snake;

import java.util.ArrayList;

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

    public SnakeEngine() {
        body = new ArrayList<point>();
        head = new point(5, 5);
        body.add(head);
        setLevel(1);
    }

    public SnakeEngine(int l) {
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
        timeGap = Math.pow(0.9, level - 1);
        limitTime = 60;
        isAlive = true;

        for(int i = 0; i < body.size(); i++) {
            body.get(i).x = n / 2;
            body.get(i).y = n / 2;
        }
        head.x = n / 2;
        head.y = n / 2;
        map[head.y][head.x] = 1;
    }

    public int getLevel() {
        return level;
    }

    public void setDirection(int swipe) {
        // swipe        1:up 2:down, 3:left, 4:right
        // direction    1:up 2:down, 3:left, 4:right
        if(direction == 0) {
            direction = swipe;
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
                body.add(0, head);
                body.remove(body.size() - 1);
                break;
            case 2:
                head.y += 1;
                body.add(0, head);
                body.remove(body.size() - 1);
                break;
            case 3:
                head.x -= 1;
                body.add(0, head);
                body.remove(body.size() - 1);
                break;
            case 4:
                head.x += 1;
                body.add(0, head);
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
                body.add(new point(tx, ty));
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

    class point {
        public int x;
        public int y;
        point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
