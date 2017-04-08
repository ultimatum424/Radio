package com.example.ultim.radio5.Univesity;


public class UniversityItem {
    String name;
    boolean isSelected;

    public UniversityItem(String name) {
        this.name = name;
        this.isSelected = false;
    }

    public UniversityItem(String name, boolean isSelected) {
        this.name = name;
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getName() {
        return name;
    }
}
