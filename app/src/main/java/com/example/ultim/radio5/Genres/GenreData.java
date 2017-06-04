package com.example.ultim.radio5.Genres;

import android.content.Context;
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
        /*String jsonString = fileManager.openFile(AppConstant.FILE_STATIONS);
        if (!Objects.equals(jsonString, "")){
            Type listType = new TypeToken<ArrayList<GenreItem>>(){}.getType();
            items = gson.fromJson(jsonString, listType);
        } else*/ {
            items.add(new GenreItem("Rock", new String[]{"rock1", "rock2"}, false));
            items.add(new GenreItem("Pop", new String[]{"pop1", "pop2"}, false));
        }
    }

    public void saveData() {
        String jsonString  = gson.toJson(items);
        fileManager.saveFile(jsonString, AppConstant.FILE_STATIONS);
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
}
