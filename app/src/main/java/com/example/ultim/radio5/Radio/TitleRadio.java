package com.example.ultim.radio5.Radio;

/**
 * Created by Ultim on 21.05.2017.
 */

public class TitleRadio {
    private static final TitleRadio ourInstance = new TitleRadio();
    private String title;

    public static TitleRadio getInstance() {
        return ourInstance;
    }

    private TitleRadio() {

    }

    public static TitleRadio getOurInstance() {
        return ourInstance;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
