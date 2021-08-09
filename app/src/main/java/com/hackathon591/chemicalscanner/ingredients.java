package com.hackathon591.chemicalscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.xdrop.fuzzywuzzy.FuzzySearch;

public class ingredients extends AppCompatActivity {
    ArrayList<String> myList = null;
    ArrayList<String> finallist;
    ListView listView;
    HashMap myfirebaseData;
    CustomAdapter customAdapter;
    String combine_ingre = "";
    private static final String TAG = "ScanIngredients";


    ArrayList tempfuzz;
    String[] scanarr;
    double score = 0;
    AsyncTaskRunner runner = new AsyncTaskRunner();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);


        listView = (ListView) findViewById(R.id.listView);
        finallist = new ArrayList<>();
        finallist.add(" ");

        myList = (ArrayList<String>) getIntent().getSerializableExtra("mylist");


        if (myList == null) {
            Log.d(TAG, "No Data Captured , try again");
            return;
        } else {

            Log.d(TAG, "length of scanned list:" + myList.size());

            for (int i = 0; i < myList.size(); i++) {
                Log.d(TAG, myList.get(i));


                combine_ingre = combine_ingre +" ," + myList.get(i);
                combine_ingre = combine_ingre.toLowerCase();

            }

            myfirebaseData = (HashMap<String, String>) getIntent().getSerializableExtra("myfirebaseData");

            compareIngre(myfirebaseData, combine_ingre);
            customAdapter = new CustomAdapter();


        }
    }

    private void compareIngre(Map<String, Object> fbdatas, String combine_ingre) {
//        Log.d(TAG, "compareIngre: firebasedata ironman" + fbdatas);
        tempfuzz = new ArrayList();


        Log.d(TAG, "compareIngre: combine_ingre " + combine_ingre);
        scanarr = combine_ingre.split("\\s+");
        Log.d(TAG, "compareIngre: scanarr " + Arrays.toString(scanarr));
        Log.d(TAG, "compareIngre: scanarr " + scanarr.length);
        runner.execute();
        myList.clear();


    }


    public void onResume() {
        super.onResume();


    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return finallist.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = getLayoutInflater().inflate(R.layout.customlayout, null);
            TextView textView = (TextView) convertView.findViewById(R.id.textView);
            textView.setText(finallist.get(position));
            if (finallist.get(position).contains("Healthy")) {
                textView.setTextColor(Color.parseColor("#27ae60"));
            }
            if (finallist.get(position).contains("Safe")) {
                textView.setTextColor(Color.parseColor("#27ae60"));
            }
            if (finallist.get(position).contains("Harm")) {
                textView.setTextColor(Color.parseColor("#e74c3c"));
            }

            if (finallist.get(position).contains("Toxic")) {
                textView.setTextColor(Color.parseColor("#e74c3c"));
            }

            return convertView;
        }
    }


    private class AsyncTaskRunner extends AsyncTask<String, String, String> {


        Map<String, Object> fbdatas;

        @Override
        protected void onPreExecute() {

            fbdatas = myfirebaseData;
            Log.d(TAG, "AsyncTaskRunner: fbdatas " + fbdatas.size());
            Log.d(TAG, "AsyncTaskRunner: scanarr " + scanarr.getClass());

        }


        @Override
        protected String doInBackground(String... params) {

            try {


                for (Map.Entry<String, Object> fbdata : fbdatas.entrySet()) {

                    //Get user map
                    String fbkey = fbdata.getKey().toLowerCase();
                    String state = fbdata.getValue().toString();


                    fbkey = fbkey.toLowerCase().trim();


                    for (int i = 0; i < scanarr.length; i++) {
                        if (scanarr[i].length() > 3 && i < scanarr.length - 3) {

                            score = FuzzySearch.partialRatio(fbkey.replaceAll("\\s+", ""), (scanarr[i] + scanarr[i + 1] + scanarr[i + 2]).replaceAll("\\s+", ""));

                            if (score > 86) {
                                tempfuzz.add(fbkey + ": " + state);
                                Log.d(TAG, fbkey.replaceAll("\\s+", "") + " : IRONMAN : " + scanarr[i] + scanarr[i + 1] + scanarr[i + 2] + "#" + score);
                            }

                        }


                    }


                }


                Log.d(TAG, "AsyncTaskRunner finallist  :" + finallist.toString());

                finallist = tempfuzz;
                // remove duplicates
                Set<String> set = new HashSet<>(finallist);
                finallist.clear();
                finallist.addAll(set);

            } catch (Exception e) {
                Log.e(TAG, "AsyncTaskRunner Error", e);
            }


            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
//            ConnectivityManager cm =
//                    (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//
//            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//            boolean isConnected = activeNetwork != null &&
//                    activeNetwork.isConnectedOrConnecting();
//            if (!isConnected) {
//
//            }
            Log.d(TAG, "Fuzz search AsyncTaskRunner  :" + finallist);


            listView.setAdapter(customAdapter);

        }


    }
}