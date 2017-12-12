package com.example.cho.snake;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by cho on 17. 12. 10.
 */

public class ScoreDB {

    SQLiteDatabase db;
    SQLiteHelper helper;

    ScoreDB(Context context) {
        helper = new SQLiteHelper(context, "ScoreBoard.db", null, 1);
    }

    public void insert(String name, int score) {
        db = helper.getWritableDatabase();

        ArrayList<RecordInfo> tmp = getAll();

        if(tmp.size() < 10) {
            tmp.add(new RecordInfo(name, score));
            Collections.sort(tmp);
        } else {
            Collections.sort(tmp);
            tmp.get(tmp.size() - 1).name = name;
            tmp.get(tmp.size() - 1).score = score;
        }

        helper.onUpgrade(db, 0, 0);
        ContentValues values = new ContentValues();
        for(int i = 0; i < tmp.size(); i++) {
            values.put("name", tmp.get(i).name);
            values.put("score", tmp.get(i).score);
            db.insert("scoreboard", null, values);
        }
    }

    public ArrayList<RecordInfo> getAll() {
        ArrayList<RecordInfo> ret = new ArrayList<>();
        String sql = "select * from scoreboard";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();

        for(int i = 0; i < getCount(); i++) {
            ret.add(new RecordInfo(cursor.getString(1), cursor.getInt(2)));
            cursor.moveToNext();
        }

        for(int i=0; i<ret.size(); i++) {
            Log.d("by cho", String.format("[%s][%d]", ret.get(i).name, ret.get(i).score));
        }

        return ret;
    }

    public long getCount() {
        db = helper.getReadableDatabase();
        long ret = DatabaseUtils.queryNumEntries(db, "scoreboard");
        Log.d("by cho", "DB count: " + Long.toString(ret));
        return ret;
    }

    public boolean isPut(int score) {
        if(getCount() < 10) return true;

        String sql = "select min(score) from scoreboard";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        int lastOne = cursor.getInt(0);
        Log.d("by cho", String.format("isPut[%d][%d]", lastOne, score));

        if(score > lastOne) return true;
        return false;
    }
}

class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "create table scoreboard (" + "_id integer primary key, " + "name text, " + "score integer);";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sql = "drop table if exists scoreboard";
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }
}

class RecordInfo implements Comparable<RecordInfo> {

    String name;
    int score;

    RecordInfo(String name, int score) {
        this.name = name;
        this.score = score;
    }

    @Override
    public int compareTo(RecordInfo recordInfo) {
        return this.score - recordInfo.score;
    }
}