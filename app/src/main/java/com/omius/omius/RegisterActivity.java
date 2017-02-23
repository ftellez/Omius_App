package com.omius.omius;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
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
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    //Register
    private static final String TAG = "RegisterActivity";
    @Bind(R.id.input_name)
    EditText _nameText;
    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_mobile) EditText _mobileText;
    @Bind(R.id.btn_register)
    Button _registerButton;

    //Camera
    // private static final String TAG = "CallCamera";
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
            public void onClick(View v) {
                register();
            }
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

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
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

        String name   = _nameText.getText().toString();
        String email  = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();


        // TODO: Implement your own register logic here.

        imgupload = new ImageUploadHandler();
        imgupload.setOnVariables(fileUri,bmp);
        imgupload.uploadImage(RegisterActivity.this);

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
        String name    = _nameText.getText().toString();
        String email   = _emailText.getText().toString();
        String mobile  = _mobileText.getText().toString();

        // check that the field is not empty and is at least three chars.
        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("At least 3 characters");
            valid = false;
        } else { _nameText.setError(null); }

        // check that field is not empty and its pattern of an email matches.
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter a valid email address");
            valid = false;
        } else { _emailText.setError(null); }

        // check that field is not empty and its length is 10
        if (mobile.isEmpty() || mobile.length()!=10) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else { _mobileText.setError(null); }

        return valid;
    }
}
