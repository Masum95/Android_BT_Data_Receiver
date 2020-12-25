package com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.FileModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by ProgrammingKnowledge on 4/3/2015.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "BAYES_BEAT";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(FileModel.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + FileModel.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }


    public boolean insertFileInfo(String name, String source, int result_generated, int is_uploaded) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FileModel.COL_FILE_NAME, name);
        contentValues.put(FileModel.COL_IS_UPLOADED, is_uploaded);
        contentValues.put(FileModel.COL_SRC, source);
        contentValues.put(FileModel.COL_RESULT_GEN, result_generated);

        long result = db.insert(FileModel.TABLE_NAME, null, contentValues);
        db.close();

        if (result == -1)
            return false;
        else
            return true;
    }


    public List<FileModel> getFiles(Integer... args) {
        int limit = args.length > 0 ? args[0] : 0;
        List<FileModel> files = new ArrayList<>();

        // Select All Query
        String selectQuery = null;
        if(limit==0){
            selectQuery = "SELECT  * FROM " + FileModel.TABLE_NAME + " ORDER BY " +
                    FileModel.COL_ID + " DESC";
        }else{
            selectQuery = "SELECT  * FROM " + FileModel.TABLE_NAME + " ORDER BY " +
                    FileModel.COL_ID + "  DESC limit " + limit;
        }


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                FileModel file = new FileModel();
                file.setFileName(cursor.getString(cursor.getColumnIndex(FileModel.COL_FILE_NAME)));
                file.setSrc(cursor.getString(cursor.getColumnIndex(FileModel.COL_SRC)));
                file.setUploadedAt(cursor.getString(cursor.getColumnIndex(FileModel.COL_UPLOAD_TIME)));
                file.setIsUploaded(cursor.getInt(cursor.getColumnIndex(FileModel.COL_IS_UPLOADED)));
                file.setResultGen(cursor.getInt(cursor.getColumnIndex(FileModel.COL_RESULT_GEN)));

                files.add(file);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return files;
    }


//    public Note getNote(long id) {
//        // get readable database as we are not inserting anything
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(Note.TABLE_NAME,
//                new String[]{Note.COLUMN_ID, Note.COLUMN_NOTE, Note.COLUMN_TIMESTAMP},
//                Note.COLUMN_ID + "=?",
//                new String[]{String.valueOf(id)}, null, null, null, null);
//
//        if (cursor != null)
//            cursor.moveToFirst();
//
//        // prepare note object
//        Note note = new Note(
//                cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)),
//                cursor.getString(cursor.getColumnIndex(Note.COLUMN_NOTE)),
//                cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)));
//
//        // close the db connection
//        cursor.close();
//
//        return note;
//    }
//    public int getNotesCount() {
//        String countQuery = "SELECT  * FROM " + Note.TABLE_NAME;
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(countQuery, null);
//
//        int count = cursor.getCount();
//        cursor.close();
//
//
//        // return count
//        return count;
//    }
//
//    public int updateNote(Note note) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(Note.COLUMN_NOTE, note.getNote());
//
//        // updating row
//        return db.update(Note.TABLE_NAME, values, Note.COLUMN_ID + " = ?",
//                new String[]{String.valueOf(note.getId())});
//    }
//
//    public void deleteNote(Note note) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(Note.TABLE_NAME, Note.COLUMN_ID + " = ?",
//                new String[]{String.valueOf(note.getId())});
//        db.close();
//    }
}