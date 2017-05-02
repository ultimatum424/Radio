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
    ArrayList<UniversityItem> items;
    Context context;
    FileManager fileManager;
    Gson gson;

    public UniversityData(Context context) {
        items = new ArrayList<UniversityItem>();
        items.add(new UniversityItem("Volgatech", ""));
        items.add(new UniversityItem("MarSU", ""));
        items.add(new UniversityItem("MOSI", ""));

        this.context = context;
        fileManager = new FileManager(context);

        gson = new Gson();
        loadData();

    }

    public void loadData() {
        String jsonString = fileManager.openFile(AppConstant.FILE_STATIONS);
        if (!Objects.equals(jsonString, "")){
            Type listType = new TypeToken<ArrayList<UniversityItem>>(){}.getType();
            items = gson.fromJson(jsonString, listType);
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
}
