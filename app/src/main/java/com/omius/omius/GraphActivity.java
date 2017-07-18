// Sign up and Sign in

//package com.omius.omius;
//
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}
package com.omius.omius;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

public class GraphActivity extends AppCompatActivity {
    private BluetoothAdapter mBtAdapter;
    Button stopBluetooth = null;
    Button connectBluetooth = null;
    Button saveImage = null;

    private StringBuilder recDataString = new StringBuilder();
    private BluetoothSerial btOmius = null;

    String coordtime = null;
    String coordsens0 = null;
    String coordsens1 = null;
    String coordsens2 = null;
    String coordsens3 = null;
    String coordsens4 = null;
    String coordsens5 = null;

    String eraseSub;
    int lineEnding;
    int pointer = 0;
    boolean isGraphEnabled = true;

    List<Entry> entries = new ArrayList<Entry>();
    List<Entry> entriesTemp1 = new ArrayList<Entry>();
    List<Entry> entriesTemp2 = new ArrayList<Entry>();
    List<Entry> entriesTemp3 = new ArrayList<Entry>();
    List<Entry> entriesTemp4 = new ArrayList<Entry>();
    List<Entry> entriesTemp5 = new ArrayList<Entry>();

    int graphPoints = 0;

    public ArrayList<String> PointsToParse = new ArrayList<String>();

    public ArrayList<Float> PointsTemp0 = new ArrayList<Float>();
    public ArrayList<Float> PointsTemp1 = new ArrayList<Float>();
    public ArrayList<Float> PointsTemp2 = new ArrayList<Float>();
    public ArrayList<Float> PointsTemp3 = new ArrayList<Float>();
    public ArrayList<Float> PointsTemp4 = new ArrayList<Float>();
    public ArrayList<Float> PointsTemp5 = new ArrayList<Float>();

    public LineChart chart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stopBluetooth = (Button) findViewById(R.id.btnStopBluetooth);
        connectBluetooth = (Button) findViewById(R.id.btnConnectBluetooth);
        saveImage = (Button) findViewById(R.id.btnSaveImage);

        stopBluetooth.setEnabled(false);
        saveImage.setEnabled(false);

        chart = (LineChart) findViewById(R.id.Temperature_chart);
        PointsTemp0.clear();
        PointsTemp1.clear();
        PointsTemp2.clear();
        PointsTemp3.clear();
        PointsTemp4.clear();
        PointsTemp5.clear();

        checkBTState();

