package com.omius.omius;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by jfert on 22/07/2017.
 */

public class HttpPostService extends IntentService {
    final String TAG = "HttpPostService";

    public HttpPostService(){
        super("HttpPostService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG,"Service Started.");

        ArrayList<Float> listTemp0 = (ArrayList<Float>) intent.getSerializableExtra("Temp0List");
        ArrayList<Float> listTemp1 = (ArrayList<Float>) intent.getSerializableExtra("Temp1List");
        ArrayList<Float> listTemp2 = (ArrayList<Float>) intent.getSerializableExtra("Temp2List");
        ArrayList<Float> listTemp3 = (ArrayList<Float>) intent.getSerializableExtra("Temp3List");
        ArrayList<Float> listTemp4 = (ArrayList<Float>) intent.getSerializableExtra("Temp4List");
        ArrayList<Float> listTemp5 = (ArrayList<Float>) intent.getSerializableExtra("Temp5List");

        try {
            // Create URL and JSON Object
            //We will use URLconnection for HTTP to send and receive data
            URL url = new URL("http://planz.omiustech.com/bombaacida.php");
            JSONObject postDataparams = new JSONObject();
            postDataparams.put("Tipo", "log");
            for(int i = 0; i < listTemp5.size(); i = i + 2){
                postDataparams.put("Tiempo" + i/2, listTemp0.get(i));
                postDataparams.put("Sensor0" + i/2, listTemp0.get(i + 1));
                postDataparams.put("Sensor1" + i/2, listTemp1.get(i + 1));
                postDataparams.put("Sensor2" + i/2, listTemp2.get(i + 1));
                postDataparams.put("Sensor3" + i/2, listTemp3.get(i + 1));
                postDataparams.put("Sensor4" + i/2, listTemp4.get(i + 1));
                postDataparams.put("Sensor5" + i/2, listTemp5.get(i + 1));
            }

            HttpURLConnection httpclient = (HttpURLConnection) url.openConnection();
            httpclient.setReadTimeout(15000);
            httpclient.setConnectTimeout(15000);
            httpclient.setRequestProperty("Content-Type", "application/json");
            httpclient.setRequestProperty("Accept", "application/json");
            httpclient.setRequestMethod("POST");
            httpclient.setDoOutput(true);
            httpclient.setDoInput(true);

            // Get response
            OutputStream os = httpclient.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(postDataparams.toString());
            writer.flush();
            writer.close();
            os.close();

            int responseCode = httpclient.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(httpclient.getInputStream()));
                StringBuffer sb = new StringBuffer("");
                String line = "";

                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    //break;
                }
                in.close();

                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);

                Notification notification = new Notification.Builder(this)
                        .setContentTitle("Omius Post Service")
                        .setContentText("Sending post data to server...")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .setTicker("Omius")
                        .build();

                startForeground(1, notification);
            } else { PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);

                Notification notification = new Notification.Builder(this)
                        .setContentTitle("Omius Post Service")
                        .setContentText("Sending post data to server but..." + responseCode)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .setTicker("Omius")
                        .build();

                startForeground(2, notification);}
        } catch (Exception e) { PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);

            Notification notification = new Notification.Builder(this)
                    .setContentTitle("Omius Post Service")
                    .setContentText("Exception: " + e.getMessage())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setTicker("Omius")
                    .build();

            startForeground(3, notification); }
    }
}
