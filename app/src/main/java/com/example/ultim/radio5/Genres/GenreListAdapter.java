package com.example.ultim.radio5.Genres;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ultim.radio5.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class GenreListAdapter extends BaseAdapter {
    ArrayList<GenreItem> genreItems;
    Context context;
    LayoutInflater layoutInflater;

    public GenreListAdapter(ArrayList<GenreItem> items, Context context) {
        super();
        this.genreItems = items;
        this.layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return genreItems.size();
    }

    @Override
    public Object getItem(int i) {
        return genreItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        GenreItem currentItem = genreItems.get(i);

        if(view == null) {
            view = layoutInflater.inflate(R.layout.genre_listview_item, viewGroup, false);
        }

        TextView textView = (TextView) view.findViewById(R.id.genre_list_item_text);
        ImageView imageView = (ImageView) view.findViewById(R.id.genre_list_item_image);
        textView.setText(currentItem.getName());
        changeImageViewSrc(imageView, currentItem.downloadStatus);

        return view;
    }

    private void changeImageViewSrc(ImageView imageView, boolean newState) {
        imageView.setImageResource(newState ? R.drawable.ic_cloud_done_24dp : R.drawable.ic_cloud_queue_24dp);
        //imageView.setTag(newState ? R.mipmap.ic_star_filled : R.mipmap.ic_star_border);
    }

    public void onItemSelect(int position) {
        GenreItem item = genreItems.get(position);
        //item.changeSelectedStatus();
        //sortGenres();
    }

    private void sortGenres() {
        Collections.sort(genreItems, new Comparator<GenreItem>() {
            @Override
            public int compare(GenreItem o1, GenreItem o2) {
                if (o1.isDownloadStatus() == o2.isDownloadStatus()) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                } else {
                    return o1.isDownloadStatus() ? -1 : 1;
                }
            }
        });
    }
}
