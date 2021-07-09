package com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.FileModel;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ProfileModel;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ResultModel;
import com.samsung.android.sdk.accessory.example.filetransfer.receiver.Result;

import java.util.ArrayList;
import java.util.List;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.WATCH_SRC_KEYWORD;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.FileModel.COL_FILE_NAME;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.FileModel.COL_IS_UPLOADED;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.FileModel.COL_SRC;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ProfileModel.COL_DEVICE_ID;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ProfileModel.COL_PHONE_NUM;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ProfileModel.COL_REGI_ID;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ProfileModel.COL_USER_NAME;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ResultModel.COL_AVG_ACTIVITY;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ResultModel.COL_AVG_HEART_RATE;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ResultModel.COL_RESULT;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Database.Model.ResultModel.COL_TIMESTAMP;
import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Utils.getDateTimeFromTimestamp;

/**
 * Created by ProgrammingKnowledge on 4/3/2015.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "BAYES_BEAT.db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        getReadableDatabase(); // <-- add this, which triggers onCreate/onUpdate

        Log.d("database", "here in constructor");
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(FileModel.CREATE_TABLE);

        db.execSQL(ProfileModel.CREATE_TABLE);
        db.execSQL(ResultModel.CREATE_TABLE);
        Log.d("tag=======", "here in creation ");
//        onCreate();
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + FileModel.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProfileModel.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }


    public boolean insertFileInfo(String name, String source, int result_generated, int is_uploaded) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_FILE_NAME, name);
        contentValues.put(COL_IS_UPLOADED, is_uploaded);
        contentValues.put(COL_SRC, source);
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
                file.setFileName(cursor.getString(cursor.getColumnIndex(COL_FILE_NAME)));
                file.setSrc(cursor.getString(cursor.getColumnIndex(COL_SRC)));
                file.setUploadedAt(cursor.getString(cursor.getColumnIndex(FileModel.COL_UPLOAD_TIME)));
                file.setIsUploaded(cursor.getInt(cursor.getColumnIndex(COL_IS_UPLOADED)));
                file.setResultGen(cursor.getInt(cursor.getColumnIndex(FileModel.COL_RESULT_GEN)));

                files.add(file);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return files;
    }

    public List<FileModel> getUnuploadedFilePaths(Integer... args) {
        List<FileModel> files = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + FileModel.TABLE_NAME + " where "+ COL_SRC + " = ?  AND " + COL_IS_UPLOADED + " = 0";


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] { WATCH_SRC_KEYWORD });

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                FileModel file = new FileModel();
                file.setFileName(cursor.getString(cursor.getColumnIndex(COL_FILE_NAME)));
                file.setSrc(cursor.getString(cursor.getColumnIndex(COL_SRC)));
                file.setUploadedAt(cursor.getString(cursor.getColumnIndex(FileModel.COL_UPLOAD_TIME)));
                file.setIsUploaded(cursor.getInt(cursor.getColumnIndex(COL_IS_UPLOADED)));
                file.setResultGen(cursor.getInt(cursor.getColumnIndex(FileModel.COL_RESULT_GEN)));

                files.add(file);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return files;
    }

    public void updateFileSendStatus(String... args) {
        String fileName = args[0];

        String updateSql = "UPDATE " + FileModel.TABLE_NAME  + " SET " + COL_IS_UPLOADED + " = 1 WHERE " + COL_FILE_NAME + " = ?";



        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(updateSql , new String[] { fileName });

        // close db connection
        db.close();

    }

    private String get_create_command(String table_name, int num_of_cols, String[] cols){
        int col_len = cols.length;
        if (col_len != num_of_cols) throw new IllegalArgumentException("Number of cols and column arrays size are not same");

        String str = "INSERT or replace INTO  " + table_name;
        str += " ( " ;
        for(int i=0; i<col_len; i++){
            String col = cols[i];
            if(i==col_len-1) str += col + " ";
            else str += col + " , ";
        }
        str += " ) ";
        str += " VALUES( ";
        for(int i=0; i<col_len; i++){
            if(i==col_len-1) str += " ? ";
            else str += " ? , ";
        }
        str +=  " )";
        return str;
    }

    public void createProfile(String user_name, String phone_num, String device_id, String regi_id) {


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        Log.d("tag=======", user_name +  phone_num +  device_id +  regi_id);

        values.put(COL_USER_NAME, user_name);
        values.put(COL_PHONE_NUM, phone_num);
        values.put(COL_DEVICE_ID, device_id);
        values.put(COL_REGI_ID, regi_id);
        Log.d("tag=======", values.toString());

        db.insert( ProfileModel.TABLE_NAME, null, values);
        db.close();

    }


    public void createResult(String file_name, String timestamp, String result, String activity, double heart_rate ) {
        Log.d("CREATE", file_name);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FILE_NAME, file_name);
        values.put(COL_RESULT, result);
        values.put(COL_TIMESTAMP, timestamp);
        values.put(COL_AVG_ACTIVITY, activity);
        values.put(COL_AVG_HEART_RATE, heart_rate);

        db.insert( ResultModel.TABLE_NAME, null, values);
        db.close();

    }

    public List<ResultModel> getResults(Integer... args) {
        int limit = args.length > 0 ? args[0] : 0;
        List<ResultModel> results = new ArrayList<>();

        // Select All Query
        String selectQuery = null;
        if(limit==0){
            selectQuery = "SELECT  * FROM " + ResultModel.TABLE_NAME + " ORDER BY " +
                    FileModel.COL_ID + " DESC";
        }else{
            selectQuery = "SELECT  * FROM " + ResultModel.TABLE_NAME + " ORDER BY " +
                    FileModel.COL_ID + "  DESC limit " + limit;
        }


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ResultModel result = new ResultModel();
                result.setFileName(cursor.getString(cursor.getColumnIndex(COL_FILE_NAME)));
                result.setResult(cursor.getString(cursor.getColumnIndex(COL_RESULT)));
                result.setAvg_activity(cursor.getString(cursor.getColumnIndex(COL_AVG_ACTIVITY)));
                result.setAvg_hr(cursor.getDouble(cursor.getColumnIndex(COL_AVG_HEART_RATE)));
                result.setTimestamp(cursor.getString(cursor.getColumnIndex(ResultModel.COL_TIMESTAMP)));


                results.add(result);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return results;
    }

    public List<ResultModel> getResultsOfNHours(Integer... args) {
        int hours = args.length > 0 ? args[0] : 24;
        List<ResultModel> results = new ArrayList<>();

        // Select All Query
        String selectQuery = String.format("SELECT * FROM %s where datetime(%s) >=datetime('now', '-%d Hour') ORDER BY ASC;",  ResultModel.TABLE_NAME, COL_TIMESTAMP,   hours);





        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ResultModel result = new ResultModel();
                result.setFileName(cursor.getString(cursor.getColumnIndex(COL_FILE_NAME)));
                result.setResult(cursor.getString(cursor.getColumnIndex(COL_RESULT)));
                result.setAvg_activity(cursor.getString(cursor.getColumnIndex(COL_AVG_ACTIVITY)));
                result.setAvg_hr(cursor.getDouble(cursor.getColumnIndex(COL_AVG_HEART_RATE)));
                result.setTimestamp(cursor.getString(cursor.getColumnIndex(ResultModel.COL_TIMESTAMP)));


                results.add(result);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return results;
    }


    public ProfileModel get_profile() {

//        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(ProfileModel.TABLE_NAME,
                new String[]{"*"},
                null,
                null, null, null, null, null);
        Log.d("tag=======", "ekhane ashse");

        ProfileModel profile = new ProfileModel();
        if (cursor != null){
            Log.d("tag=======", "null pay nai ");

        }else{
            Log.d("tag=======", "Null paise ");

        }

        if (cursor.moveToFirst()) {
                Log.d("tag=======", cursor.getString(cursor.getColumnIndex(COL_USER_NAME)));

                profile.setUserName(cursor.getString(cursor.getColumnIndex(COL_USER_NAME)));
                profile.setDevice_id(cursor.getString(cursor.getColumnIndex(COL_DEVICE_ID)));
                profile.setPhone_num(cursor.getString(cursor.getColumnIndex(COL_PHONE_NUM)));
                profile.setRegi_id(cursor.getString(cursor.getColumnIndex(COL_REGI_ID)));

        }

        cursor.close();

        return profile;

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