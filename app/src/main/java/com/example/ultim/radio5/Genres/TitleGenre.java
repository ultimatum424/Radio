package com.example.ultim.radio5.Genres;

/**
 * Created by Ultim on 04.06.2017.
 */

public class TitleGenre {
    private static final TitleGenre ourInstance = new TitleGenre();
    private String title;

    public static TitleGenre getInstance() {
        return ourInstance;
    }

    private TitleGenre() {

    }

    public static TitleGenre getOurInstance() {
        return ourInstance;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
