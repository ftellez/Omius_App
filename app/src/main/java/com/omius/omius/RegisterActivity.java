package com.omius.omius;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Manifest;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {
    //Register
    private static final String TAG = "RegisterActivity";
    @Bind(R.id.input_firstname) EditText _firstnameText;
    @Bind(R.id.input_lastname) EditText _lastnameText;
    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.btn_register) Button _registerButton;
    String[] valuesPOST = new String[3];

    //Camera  private static final String TAG = "CallCamera";
    public static final int REQUEST_MULTIPLE_PERMISSIONS = 1;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;
    Uri fileUri = null;
    ImageView photoImage = null;
    ImageUploadHandler imgupload;
    Bitmap bmp;
    Uri photoUri = null;
    Boolean photo = false;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        ButterKnife.bind(this);
        photoImage = (ImageView) findViewById(R.id.photo_image);
        ImageButton callCameraButton = (ImageButton) findViewById(R.id.button_callcamera);

        if (checkPermissionsAndRequest()) { }

        callCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = FileProvider.getUriForFile(RegisterActivity.this, RegisterActivity.this.getApplicationContext().getPackageName() + ".provider", getOutputPhotoFile());;
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ);
            }
        });

            _registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { register(); }
            });

            client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Permission Request
    private boolean checkPermissionsAndRequest() {
        int permissionCamera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int permissionStorage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissions = new ArrayList<>();
        if (permissionStorage != PackageManager.PERMISSION_GRANTED) {listPermissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);}
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {listPermissions.add(android.Manifest.permission.CAMERA);}
        if (!listPermissions.isEmpty()){
            ActivityCompat.requestPermissions(this, listPermissions.toArray(new String[listPermissions.size()]),REQUEST_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(android.Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "camera and storage permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                      // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK("SMS and Location Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE: checkPermissionsAndRequest();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Camera methods
    private void showPhoto(Uri photoUri) {
        File imageFile = new File(photoUri.getPath().toString()); //photoUri.getPath().toString()
        InputStream iStream = null;
        if (imageFile.isFile()) {
            try { iStream = getContentResolver().openInputStream(photoUri); }
            catch (Exception ex) { Toast.makeText(getBaseContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();}
            bmp = RotateBitmap(resizeImage(iStream), 270);
            //Toast.makeText(getBaseContext(), "Imagen subida exitosamente", Toast.LENGTH_LONG).show();
            photoImage.setImageBitmap(bmp);
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

    public Bitmap bmpResize(Bitmap bitmap,int newWidth,int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;

    }

    public Bitmap resizeDirectBitmap(Bitmap bmp){
        Bitmap resBitmap = Bitmap.createScaledBitmap(bmp,(int)(bmp.getWidth()*0.25), (int)(bmp.getHeight()*0.25), true);
        return resBitmap;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());

        return output;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private File getOutputPhotoFile() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getPackageName());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "Failed to create storage directory.");
                return null;
            }
        }
        CharSequence charSeq = new String("yyyMMddHHmmss");
        String timeStamp = new DateFormat().format(charSeq, new Date()).toString();
        return new File(directory.getPath() + File.separator + "IMG_"
                + timeStamp + ".jpg");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
            if (resultCode == RESULT_OK) {
//                photoUri = fileUri;
//                showPhoto(photoUri);
                if (data == null) {
                    // A known bug here! The image should have saved in fileUri
                    //Toast.makeText(this, "Image loaded successfully", Toast.LENGTH_SHORT).show();
                    photoUri = fileUri;
                    photo = true;
                    showPhoto(photoUri);
                } else {
                    //photoUri = (Uri) data.getExtras().get("data");
                    Bitmap bitParsed = (Bitmap) data.getExtras().get("data");
                    bmp = RotateBitmap(bmpResize(bitParsed,(int)(Math.ceil(bitParsed.getWidth()*0.25)),(int)(Math.ceil(bitParsed.getHeight()*0.25))), 0);
                    //Toast.makeText(getBaseContext(), "Imagen subida exitosamente", Toast.LENGTH_LONG).show();
                    photoImage.setImageBitmap(bmp);
                    photo = true;
                    //Toast.makeText(this, "Image loaded successfully in: " + data.getData(), Toast.LENGTH_SHORT).show();
                    //showPhoto(photoUri);
                }
                // showPhoto(photoUri);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Callout for image capture failed!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////// Register methods
    public void register() {

        Log.d(TAG, "Register");
        if (!validate()) {
            onRegisterFailed();
            return;
        }

        _registerButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Registry...");
        progressDialog.show();

        valuesPOST[0] = _firstnameText.getText().toString();
        valuesPOST[1] = _lastnameText.getText().toString();
        valuesPOST[2] = _emailText.getText().toString();

        new SendPOSTrequest().execute();

        imgupload = new ImageUploadHandler();
        imgupload.setOnVariables(fileUri, bmp, null);
        imgupload.uploadImage(RegisterActivity.this);

        new Handler().postDelayed(new Runnable() {
            // TODO: Implement your own register logic here.
            public void run() {
                // On complete call either onRegisterSuccess or onRegisterFailed depending on success
                onRegisterSuccess();
                //onRegisterFailed();
                progressDialog.dismiss();
            }
        }, 3000);
    }

    public void onRegisterSuccess() {
        _registerButton.setEnabled(true);
        setResult(RESULT_OK, null);
        //finish();
        Intent intent = new Intent(this, GraphActivity.class);
        startActivity(intent);
    }

    public void onRegisterFailed() {
        Toast.makeText(getBaseContext(), "Unable to register", Toast.LENGTH_LONG).show();
        _registerButton.setEnabled(true);
    }

    // Method validates input text on every field.
    public boolean validate() {
        boolean valid = true;
        String fn = _firstnameText.getText().toString();
        String ln = _lastnameText.getText().toString();
        String em = _emailText.getText().toString();

        // check that the field is not empty and is at least three chars.
        if (fn.isEmpty() || fn.length() < 3) {
            _firstnameText.setError("At least 3 characters");
            valid = false;
        } else {
            _firstnameText.setError(null);
        }

        // check that field is not empty and its length is 10
        if (ln.isEmpty() || ln.length() < 3) {
            _lastnameText.setError("At least 3 characters");
            valid = false;
        } else {
            _lastnameText.setError(null);
        }

        // check that field is not empty and its pattern of an email matches.
        if (em.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
            _emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (!photo) {
            Toast.makeText(getBaseContext(), "Must add an image", Toast.LENGTH_LONG).show();
            valid = false;
        }

        return valid;
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Register Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // HTTP methods
    private class SendPOSTrequest extends AsyncTask<String, Void, String> {
        protected void onPreExecute() { }
        protected String doInBackground(String... params) {
            try {
                // Create URL and JSON Object
                //We will use URLconnection for HTTP to send and receive data
                URL url = new URL("http://planz.omiustech.com/bombaacida.php");
                JSONObject postDataparams = new JSONObject();
                postDataparams.put("Firstname", valuesPOST[0]);
                postDataparams.put("Lastname", valuesPOST[1]);
                postDataparams.put("email", valuesPOST[2]);

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
                        break;
                    }

                    in.close();
                    return sb.toString();
                } else {
                    return new String("false: " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        //Get response
        @Override
        protected void onPostExecute(String result) {
//            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "Registro logrado.", Toast.LENGTH_LONG).show();
        }
    }

    //Method to convert JSON Obect to encode url string format
    public String getPostDataString(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {
            String key = itr.next();
            Object value = params.get(key);

            if (first) { first = false;
            } else { result.append("&"); }

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }
}
