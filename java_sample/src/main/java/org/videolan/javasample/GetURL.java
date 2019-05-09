package org.videolan.javasample;


import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by akimasa on 16/09/04.
 */
public class GetURL implements Runnable {

    android.content.Context ctx;
    Activity activity;
    boolean adaptercalled;
    SwipeRefreshLayout mSwipeRefreshLayout;
    final ArrayList<Chan> recs = new ArrayList<Chan>();

    public GetURL(JavaActivity _that) {
        this.activity = (Activity) _that;

    }

    public void run(){

        try {
            // 大阪の天気予報XMLデータ
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
            String server = sp.getString("server",null);
            if(server == null){
                recs.add(new Chan("setting", "setting"));

                ArrayAdapter<Chan> adapter = new ChanAdapter(activity, recs);
                if (!adaptercalled) {
                    Spinner listView = (Spinner) activity.findViewById(R.id.spinner);
                    listView.setAdapter(adapter);
                    adaptercalled = true;
                    Log.d("hoge", "adapter first time call");
                }
            } else {

                URL url = new URL("http://" + server + "/api/services");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                String str = InputStreamToString(con.getInputStream());
                Log.d("HTTP", str);
                final JSONArray json = new JSONArray(str);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        AddSomething(json, recs);
                        ArrayAdapter<Chan> adapter = new ChanAdapter(activity, recs);
                        if (!adaptercalled) {
                            Spinner listView = (Spinner) activity.findViewById(R.id.spinner);
                            listView.setAdapter(adapter);
                            adaptercalled = true;
                            Log.d("hoge", "adapter first time call");
                        }


                    }
                });
            }

        } catch(Exception ex) {
            Log.d("ex", ex.toString());
        }
    }
    protected void AddSomething(JSONArray json, List<Chan> recs){
        for (int i = 0; i < json.length(); i++) {
            try {
                recs.add(new Chan(json.getJSONObject(i).getString("name"),
                        json.getJSONObject(i).getString("id")));


            } catch (Exception ex) {

            }
        }
        recs.add(new Chan("setting", "setting"));
    }

    // InputStream -> String
    static String InputStreamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}