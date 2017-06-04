package com.example.ultim.radio5.Genres;

import android.content.Context;
import android.content.res.AssetManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.ultim.radio5.AppConstant;
import com.example.ultim.radio5.FileManager;
import com.example.ultim.radio5.R;
import com.example.ultim.radio5.Univesity.UniversityItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import static com.example.ultim.radio5.AppConstant.UNIVERSITY_LIST_MINIMAL_LENGTH_TO_SEARCH;


public class GenreData {
    ArrayList<GenreItem> items;
    Context context;
    FileManager fileManager;
    Gson gson;

    public GenreData(Context context) {
        items = new ArrayList<GenreItem>();
        this.context = context;
        fileManager = new FileManager(context);
        gson = new Gson();
        loadData();
    }

    public void loadData() {
        //TODO: вставить нужное название файла для жанров и раскомментить
        String jsonString = fileManager.openFile(AppConstant.FILE_GENRE);
        if (!Objects.equals(jsonString, "")){
            Type listType = new TypeToken<ArrayList<GenreItem>>(){}.getType();
            items = gson.fromJson(jsonString, listType);
        } else
            {
                Type listType = new TypeToken<ArrayList<GenreItem>>(){}.getType();
                items = gson.fromJson(loadFromAsset(), listType);

        }
    }

    public void saveData() {
        String jsonString  = gson.toJson(items);
        fileManager.saveFile(jsonString, AppConstant.FILE_GENRE);
    }

    public boolean isSearchViewNeeded() {
        return items.size() >= UNIVERSITY_LIST_MINIMAL_LENGTH_TO_SEARCH;
    }

    public ArrayList<GenreItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<GenreItem> items) {
        this.items = items;
    }

    public GenreItem findItemByTitle(String pattern) {
        for(GenreItem item : items) {
            if (item.getName().equals(pattern)) {
                return item;
            }
        }
        return null;
    }

    private String loadFromAsset(){
        String str = "";
        try
        {
            AssetManager assetManager = context.getAssets();
            InputStream in = assetManager.open("genre_list.json");
            InputStreamReader isr = new InputStreamReader(in);
            char [] inputBuffer = new char[100];

            int charRead;
            while((charRead = isr.read(inputBuffer))>0)
            {
                String readString = String.copyValueOf(inputBuffer,0,charRead);
                str += readString;
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        return str;
    }
}
