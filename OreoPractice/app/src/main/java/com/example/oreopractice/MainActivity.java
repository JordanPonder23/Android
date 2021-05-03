package com.example.oreopractice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    DbAdapter dbAdapter;
    ArrayAdapter<String> itemsAdapter;

    ListView listView;

    ArrayList<String> listOfSchoolsArray = new ArrayList<>();

    private class DownloadSchoolTask extends AsyncTask<String,Void,String> {

        private WeakReference<MainActivity> mActivity;
        public DownloadSchoolTask(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        MainActivity activity;

        URL url = null;
        JSONArray satData;

        JSONArray schoolData = new JSONArray();

        private JSONArray mixedData = new JSONArray();

        /*
        * the getSATData and getSchoolData getters provide no real meaningful implementation now
        * but I might build something out that requires them later
        */
        protected JSONArray getSATData() {
            return satData;
        }

        protected JSONArray getSchoolData() {
            return schoolData;
        }

        protected ArrayList<String> getSchoolDataForAdapter() {
            ArrayList<String> sList = new ArrayList<>();
            for(int i = 0; i < this.schoolData.length(); i++ ){
                try {
                    sList.add(new JSONObject(this.schoolData.get(i).toString()).getString("schoolName"));
                }catch (JSONException e) {}
            }
            return sList;
        }

        protected List<String> mixedData(){
            List<String> aList = new ArrayList<>();
            for(int i = 0; i < this.mixedData.length(); i++ ){
                try {
                    aList.add( this.mixedData.get(i).toString() );
                }catch (JSONException e) {}
            }
            return aList;
        }

        private void associateData() throws JSONException{
            JSONArray combined = new JSONArray();
            /*
            * There is no point to having a list item without a mentioend school, so SAT scores will
            * be appended to new view-ready objects of final list on the basis that a school already exists.
            */
            for(int i =0; i < getSchoolData().length(); i++){
                try {
                    Log.i("An-Object",  getSchoolData().get(i).toString());
                }catch (Exception e) {}

                for(int e = 0; e < getSATData().length(); e ++) {
                    JSONObject school = new JSONObject( getSchoolData().get(i).toString());
                    JSONObject satObject = new JSONObject( getSATData().get(e).toString());
                    if(school.getString("dbn").equals(satObject.getString("dbn"))) {
                        JSONObject combinedObject = new JSONObject();
                        combinedObject.put("dbn",school.getString("dbn"));
                        combinedObject.put("schoolName", school.getString("schoolName"));
                        combinedObject.put("boro", school.getString("boro"));
                        combinedObject.put("overview_paragraph", school.getString("overview_paragraph"));
                        combinedObject.put("location", school.getString("location"));
                        combinedObject.put("website", school.getString("website"));
                        combinedObject.put("phone_number", school.getString("phone_number"));
                        combinedObject.put("takers", satObject.getString("takers"));
                        combinedObject.put("reading", satObject.getString("reading"));
                        combinedObject.put("math", satObject.getString("math"));
                        combinedObject.put("writing", satObject.getString("writing"));

                        combined.put(combinedObject);
                    }
                }
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            HttpURLConnection urlConnection = null;

            this.satData = new JSONArray();
            this.schoolData = new JSONArray();

            /*
            * Loop through multiple URLS
            *
            * This will make for an easy update process in case there are
            * new data sources (eg. ACT scores) that are to be appended in the future
            *
            * Only downside is that SAT and School list are not being fetched at same time
            * (this could have been down by having a separate ASYNC task performed for SAT scores
            * and executed adjacent to schools being fetched..)
            */
            for(int e =0; e < urls.length; e++) {
                try {

                    this.url = new URL(urls[e]);

                    urlConnection = (HttpURLConnection) this.url.openConnection();
                    urlConnection.setInstanceFollowRedirects(true);
                    urlConnection.setUseCaches(true);
                    urlConnection.setDefaultUseCaches(true);

                    InputStream in = urlConnection.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result += line;
                    }

                    Log.i("result: ", result);

                    return result;

                } catch (Exception err) {
                    err.printStackTrace();
                    Log.e("Error Log: ", err.getClass().toString());
                    return "error: "+err.getClass();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.activity = mActivity.get();
            if (activity != null) {
                activity.listView = (ListView) activity.findViewById(R.id.listViewSchool);
                activity.itemsAdapter =  new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, activity.listOfSchoolsArray);
                activity.listView.setAdapter(itemsAdapter);
            }

            try {
                Log.i("PostExecute",s);
                JSONArray jsonList = new JSONArray(s);

                if (this.url.toString().contains("s3k6")) {
                    persistSchoolData(jsonList);
                } else if(this.url.toString().contains("f9bf")){
                    persistSATData(jsonList) ;
                }
                associateData();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //SAT json data is persisted as initially provided by resource
        private void persistSATData (JSONArray satList) throws JSONException {
            for(int i = 0; i < satList.length(); i++) {
                JSONObject row = satList.getJSONObject(i);
                long id = dbAdapter.insertSATData(row.getString("dbn"),
                        row.getString("school_name"),
                        row.getString("num_of_sat_test_takers"),
                        row.getString("sat_critical_reading_avg_score"),
                        row.getString("sat_math_avg_score"),
                        row.getString("sat_writing_avg_score"));
                JSONObject data = dbAdapter.getSATData();
                this.satData.put(data);
                Log.i("SATData", getSATData().toString() );
            }
        }

        //School json data is persisted as initially provided by resource
        private void persistSchoolData(JSONArray dbSchoolList) throws JSONException {
            for(int i = 0; i < dbSchoolList.length(); i++){
                JSONObject row = dbSchoolList.getJSONObject(i);
                long id = dbAdapter.insertSchoolData(row.getString("dbn"),
                        row.getString("school_name"),
                        row.getString("boro"),
                        row.getString("overview_paragraph"),
                        row.getString("location"),
                        row.getString("website"),
                        row.getString("phone_number"));
                JSONObject data = dbAdapter.getSchoolData();
                this.schoolData.put(data);
                activity.listOfSchoolsArray.add( getSchoolDataForAdapter().get(i) );
               /*
                * A lot of this was done with loading the list items asynchronously as they are
                * returned from DB. After further research, this is a little tougher than MVP
                * than I original thought.
                */
                activity.listView.invalidateViews();
                activity.itemsAdapter.notifyDataSetChanged();

                Log.i("SchoolData", getSchoolData().toString());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("main", "At entry point");
        DownloadSchoolTask task = new DownloadSchoolTask(this);
        dbAdapter = new DbAdapter(this);
        Toast.makeText(getApplicationContext(), "Eager Loading School and SAT data", Toast.LENGTH_LONG).show();

        task.execute( "https://data.cityofnewyork.us/resource/s3k6-pzi2.json", "https://data.cityofnewyork.us/resource/f9bf-2cp4.json");
    }
}