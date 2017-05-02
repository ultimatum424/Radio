package com.example.ultim.radio5.Univesity;


public class UniversityItem {
    String name;
    boolean isSelected;
    String stream;

    public UniversityItem(String name, String stream) {
        this.name = name;
        this.isSelected = false;
        this.stream = stream;
    }

    public UniversityItem(String name, boolean isSelected, String stream) {
        this.name = name;
        this.isSelected = isSelected;
        this.stream = stream;
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

    public String getStream(){
        return stream;
    }
}
