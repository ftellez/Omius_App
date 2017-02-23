package com.omius.omius;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    //Register
    private static final String TAG = "RegisterActivity";
    @Bind(R.id.input_firstname)   EditText _firstnameText;
    @Bind(R.id.input_lastname) EditText _lastnameText;
    @Bind(R.id.input_email)  EditText _emailText;
    @Bind(R.id.btn_register) Button _registerButton;
    String[] valuesPOST = new String[3];

    //Camera  private static final String TAG = "CallCamera";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;
    Uri fileUri = null;
    ImageView photoImage = null;
    ImageUploadHandler imgupload;
    Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        ButterKnife.bind(this);
        photoImage = (ImageView) findViewById(R.id.photo_image);
//        photoImage.setImageDrawable(null);

        ImageButton callCameraButton = (ImageButton) findViewById(R.id.button_callcamera);
        callCameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //File file = getOutputPhotoFile();
                fileUri = Uri.fromFile(getOutputPhotoFile());
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ );
            }
        });

        _registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { register(); }
        });
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////// Camera methods
    private void showPhoto(Uri photoUri) {
        File imageFile = new File(photoUri.getPath().toString()); //photoUri.getPath().toString()
        InputStream iStream = null;
        if (imageFile.isFile()) {
            try {
                iStream = getContentResolver().openInputStream(photoUri);
            } catch (Exception ex){
                Toast.makeText(getBaseContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
            }
            bmp = RotateBitmap(resizeImage(iStream),270);
            Toast.makeText(getBaseContext(),"Imagen subida exitosamente",Toast.LENGTH_LONG).show();
            photoImage.setImageBitmap(bmp);
        }
    }

    public Bitmap resizeImage (InputStream is) {
        BitmapFactory.Options options;
        try {
            options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1/3 of origin image size from width and height
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            return bitmap;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
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
        String timeStamp = new DateFormat().format(charSeq,new Date()).toString();
        return new File(directory.getPath() + File.separator + "IMG_"
                + timeStamp + ".jpg");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = null;
                if (data == null) {
                    // A known bug here! The image should have saved in fileUri
                    Toast.makeText(this, "Image loaded successfully",
                            Toast.LENGTH_LONG).show();
                    photoUri = fileUri;
                    showPhoto(photoUri);
                } else {
                    photoUri = data.getData();
                    Toast.makeText(this, "Image loaded successfully in: " + data.getData(),
                            Toast.LENGTH_LONG).show();
                    showPhoto(photoUri);
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

////////////////////////////////////////////////////////////////////////////////////////////////////
///////// Register methods
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
        imgupload.setOnVariables(fileUri,bmp);
        imgupload.uploadImage(RegisterActivity.this);

        new android.os.Handler().postDelayed(new Runnable() {
        // TODO: Implement your own register logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onRegisterSuccess or onRegisterFailed
                        // depending on success
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
    }

    public void onRegisterFailed() {
        Toast.makeText(getBaseContext(), "Unable to register", Toast.LENGTH_LONG).show();
        _registerButton.setEnabled(true);
    }

    // Method validates input text on every field.
    public boolean validate() {
        boolean valid  = true;
        String fn  = _firstnameText.getText().toString();
        String ln  = _lastnameText.getText().toString();
        String em  = _emailText.getText().toString();

        // check that the field is not empty and is at least three chars.
        if (fn.isEmpty() || fn.length() < 3) {
            _firstnameText.setError("At least 3 characters");
            valid = false;
        } else { _firstnameText.setError(null); }

        // check that field is not empty and its length is 10
        if (ln.isEmpty() || ln.length() < 3) {
            _lastnameText.setError("At least 3 characters");
            valid = false;
        } else { _lastnameText.setError(null); }

        // check that field is not empty and its pattern of an email matches.
        if (em.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
            _emailText.setError("Enter a valid email address");
            valid = false;
        } else { _emailText.setError(null); }

        return valid;
    }

    ///////////////////////////////////////////////////////////
    private class SendPOSTrequest extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {}
        protected String doInBackground(String...params) {
            try {
                // Create URL and JSON Object
                //We will use URLconnection for HTTP to send and receive data
                URL url = new URL("http://planz.omiustech.com/bombaacida.php");
                JSONObject postDataparams = new JSONObject();
                postDataparams.put("Firstname", valuesPOST[0]);
                postDataparams.put("Lastname",  valuesPOST[1]);
                postDataparams.put("email",  valuesPOST[2]);

                HttpURLConnection httpclient = (HttpURLConnection) url.openConnection();
                httpclient.setReadTimeout(15000);
                httpclient.setConnectTimeout(15000);
                httpclient.setRequestMethod("POST");
                httpclient.setDoOutput(true);
                httpclient.setDoInput(true);

                // Get response
                OutputStream os = httpclient.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataparams));
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
