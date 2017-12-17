package com.example.cho.snake.database;

/**
 * Created by cho on 17. 12. 17.
 */

public class RecordInfo implements Comparable<RecordInfo> {

    public String name;
    public int score;

    RecordInfo(String name, int score) {
        this.name = name;
        this.score = score;
    }

    @Override
    public int compareTo(RecordInfo recordInfo) {
        return recordInfo.score - this.score;
    }
}