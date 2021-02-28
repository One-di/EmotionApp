package com.whynot.EmotionApp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.apache.http.entity.mime.MultipartEntity;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.*;


public class MainActivity extends AppCompatActivity {

    private URL url;
    static final int GALLERY_REQUEST = 1;
    static final Bitmap DEAFAULT_IMAGE = null;
    MultipartEntity reqEntity;
    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonSelectImage = findViewById(R.id.button1);
        Button buttonSendImage = findViewById(R.id.button2);
        final TextView textView = findViewById(R.id.textView);

        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
            }
        });

        buttonSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmap == null){
                    System.out.println("нет картинки");
                }else{
                    if(isOnline()){
                        try {
                            url = new URL("https://5b152b376255.ngrok.io/save"); //ССЫЛКА НА СЕРВЕР
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        String c = requestServer();
                        textView.setText(c);
                    }
                }
            }
        });

    }

    //метод обращения к серверу
    String requestServer() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Future<String> future = executorService.submit(task);
        try {
            String response = future.get();
            return response;
        }catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return "null";
    }

    //поток обращения к серверу
    final Callable<String> task = new Callable<String>() {
        @Override
        public String call() {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "multipart/form-data");


                BufferedOutputStream stream = new BufferedOutputStream(urlConnection.getOutputStream());
                //stream.write((jsonObject.toString().getBytes()));
                stream.flush();
                stream.close();

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String string_response = reader.readLine();
                    return string_response;

                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "resp_null";
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        ImageView imageView = findViewById(R.id.imageView1);

        if (requestCode == GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = imageReturnedIntent.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
