package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ProgrammingKnowledge on 4/3/2015.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "BayesBeat.db";
    public static final String TABLE_NAME = "files_table";
    public static final String COL_1 = "id";
    public static final String COL_2 = "name";
    public static final String COL_3 = "is_uploaded";
    public static final String COL_4 = "source";
    public static final String COL_5 = "uploaded_at";
    public static final String COL_6 = "result_generated";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "is_uploaded INTEGER," +
                "source TEXT," +
                "uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "result_generated INTEGER" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String name, String source, int result_generated, int is_uploaded) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, is_uploaded);
        contentValues.put(COL_4, source);
        contentValues.put(COL_6, result_generated);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    public Cursor getLastN_Data(int n) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME +" order by id  DESC limit "+n, null);
        return res;
    }

//    public boolean updateData(String id,String name,String surname,String marks) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(COL_1,id);
//        contentValues.put(COL_2,name);
//        contentValues.put(COL_3,surname);
//        contentValues.put(COL_4,marks);
//        db.update(TABLE_NAME, contentValues, "ID = ?",new String[] { id });
//        return true;
//    }

    public Integer deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "id = ?", new String[]{id});
    }
}