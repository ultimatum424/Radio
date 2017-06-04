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
        String jsonString = fileManager.openFile(AppConstant.FILE_GENRE);
        if (!Objects.equals(jsonString, "")){
            Type listType = new TypeToken<ArrayList<GenreItem>>(){}.getType();
            items = gson.fromJson(jsonString, listType);
        } else {
            items.add(new GenreItem("Rock",
                    "https://downloader.disk.yandex.ru/disk/c135b0c63bc48e63503aa4870807bab89eeef68183da08b7ceb61ab129dfc648/593482b5/AhqCbS4YLYWsy1JxRIDicPWE2ERwApl6bR8JOj2ZEhOg4lVjjOlpi4WPpT6bGZaoNY5DiKGETHBXfxcHrfJ9EQ%3D%3D?uid=0&filename=moby_-_extreme_ways_%28zvukoff.ru%29.mp3&disposition=attachment&hash=VmUiODl0hcGP7j5KDDzO7cEsmd8Ecec8d/vhf1l7bec%3D%3A&limit=0&content_type=audio%2Fmpeg&fsize=3808488&hid=c443be5b1e9331a1a5d99a2e7982bc7a&media_type=audio&tknv=v2",
                    null, new String[]{"rock1", "rock2"}, false));
            items.add(new GenreItem("Pop",
                    "https://downloader.disk.yandex.ru/disk/c135b0c63bc48e63503aa4870807bab89eeef68183da08b7ceb61ab129dfc648/593482b5/AhqCbS4YLYWsy1JxRIDicPWE2ERwApl6bR8JOj2ZEhOg4lVjjOlpi4WPpT6bGZaoNY5DiKGETHBXfxcHrfJ9EQ%3D%3D?uid=0&filename=moby_-_extreme_ways_%28zvukoff.ru%29.mp3&disposition=attachment&hash=VmUiODl0hcGP7j5KDDzO7cEsmd8Ecec8d/vhf1l7bec%3D%3A&limit=0&content_type=audio%2Fmpeg&fsize=3808488&hid=c443be5b1e9331a1a5d99a2e7982bc7a&media_type=audio&tknv=v2",
                    null, new String[]{"pop1", "pop2"}, false));
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
}
