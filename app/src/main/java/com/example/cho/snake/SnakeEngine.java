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

    public SnakeEngine() {
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

    class point {
        public int x;
        public int y;
        point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
