package com.example.oreopractice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class DbAdapter {
    myDbHelper myhelper;

    public DbAdapter(Context context)
    {
        myhelper = new myDbHelper(context);
    }

    public long insertSchoolData(String id, String school_name, String boro, String overview, String location, String website, String phone)
    {
        SQLiteDatabase dbb = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(myDbHelper.DBN, id);
        contentValues.put(myDbHelper.SCHOOL_NAME, school_name);
        contentValues.put(myDbHelper.BORO, boro);
        contentValues.put(myDbHelper.OVERVIEW, overview);
        contentValues.put(myDbHelper.LOCATION, location);
        contentValues.put(myDbHelper.WEBSITE, website);
        contentValues.put(myDbHelper.PHONE, phone);

        long idBack = dbb.insert(myDbHelper.TABLE_NAME, null , contentValues);
        return idBack;
    }

    public long insertSATData(String id, String school_name, String takers, String reading, String math, String writing)
    {
        SQLiteDatabase dbb = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(myDbHelper.DBN, id);
        contentValues.put(myDbHelper.SCHOOL_NAME, school_name);
        contentValues.put(myDbHelper.TEST_TAKERS, takers);
        contentValues.put(myDbHelper.READING_AVG_SCORE, reading);
        contentValues.put(myDbHelper.MATH_AVG_SCORE, math);
        contentValues.put(myDbHelper.WRITING_AVG_SCORE, writing);
        long idBack = dbb.insert(myDbHelper.SAT_TABLE_NAME, null , contentValues);
        return idBack;
    }

    public JSONObject getSATData() throws JSONException {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] columns = {myDbHelper.DBN,myDbHelper.SCHOOL_NAME,myDbHelper.TEST_TAKERS,
                myDbHelper.READING_AVG_SCORE,myDbHelper.MATH_AVG_SCORE,myDbHelper.WRITING_AVG_SCORE};
        Cursor cursor =db.query(myDbHelper.SAT_TABLE_NAME,columns,null,null,null,null,null);
        JSONObject sat = new JSONObject();
        while (cursor.moveToNext())
        {
            int dbn =cursor.getInt(cursor.getColumnIndex(myDbHelper.DBN));
            String schoolName =cursor.getString(cursor.getColumnIndex(myDbHelper.SCHOOL_NAME));
            String takers =cursor.getString(cursor.getColumnIndex(myDbHelper.TEST_TAKERS));
            String reading =cursor.getString(cursor.getColumnIndex(myDbHelper.READING_AVG_SCORE));
            String math =cursor.getString(cursor.getColumnIndex(myDbHelper.MATH_AVG_SCORE));
            String write =cursor.getString(cursor.getColumnIndex(myDbHelper.WRITING_AVG_SCORE));

            sat.put("dbn", dbn);
            sat.put("schoolName", schoolName);
            sat.put("takers", takers);
            sat.put("reading", reading);
            sat.put("math", math);
            sat.put("writing", write);

        }
        return sat;
    }

    public JSONObject getSchoolData() throws JSONException
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] columns = {myDbHelper.DBN,myDbHelper.SCHOOL_NAME,myDbHelper.BORO,myDbHelper.OVERVIEW,myDbHelper.LOCATION,myDbHelper.WEBSITE, myDbHelper.PHONE};
        Cursor cursor =db.query(myDbHelper.TABLE_NAME,columns,null,null,null,null,null);
        JSONObject school = new JSONObject();
        while (cursor.moveToNext())
        {
            int dbn =cursor.getInt(cursor.getColumnIndex(myDbHelper.DBN));
            String schoolName =cursor.getString(cursor.getColumnIndex(myDbHelper.SCHOOL_NAME));
            String  boro =cursor.getString(cursor.getColumnIndex(myDbHelper.BORO));
            String  overview =cursor.getString(cursor.getColumnIndex(myDbHelper.OVERVIEW));
            String  location =cursor.getString(cursor.getColumnIndex(myDbHelper.LOCATION));
            String  website =cursor.getString(cursor.getColumnIndex(myDbHelper.WEBSITE));
            String  phone =cursor.getString(cursor.getColumnIndex(myDbHelper.PHONE));

            school.put("dbn", dbn);
            school.put("schoolName", schoolName);
            school.put("boro", boro);
            school.put("overview_paragraph", overview);
            school.put("location", location);
            school.put("website", website);
            school.put("phone_number", phone);
        }
        return school;
    }

    public  int delete(String uname)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] whereArgs ={uname};

        int count =db.delete(myDbHelper.TABLE_NAME ,myDbHelper.SCHOOL_NAME+" = ?",whereArgs);
        return  count;
    }

    public int updateName(String oldName , String newName)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(myDbHelper.SCHOOL_NAME,newName);
        String[] whereArgs= {oldName};
        int count =db.update(myDbHelper.TABLE_NAME,contentValues, myDbHelper.SCHOOL_NAME+" = ?",whereArgs );
        return count;
    }

    static class myDbHelper extends SQLiteOpenHelper
    {
        private static final int DATABASE_Version = 3;
        private static final String DATABASE_NAME = "NYC-Database";
        private static final String TABLE_NAME = "SCHOOL_TABLE";
        //Was going to initially use DBN as primary key for both tables, but apparently it is not unique to all values in SAT data set
        private static final String UID="_id";
        private static final String DBN ="dbn";

        private static final String SCHOOL_NAME = "school_name";
        private static final String BORO= "boro";
        private static final String OVERVIEW= "overview_paragraph";
        private static final String LOCATION= "location";
        private static final String WEBSITE= "website";
        private static final String PHONE= "phone_number";
        private static final String CREATE_SCHOOL_TABLE = "CREATE TABLE "+TABLE_NAME+
                "("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+DBN+" VARCHAR(255), "+SCHOOL_NAME+" VARCHAR(255), "+ BORO+" VARCHAR(255), "
                +OVERVIEW +" VARCHAR(255), " +LOCATION+" VARCHAR(255), " +WEBSITE+" VARCHAR(255), " +PHONE +" VARCHAR(255));";

        private static final String SAT_TABLE_NAME = "SAT_TABLE";
        private static final String TEST_TAKERS = "NUM_OF_TEST_TAKERS";
        private static final String READING_AVG_SCORE = "CRIT_READING_AVG_SCORE";
        private static final String MATH_AVG_SCORE = "MATH_AVG_SCORE";
        private static final String WRITING_AVG_SCORE = "WRITING_AVG_SCORE";
        private static final String CREATE_SAT_TABLE = "CREATE TABLE "+SAT_TABLE_NAME+
                "("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+DBN+" VARCHAR(255), "+SCHOOL_NAME+" VARCHAR(255), "+ TEST_TAKERS+" VARCHAR(255), "
                + READING_AVG_SCORE+ " VARCHAR(255), " +MATH_AVG_SCORE +" VARCHAR(255), "
                + WRITING_AVG_SCORE+" VARCHAR(255));";

        private static final String DROP_SCHOOL_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;
        private static final String DROP_SAT_TABLE = "DROP TABLE IF EXISTS "+ SAT_TABLE_NAME;
        private Context context;

        public myDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
            this.context=context;
        }

        public void onCreate(SQLiteDatabase db) {

            try {
                db.execSQL(CREATE_SCHOOL_TABLE);
                db.execSQL(CREATE_SAT_TABLE);
            } catch (Exception e) {
                Log.i("DBCreateError",""+e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                Log.i("DBUpdateStart","OnUpgrade");
                db.execSQL(DROP_SCHOOL_TABLE);
                db.execSQL(DROP_SAT_TABLE);
                onCreate(db);
            }catch (Exception e) {
                Log.e("DBUpdateError",""+e);
            }
        }
    }
}