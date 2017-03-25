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
import java.util.UUID;

public class GraphActivity extends AppCompatActivity {

    private BluetoothAdapter mBtAdapter;
    private BluetoothSocket btSocket;
    public String BtAddress = null;
    public ConnectedThread mConnectedThread = null;
    final int handlerState = 0;
    Button stopBluetooth = null;
    Button connectBluetooth = null;
    Button saveImage = null;

    // UUID service - This is the type of Bluetooth device that the BT module is
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Handler bluetoothIn;
    private StringBuilder recDataString = new StringBuilder();
    String[] valuesPOST = new String[3];

    String coord1 = null;
    String coord2 = null;
    String coordtemp = null;
    String coordhum = null;

    String eraseSub;
    int lineEnding;
    boolean isGraphEnabled = true;
    boolean isBTAdaptSelected = false;

//    DataPoint[] dataBattery = new DataPoint[]{new DataPoint(0, 0)};
//    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataBattery);

    List<Entry> entries = new ArrayList<Entry>();
    List<Entry> entriesTemp = new ArrayList<Entry>();
    List<Entry> entriesHum = new ArrayList<Entry>();

    int graphPoints = 0;

    public ArrayList<Float> Points = new ArrayList<Float>();
    public ArrayList<Float> PointsTemp = new ArrayList<Float>();
    public ArrayList<Float> PointsHum = new ArrayList<Float>();

    public LineChart chart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        graph = (GraphView) findViewById(R.id.graph);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        stopBluetooth = (Button) findViewById(R.id.btnStopBluetooth);
        connectBluetooth = (Button) findViewById(R.id.btnConnectBluetooth);
        saveImage = (Button) findViewById(R.id.btnSaveImage);


        stopBluetooth.setEnabled(false);
        saveImage.setEnabled(false);

        chart = (LineChart) findViewById(R.id.Temperature_chart);
        Points.clear();
        PointsTemp.clear();
        PointsHum.clear();

        //It is best to check BT status at onResume in case something has changed while app was paused etc
        checkBTState();


//        graph.addSeries(series);
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~

