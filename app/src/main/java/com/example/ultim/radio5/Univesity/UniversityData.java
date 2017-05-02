package com.example.ultim.radio5.Univesity;


import android.content.Context;

import com.example.ultim.radio5.AppConstant;
import com.example.ultim.radio5.FileManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import static com.example.ultim.radio5.AppConstant.UNIVERSITY_LIST_MINIMAL_LENGTH_TO_SEARCH;

public class UniversityData {
    private ArrayList<UniversityItem> items;
    private FileManager fileManager;
    private Gson gson;


    public UniversityData(Context context) {
        items = new ArrayList<UniversityItem>();
        fileManager = new FileManager(context);
        gson = new Gson();
        loadData();

    }

    public void loadData() {
        String jsonString = fileManager.openFile(AppConstant.FILE_STATIONS);
        //jsonString = "";
        if (!Objects.equals(jsonString, "")){
            Type listType = new TypeToken<ArrayList<UniversityItem>>(){}.getType();
            items = gson.fromJson(jsonString, listType);
        }
        else {

          //  items.add(new UniversityItem("Волгатех", "http://217.22.172.49:8000/o5radio"));
           // items.add(new UniversityItem("МарГУ", "http://217.22.172.49:8000/margu"));
        }
    }

    public void saveData() {
        String jsonString  = gson.toJson(items);
        fileManager.saveFile(jsonString, AppConstant.FILE_STATIONS);
    }

    public boolean isSearchViewNeeded() {
        return items.size() >= UNIVERSITY_LIST_MINIMAL_LENGTH_TO_SEARCH;
    }


    public ArrayList<UniversityItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<UniversityItem> items) {
        this.items = items;
    }

    public UniversityItem findItemByTitle(String pattern) {
        for(UniversityItem item : items) {
            if (item.getName().equals(pattern)) {
                return item;
            }
        }
        return null;
    }
}
