package com.example.ultim.radio5.Univesity;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ultim.radio5.NavigationDrawerActivity;
import com.example.ultim.radio5.R;

import java.util.ArrayList;


public class UnivesityListAdapter extends BaseAdapter {
    ArrayList<UniversityItem> universityItems;
    Context context;
    LayoutInflater layoutInflater;

    public UnivesityListAdapter(ArrayList<UniversityItem> items, Context context) {
        super();
        this.universityItems = items;
        this.layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return universityItems.size();
    }

    @Override
    public Object getItem(int i) {
        return universityItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        UniversityItem currentItem = universityItems.get(i);

        if(view == null) {
            view = layoutInflater.inflate(R.layout.university_listview_item, viewGroup, false);
        }

        TextView textView = (TextView) view.findViewById(R.id.university_list_item_text);
        ImageView imageView = (ImageView) view.findViewById(R.id.university_list_item_image);
        textView.setText(currentItem.getName());
        changeImageViewSrc(imageView, currentItem.isSelected);
        return view;
    }

    private void changeImageViewSrc(ImageView imageView, boolean newState) {
        imageView.setTag(newState ? R.mipmap.ic_star_filled : R.mipmap.ic_star_border);
    }
}
