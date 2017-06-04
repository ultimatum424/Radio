package com.example.ultim.radio5.Genres;


import android.net.Uri;

public class GenreItem {
    String name;
    boolean downloadStatus;
    int length;
    String url;
    String filePatch;
    String list[];

    public GenreItem(String name, String url, String filePatch, String list[], boolean downloadStatus) {
        this.name = name;
        this.downloadStatus = downloadStatus;
        this.list = list;
        this.filePatch = filePatch;
        this.length = list.length;
        this.url = url;
    }

    public GenreItem(String name, boolean downloadStatus) {
        this.name = name;
    }

    public boolean isDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(boolean downloadStatus) {
        this.downloadStatus = downloadStatus;
    }
    public void changeSelectedStatus() {
        downloadStatus = !downloadStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String[] getList() {
        return list;
    }

    public void setList(String[] list) {
        this.list = list;
    }

    public void setFilePatch(Uri filePatch) {

        this.filePatch = filePatch.toString();
    }

    public Uri getFilePatch(){
        return Uri.parse(this.filePatch);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
