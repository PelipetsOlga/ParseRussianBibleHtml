package com.example.olga.parserussianbiblehtml;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

public class BibleDataBase {
    private Context mCtx;
    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;
    private ContentValues cv;

    public static final String DATABASE_NAME = "bible.db";
      public static final int DATABASE_VERSION = 1;
    public static final String TABLE_CHAPTERS = "chapters";
    public static final String TABLE_TEXTS = "texts";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_NUM = "num"; // default 0
    public static final String COLUMN_MODE = "mode";// default 1

    public static final String COLUMN_CHAPTER_ID = "chapter_id";
    public static final String COLUMN_CHAPTER_NUM = "chapter_num";
    public static final String COLUMN_POSITION = "position";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_HIGHLIGHT = "highlight"; // default 0
    public static final String COLUMN_BOOKMARK = "bookmark"; // default 0
    public static final String COLUMN_BOOKMARK_TEXT = "bookmark_text";

    private static final String DATABASE_TABLE_CHAPTERS_CREATE_SCRIPT = "create table "
            + TABLE_CHAPTERS
            + " ( "
            + COLUMN_ID
            + " integer primary key autoincrement, "
            +  COLUMN_NUM
            +" integer default 0, "
            +  COLUMN_MODE
            +" integer default 1, "
            + COLUMN_TITLE
            + " text not null);";

    private static final String DATABASE_TABLE_TEXTS_CREATE_SCRIPT = "create table "
            + TABLE_TEXTS
            + " ( "
            + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_CHAPTER_ID
            + " integer, "
            + COLUMN_CHAPTER_NUM
            + " integer, "
            + COLUMN_POSITION
            + " integer, "
            +  COLUMN_HIGHLIGHT
            +" integer default 0, "
            +  COLUMN_BOOKMARK
            +" integer default 0, "
            +  COLUMN_BOOKMARK_TEXT
            +" text, "
            + COLUMN_TEXT
            + " text not null);";

    public BibleDataBase (Context ctx) {
        mCtx = ctx;
    }

    private long getCurrentChapterId(String title){
        Cursor c=mDB.query(TABLE_CHAPTERS,null, COLUMN_TITLE+"=\""+title+"\"",null,null,null, null);
        if (c.moveToFirst()){
            return c.getLong(c.getColumnIndex(COLUMN_ID));
        }else {

            return -1;
        }
    }

    public void open() {
        mDBHelper = new DBHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    public void close() {
        if (mDBHelper != null)
            mDBHelper.close();
    }

    public void saveChapter(Chapter chapter){
        long chapterId=getCurrentChapterId(chapter.getTitle());
        if (chapterId<0) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_TITLE, chapter.getTitle());
            chapterId = mDB.insert(TABLE_CHAPTERS, null, cv);
        }

        final ArrayList<String> content = chapter.getContent();
        for (String row: content){
            cv=new ContentValues();
            cv.put(COLUMN_CHAPTER_ID, chapterId);
            cv.put(COLUMN_CHAPTER_NUM, chapter.getNum());
            cv.put(COLUMN_POSITION, content.indexOf(row)+1);
            cv.put(COLUMN_TEXT, row);
            mDB.insert(TABLE_TEXTS, null, cv);
        }


    }


    private class DBHelper extends SQLiteOpenHelper {
        private final Context fContext;

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
            fContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_TABLE_CHAPTERS_CREATE_SCRIPT);
            db.execSQL(DATABASE_TABLE_TEXTS_CREATE_SCRIPT);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }


  }