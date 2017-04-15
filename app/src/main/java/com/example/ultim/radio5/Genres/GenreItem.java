package com.example.ultim.radio5.Genres;


public class GenreItem {
    String name;
    boolean isSelected;

    public GenreItem(String name) {
        this.name = name;
        this.isSelected = false;
    }

    public GenreItem(String name, boolean isSelected) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public void changeSelectedStatus() {
        isSelected = !isSelected;
    }

    public String getName() {
        return name;
    }
}
