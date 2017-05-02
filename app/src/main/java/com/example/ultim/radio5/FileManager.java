package com.example.ultim.radio5;

import android.content.Context;
import android.view.View;
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

/**
 * Created by Ultim on 02.05.2017.
 */

public class FileManager {

    private Context context;

    public FileManager(Context context) {
        this.context = context;
    }

    private void saveFile(String object, String fileName){
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

    private String openFile(String fileName){
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

    public ArrayList<UniversityItem> getListUniversity(){
        ArrayList<UniversityItem> universityItems = new ArrayList<UniversityItem>();
        String jsonString = openFile(AppConstant.FILE_STATIONS);
        if (!Objects.equals(jsonString, "")){
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<UniversityItem>>(){}.getType();
            universityItems = gson.fromJson(jsonString, listType);
        }
        return universityItems;
    }

    public void saveListUniversity(ArrayList<UniversityItem> universityItems){
        Gson gson = new Gson();
        String jsonString  = gson.toJson(universityItems);
        saveFile(jsonString, AppConstant.FILE_STATIONS);
    }
}