                    lineEnding = recDataString.indexOf("\r\n");
                    if (lineEnding > 0)
                        recDataString.replace(lineEnding,lineEnding+4,"");
                    int endOfLineIndex = recDataString.indexOf(",");                    // determine the end-of-line
                    if (coord1 == null) {
                        if (endOfLineIndex > 0) {                                           // make sure there data before ~
                            //String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                            //int dataLength = dataInPrint.length();                          //get length of data received

                            String chk = recDataString.substring(0, endOfLineIndex);

                            if (chk.equals("0")) {
//                                Toast.makeText(getBaseContext(),chk,Toast.LENGTH_SHORT).show();
                                eraseSub = recDataString.substring(0, endOfLineIndex + 1);
                                int i = recDataString.indexOf(eraseSub);
                                if (i != -1) {
                                    try {
                                        recDataString.delete(i, eraseSub.length());                    //clear all string data
                                    } catch (StringIndexOutOfBoundsException ex) {
                                        Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
//                                Toast.makeText(getBaseContext(),chk,Toast.LENGTH_SHORT).show();
                                coord1 = chk;
                                eraseSub = recDataString.substring(0, endOfLineIndex + 1);
                                int i = recDataString.indexOf(eraseSub);
                                if (i != -1) {
                                    try {
                                        recDataString.delete(i, eraseSub.length());                    //clear all string data
                                    } catch (StringIndexOutOfBoundsException ex) {
                                        Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    } else {
                        if (coord2 == null) {
                            if (endOfLineIndex > 0) {                                           // make sure there data before ~
                                String chk = recDataString.substring(0, endOfLineIndex);
//                                Toast.makeText(getBaseContext(),chk,Toast.LENGTH_SHORT).show();
                                coord2 = chk;

                                eraseSub = recDataString.substring(0, endOfLineIndex + 1);
                                int i = recDataString.indexOf(eraseSub);
                                if (i != -1) {
                                    try {
                                        recDataString.delete(i, eraseSub.length());                    //clear all string data
                                    } catch (StringIndexOutOfBoundsException ex) {
                                        Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                Points.add(Float.parseFloat(coord1));
                                Points.add(Float.parseFloat(coord2));
//                                coord1 = null;
//                                coord2 = null;
//                                RefreshGraph(chart);
                            }
                        } else {
                            if (coordtemp == null){
                                if (endOfLineIndex > 0) {                               // make sure there data before ~
                                    String chk = recDataString.substring(0, endOfLineIndex);
//                                Toast.makeText(getBaseContext(),chk,Toast.LENGTH_SHORT).show();
                                    coordtemp = chk;

                                    eraseSub = recDataString.substring(0, endOfLineIndex + 1);
                                    int i = recDataString.indexOf(eraseSub);
                                    if (i != -1) {
                                        try {
                                            recDataString.delete(i, eraseSub.length());                    //clear all string data
                                        } catch (StringIndexOutOfBoundsException ex) {
                                            Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    PointsTemp.add(Float.parseFloat(coord1));
                                    PointsTemp.add(Float.parseFloat(coordtemp));

//                                coord1 = null;
//                                coord2 = null;

//                                RefreshGraph(chart);
                                }
                            } else {
                                if (coordhum == null){
                                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                                        String chk = recDataString.substring(0, endOfLineIndex);
//                                          Toast.makeText(getBaseContext(),chk,Toast.LENGTH_SHORT).show();
                                        coordhum = chk;

                                        eraseSub = recDataString.substring(0, endOfLineIndex + 1);
                                        int i = recDataString.indexOf(eraseSub);
                                        if (i != -1) {
                                            try {
                                                recDataString.delete(i, eraseSub.length());                    //clear all string data
                                            } catch (StringIndexOutOfBoundsException ex) {
                                                Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        PointsHum.add(Float.parseFloat(coord1));
                                        PointsHum.add(Float.parseFloat(coordhum));

                                        coord1 = null;
                                        coord2 = null;
                                        coordtemp = null;
                                        coordhum = null;

                                        if (isGraphEnabled){
                                            RefreshGraph();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };

        stopBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mConnectedThread.mmInStream.close();
                    mConnectedThread.mmOutStream.close();
                    mConnectedThread.running = false;
                    btSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //bluetoothIn.removeMessages(0);
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
                try{
                    if (mConnectedThread != null){
                        mConnectedThread.mmInStream.close();
                        mConnectedThread.mmOutStream.close();
                        mConnectedThread.running = false;
                        mConnectedThread = null;
                        btSocket.close();
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                }
                bluetoothIn.removeMessages(0);
                graphPoints = 0;
                Points.clear();
                PointsTemp.clear();
                PointsHum.clear();
                //chart.clearValues();
                //chart.invalidate();
                isGraphEnabled = true;
                ConnectDeviceBluetooth();
                stopBluetooth.setEnabled(true);
            }
        });
    }

    public void RefreshGraph() {
        if (isGraphEnabled) {
            entries.add(new Entry(Points.get(graphPoints), Points.get(graphPoints + 1)));
            entriesTemp.add(new Entry(PointsTemp.get(graphPoints), PointsTemp.get(graphPoints + 1)));
            entriesHum.add(new Entry(PointsHum.get(graphPoints), PointsHum.get(graphPoints + 1)));
            LineDataSet datasetVel = new LineDataSet(entries, "Velocidad");
            datasetVel.setColor(Color.BLUE);
            datasetVel.setCircleColor(Color.BLUE);
            LineDataSet datasetTempCorp = new LineDataSet(entriesTemp, "Temperatura Corporal");
            datasetTempCorp.setColor(Color.CYAN);
            datasetTempCorp.setCircleColor(Color.CYAN);
            LineDataSet datasetTempPel = new LineDataSet(entriesHum, "Temperatura Peltier");
            datasetTempPel.setColor(Color.MAGENTA);
            datasetTempPel.setCircleColor(Color.MAGENTA);
            List<ILineDataSet> dataset = new ArrayList<ILineDataSet>();
            //dataset.add(datasetVel);
            dataset.add(datasetTempCorp);
            //dataset.add(datasetTempPel);
            LineData lineData = new LineData(dataset);
            chart.setData(lineData);
            setChartOptions();
            graphPoints = graphPoints + 2;
        }
    }

    public Bitmap resizeImage(InputStream is) {
        BitmapFactory.Options options;
        try {
            options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1/3 of origin image size from width and height
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            return bitmap;
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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
        leftyaxis.setTextColor(Color.WHITE);
        rightyaxis.setTextColor(Color.WHITE);
        xaxis.setTextColor(Color.WHITE);
        chart.setDescription(desc);
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
        //It is best to check BT status at onResume in case something has changed while app was paused etc
        checkBTState();
    }

    public void ConnectDeviceBluetooth() {
        // Get a set of currently paired devices and append to pairedDevices list
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // Initialize array adapter for paired devices
        final ArrayList<String> mPairedDevicesArrayAdapter = new ArrayList<String>();

        // Add previously paired devices to the array
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }

            final AlertDialog dialog;
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(GraphActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View convertView = (View) inflater.inflate(R.layout.custom, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Bluetooth Device List: ");
            ListView lv = (ListView) convertView.findViewById(R.id.listView1);
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mPairedDevicesArrayAdapter);
            lv.setAdapter(adapter);
            dialog = alertDialog.show();

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    Toast.makeText(getBaseContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                    // Get the device MAC address, which is the last 17 chars in the View
                    String info = adapter.getItem(position);
                    String address = info.substring(info.length() - 17);
                    BtAddress = address;

                    // Set up a pointer to the remote device using its address.
                    BluetoothDevice device = mBtAdapter.getRemoteDevice(BtAddress);

                    //Attempt to create a bluetooth socket for comms
                    try {
                        btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    } catch (IOException e1) {
                        Toast.makeText(getBaseContext(), "ERROR - Could not create Bluetooth socket", Toast.LENGTH_SHORT).show();
                    }

                    ConnectSocket(btSocket);
                    isBTAdaptSelected = true;

                    dialog.dismiss();
                }
            });
        } else {
            mPairedDevicesArrayAdapter.add("no devices paired");
        }
    }

    public void ConnectSocket(BluetoothSocket BtSocket) {
        // Establish the connection.
        try {
            Toast.makeText(getBaseContext(), "Conectando....", Toast.LENGTH_SHORT).show();
            BtSocket.connect();
        } catch (IOException e) {
            try {
                BtSocket.close();        //If IO exception occurs attempt to close socket
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "ERROR - Could not close Bluetooth socket", Toast.LENGTH_SHORT).show();
            }
        }

        //When activity is resumed, attempt to send a piece of junk data ('x') so that it will fail if not connected
        // i.e don't wait for a user to press button to recognise connection failure
        if (mConnectedThread == null) {
            mConnectedThread = null;
            mConnectedThread = new ConnectedThread(btSocket);
            mConnectedThread.start();
        }

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        //--------mConnectedThread.write("x");

        final AlertDialog addressConf = new AlertDialog.Builder(GraphActivity.this)
                .setTitle("Bluetooth Address")
                .setMessage("Connection Ready!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
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

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        volatile boolean running = true;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket ArrivingSocket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = ArrivingSocket.getInputStream();
                tmpOut = ArrivingSocket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "ERROR: I/O Streams Failed.", Toast.LENGTH_SHORT).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                if (!running){
                    try {
                        mmInStream.close();
                        mmOutStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (Exception e) {
                    //break;
                    e.printStackTrace();
                }
            }
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();
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
                for(int i = 0; i < PointsHum.size(); i = i + 2){
                    postDataparams.put("Tiempo" + i/2, Points.get(i));
                    postDataparams.put("Voltaje" + i/2, Points.get(i + 1) );
                    postDataparams.put("Temperatura" + i/2,  PointsTemp.get(i + 1));
                    postDataparams.put("Humedad" + i/2, PointsHum.get(i + 1));
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
            Toast.makeText(getApplicationContext(), result,
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