        btOmius = new BluetoothSerial(this,new BluetoothSerial.MessageHandler() {
            @Override
            public int read(int bufferSize, byte[] buffer){
                int respReturn = 0;
                final String TAG = "MessageHandler";
                String readMessage = new String(buffer);                          // msg.arg1 = bytes from connect thread
                lineEnding = readMessage.indexOf("\r\n");
                if (lineEnding != -1){
                    recDataString = new StringBuilder(readMessage.substring(0,lineEnding));
                    while(recDataString.indexOf(",") > 0){
                        PointsToParse.add(recDataString.substring(0,recDataString.indexOf(",")));
                        try {
                            recDataString.delete(0, recDataString.indexOf(",")+1);                    //clear all string data
                        } catch (StringIndexOutOfBoundsException ex) {
                            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    PointsTemp0.add(Float.parseFloat(PointsToParse.get(pointer)));
                    PointsTemp0.add(Float.parseFloat(PointsToParse.get(pointer + 1)));
                    PointsTemp1.add(Float.parseFloat(PointsToParse.get(pointer)));
                    PointsTemp1.add(Float.parseFloat(PointsToParse.get(pointer + 2)));
                    PointsTemp2.add(Float.parseFloat(PointsToParse.get(pointer)));
                    PointsTemp2.add(Float.parseFloat(PointsToParse.get(pointer + 3)));
                    PointsTemp3.add(Float.parseFloat(PointsToParse.get(pointer)));
                    PointsTemp3.add(Float.parseFloat(PointsToParse.get(pointer + 4)));
                    PointsTemp4.add(Float.parseFloat(PointsToParse.get(pointer)));
                    PointsTemp4.add(Float.parseFloat(PointsToParse.get(pointer + 5)));
                    PointsTemp5.add(Float.parseFloat(PointsToParse.get(pointer)));
                    PointsTemp5.add(Float.parseFloat(PointsToParse.get(pointer + 6)));

                    Log.d(TAG, "Graph Enabled: " + String.valueOf(isGraphEnabled));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isGraphEnabled) {
                                RefreshGraph();
                                Log.d(TAG, "Entered refresh graph.");
                            }
                        }
                    });

                    pointer = pointer + 7;
                    respReturn = lineEnding + 2;
                }
                return respReturn;
            }
        },"OMIUS");

        stopBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGraphEnabled = false;
                new SendPOSTrequest().execute();
                saveImage.setEnabled(true);

            }
        });

        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence charSequence = new String ("yyyMMddHHmmss");
                String timeStamp = new DateFormat().format(charSequence, new Date()).toString();
                String graphTitle = "IMG_" + timeStamp;
                chart.saveToGallery(graphTitle, 30);
                File root = Environment.getExternalStorageDirectory();
                String imgbmp = root + "/DCIM/" + graphTitle + ".jpg";
                Bitmap bMap = BitmapFactory.decodeFile(imgbmp);
                ImageUploadHandler imgUpload = new ImageUploadHandler();
                imgUpload.setOnVariables(null, bMap, "graph");
                imgUpload.uploadImage(GraphActivity.this);
                Toast.makeText(getApplicationContext(), "Graph image uploaded", Toast.LENGTH_SHORT).show();
                RestartApp();
            }
        });

        connectBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGraphEnabled = true;
                stopBluetooth.setEnabled(true);
            }
        });
    }

    public void RefreshGraph() {
        final String TAG = "RefreshGraph";
        if (isGraphEnabled) {
            entries.add(new Entry(PointsTemp0.get(graphPoints), PointsTemp0.get(graphPoints + 1)));
            entriesTemp1.add(new Entry(PointsTemp1.get(graphPoints), PointsTemp1.get(graphPoints + 1)));
            entriesTemp2.add(new Entry(PointsTemp2.get(graphPoints), PointsTemp2.get(graphPoints + 1)));
            entriesTemp3.add(new Entry(PointsTemp3.get(graphPoints), PointsTemp3.get(graphPoints + 1)));
            entriesTemp4.add(new Entry(PointsTemp4.get(graphPoints), PointsTemp4.get(graphPoints + 1)));
            entriesTemp5.add(new Entry(PointsTemp5.get(graphPoints), PointsTemp5.get(graphPoints + 1)));
            LineDataSet datasetTempCorp0 = new LineDataSet(entries, "Antebrazo Izquierdo");
            datasetTempCorp0.setColor(Color.BLUE);
            datasetTempCorp0.setCircleColor(Color.BLUE);
            LineDataSet datasetTempCorp1 = new LineDataSet(entriesTemp1, "Antebrazo Derecho");
            datasetTempCorp1.setColor(Color.CYAN);
            datasetTempCorp1.setCircleColor(Color.CYAN);
            LineDataSet datasetTempCorp2 = new LineDataSet(entriesTemp2, "Brazo Izquierdo");
            datasetTempCorp2.setColor(Color.MAGENTA);
            datasetTempCorp2.setCircleColor(Color.MAGENTA);
            LineDataSet datasetTempCorp3 = new LineDataSet(entriesTemp3, "Brazo Derecho");
            datasetTempCorp3.setColor(Color.GRAY);
            datasetTempCorp3.setCircleColor(Color.GRAY);
            LineDataSet datasetTempCorp4 = new LineDataSet(entriesTemp4, "Pectoral Izquierdo");
            datasetTempCorp4.setColor(Color.GREEN);
            datasetTempCorp4.setCircleColor(Color.GREEN);
            LineDataSet datasetTempCorp5 = new LineDataSet(entriesTemp5, "Pectoral Derecho");
            datasetTempCorp5.setColor(Color.RED);
            datasetTempCorp5.setCircleColor(Color.RED);
            List<ILineDataSet> dataset = new ArrayList<ILineDataSet>();
            dataset.add(datasetTempCorp0);
            dataset.add(datasetTempCorp1);
            dataset.add(datasetTempCorp2);
            dataset.add(datasetTempCorp3);
            dataset.add(datasetTempCorp4);
            dataset.add(datasetTempCorp5);
            LineData lineData = new LineData(dataset);
            chart.setData(lineData);
            setChartOptions();
            graphPoints = graphPoints + 2;
            Log.d(TAG,"Points added: " + graphPoints/2);
        }
    }

    public void setChartOptions(){
        Description desc = new Description();
        desc.setText("® Omius, 2017.");
        desc.setTextColor(Color.WHITE);
        Legend legend = chart.getLegend();
        legend.setTextColor(Color.WHITE);
        chart.setVisibleXRangeMinimum(0.00f);
        chart.setVisibleXRangeMaximum(30.0f);
        XAxis xaxis = chart.getXAxis();
        YAxis leftyaxis = chart.getAxisLeft();
        YAxis rightyaxis = chart.getAxisRight();
        leftyaxis.setAxisMinimum(-1.00f);
        leftyaxis.setAxisMaximum(105.0f);
        rightyaxis.setAxisMinimum(-1.00f);
        rightyaxis.setAxisMaximum(105.0f);
        leftyaxis.setTextColor(Color.WHITE);
        rightyaxis.setTextColor(Color.WHITE);
        xaxis.setTextColor(Color.WHITE);
        chart.setDescription(desc);
        //chart.fitScreen();
        chart.invalidate(); // refresh
    }

    public void RestartApp(){
        Intent mStartActivity = new Intent(this, RegisterActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        android.os.Process.killProcess(android.os.Process.myPid()); //System.exit(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        btOmius.onResume();
    }

    //method to check if the device has Bluetooth and if it is on.
    //Prompts the user to turn it on if it is off

    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        mBtAdapter = BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!mBtAdapter.isEnabled()) {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class SendPOSTrequest extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {}
        protected String doInBackground(String...params) {
            try {
                // Create URL and JSON Object
                //We will use URLconnection for HTTP to send and receive data
                URL url = new URL("http://planz.omiustech.com/bombaacida.php");
                JSONObject postDataparams = new JSONObject();
                postDataparams.put("Tipo", "log");
                for(int i = 0; i < PointsTemp5.size(); i = i + 2){
                    postDataparams.put("Tiempo" + i/2, PointsTemp0.get(i));
                    postDataparams.put("Sensor0" + i/2, PointsTemp0.get(i + 1));
                    postDataparams.put("Sensor1" + i/2, PointsTemp1.get(i + 1));
                    postDataparams.put("Sensor2" + i/2, PointsTemp2.get(i + 1));
                    postDataparams.put("Sensor3" + i/2, PointsTemp3.get(i + 1));
                    postDataparams.put("Sensor4" + i/2, PointsTemp4.get(i + 1));
                    postDataparams.put("Sensor5" + i/2, PointsTemp5.get(i + 1));
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
                    return sb.toString();
                } else { return new String("false: " + responseCode);}
            } catch (Exception e) { return new String("Exception: " + e.getMessage()); }
        }

        //Get response
        @Override
        protected void onPostExecute(String result) {
             Toast.makeText(getApplicationContext(), "Saved!",
                    Toast.LENGTH_LONG).show();
        }
    }

    //Method to convert JSON Obect to encode url string format
    public String getPostDataString(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();

        while (itr.hasNext()){
            String key = itr.next();
            Object value = params.get(key);

            if (first){ first = false;
            } else { result.append("&");}

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }
}
