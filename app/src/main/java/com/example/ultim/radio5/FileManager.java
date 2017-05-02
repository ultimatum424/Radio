package com.example.ultim.radio5;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ultim.radio5.Univesity.UniversityItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Ultim on 02.05.2017.
 */

public class FileManager {

    private Context context;

    public FileManager(Context context) {
        this.context = context;
    }

    public void saveFile(String object, String fileName){
        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(object.getBytes());
        }catch(IOException ex) {
            ex.printStackTrace();
        }
        finally{
            try{
                if(outputStream!=null)
                    outputStream.close();
            }
            catch(IOException ex){
            }
        }
    }

    public String openFile(String fileName){
        FileInputStream fileInputStream = null;
        String json = "";
        try {
            fileInputStream = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            json = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{

            try{
                if(fileInputStream !=null)
                    fileInputStream.close();
            }
            catch(IOException ex){
            }
        }
        return json;

    }

    public String getUrlResponse(String url) {
        OkHttpClient client = new OkHttpClient();
        String str = "";
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            str = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

}
