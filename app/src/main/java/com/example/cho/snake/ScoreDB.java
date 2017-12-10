package com.example.cho.snake;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("score", score);
        db.insert("scoreboard", null, values);
    }

    public long getCount() {
        db = helper.getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, "scoreboard");
    }

    public boolean isPut(int score) {
        if(getCount() <= 10) return true;

        // TODO
        String sql = "select * from scoreboard";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToLast();
        return true;
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
