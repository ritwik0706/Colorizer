package com.example.ritwik.colorizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sliderlibrary.BeforeAfterSlider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ColorizeActivity extends AppCompatActivity {

    String filename;
    Uri uri;
    BeforeAfterSlider slider;
    ImageView ivColorize;
    File uploadFile;
    MenuItem buSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorize);

        ivColorize = findViewById(R.id.ivColorize);
        Button buColorize = findViewById(R.id.buColorize);
        slider = findViewById(R.id.mySlider);

        Intent intent = getIntent();
        String imageUri = intent.getStringExtra("imageUri");
        uri = Uri.parse(imageUri);

        String filePath = "/storage/emulated/0/Colorizer/upload.jpg";
        uploadFile = new File(filePath);
        Uri myUri = Uri.fromFile(uploadFile);

        ivColorize.setImageURI(myUri);

        buColorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        buSave = menu.findItem(R.id.Save);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.Save:
                new Thread(new Runnable() {
                    public void run() {

                        Bitmap myBitmap = getBitmapfromURL("http://ec2-52-71-24-249.compute-1.amazonaws.com/colored/col_"+ filename + ".png");
                        storeColoredImage(myBitmap);

                    }
                }).start();
        }
        return super.onOptionsItemSelected(item);
    }

    public void uploadImage(){

        String filePath = "/storage/emulated/0/Colorizer/upload.jpg";
        final File originalfile=new File(filePath);

        RequestBody filepart=RequestBody.create(
                MediaType.parse("image/jpeg"),
                originalfile
        );

        Log.e("file name", uploadFile.getName());
        MultipartBody.Part file=MultipartBody.Part.createFormData("photo",uploadFile.getName(), filepart);

        String baseUrl="http://ec2-52-71-24-249.compute-1.amazonaws.com/";
        Retrofit retrofit= new Retrofit.Builder().baseUrl(baseUrl).
                addConverterFactory(GsonConverterFactory.create()).build();

        ApiInterface apiInterface=retrofit.create(ApiInterface.class);

        Call<ResponseBody> call= apiInterface.uploadImage(file);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    assert response.body() != null;
                    filename= Objects.requireNonNull(response.body()).string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String black_white="http://ec2-52-71-24-249.compute-1.amazonaws.com/original/"+ filename + ".jpg";
                String colored="http://ec2-52-71-24-249.compute-1.amazonaws.com/colored/col_"+ filename + ".png";
                setSlider(black_white, colored);

                uploadFile.delete();

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ColorizeActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void setSlider(String blackWhite, String colored){
        Log.e("b/w", blackWhite);
        Log.e("col", colored);
        slider.setBeforeImage(colored).setAfterImage(blackWhite);
        ivColorize.setVisibility(View.GONE);
        slider.setVisibility(View.VISIBLE);
        buSave.setVisible(true);
    }

    public Bitmap getBitmapfromURL(String src) {

        Bitmap myBitmap;
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void storeColoredImage(Bitmap image) {
        File pictureFile = getOutputDirectory();
        if (pictureFile == null) {
            Log.d("Error",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Error", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Error", "Error accessing file: " + e.getMessage());
        }
    }

    private  File getOutputDirectory(){

        File mediaStorageDir = new File("/storage/emulated/0/Colorizer/Coloured Images/");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                mediaStorageDir.mkdirs();
            }
        }

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "col_" + filename + ".png");
        return mediaFile;
    }
}
