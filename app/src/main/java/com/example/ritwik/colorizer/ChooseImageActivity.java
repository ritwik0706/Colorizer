package com.example.ritwik.colorizer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ChooseImageActivity extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE = 225;
    private static final int TXT_STORAGE = 2;
    private PermissionUtil permissionUtil;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);

        permissionUtil = new PermissionUtil(this);

        Button buChoose = findViewById(R.id.buChoose);
        tv = findViewById(R.id.textView);

        buChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (CheckPermission(TXT_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    if (ActivityCompat.shouldShowRequestPermissionRationale(ChooseImageActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        showPermissionExplanation(TXT_STORAGE);
                    }
                    else if (!permissionUtil.checkPermissionPreference("storage")){
                        requestPermission(TXT_STORAGE);
                        permissionUtil.updatePermissionPreference("storage");
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Please Allow Storage Permission in your App Setting.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(),null);
                        intent.setData(uri);
                        getApplicationContext().startActivity(intent);
                    }
                }
                else{

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                storeUploadImage(bitmap);

                Intent intent = new Intent(this, ColorizeActivity.class);
                intent.putExtra("imageUri",uri.toString());
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int CheckPermission(int permission){

        int status = PackageManager.PERMISSION_DENIED;

        switch (permission){
            case TXT_STORAGE:
                status = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                break;
        }

        return status;
    }

    private void requestPermission(int permission){

        switch (permission){
            case TXT_STORAGE:
                ActivityCompat.requestPermissions(ChooseImageActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                break;
        }
    }

    private void showPermissionExplanation(final int permission){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(permission == TXT_STORAGE){
            builder.setMessage("This App needs Storage Permission..Please Allow");
            builder.setTitle("Storage Permission Needed");
        }

        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (permission == TXT_STORAGE)
                    requestPermission(TXT_STORAGE);
            }
        });

        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void storeUploadImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        Bitmap newImage = getResizedBitmap(image);
        if (pictureFile == null) {
            Log.d("Error",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            newImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Error", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Error", "Error accessing file: " + e.getMessage());
        }
    }

    private  File getOutputMediaFile(){

        File mediaStorageDir = new File("/storage/emulated/0/Colorizer/");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                mediaStorageDir.mkdirs();
            }
        }

        File mediaFile;
        String mImageName="upload.jpg" ;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public Bitmap getResizedBitmap(Bitmap bitmap){

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        float aspectRatio = originalWidth / (float) originalHeight;
        int newWidth = tv.getWidth();
        int newHeight = Math.round(newWidth / aspectRatio);

        if(newHeight>0) {
            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            return newBitmap;
        }
        else{
            return bitmap;
        }


    }
}
