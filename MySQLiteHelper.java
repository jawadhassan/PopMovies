package com.example.android.popmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jawad on 6/13/16.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "FMoviesDB";


    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_FMOVIE_TABLE = "CREATE TABLE MOVIE(" +

                "VOTES TEXT ," +
                "OVERVIEW TEXT," +
                "ID INTEGER PRIMARY KEY," +
                "POSTERPATH TEXT," +
                "TITLE TEXT," +
                "RELEASEDATE TEXT)";

        db.execSQL(CREATE_FMOVIE_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXITS MOVIE");

        this.onCreate(db);

    }
}
