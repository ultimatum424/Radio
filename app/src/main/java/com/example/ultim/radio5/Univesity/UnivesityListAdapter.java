package com.example.ultim.radio5.Univesity;

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


public class UnivesityListAdapter extends BaseAdapter {
    ArrayList<UniversityItem> universityItems;
    Context context;
    LayoutInflater layoutInflater;
    String searchPattern;

    UniversityItem current;

    public UnivesityListAdapter(ArrayList<UniversityItem> items, Context context) {
        super();
        searchPattern = new String();
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final UniversityItem currentItem = universityItems.get(i);

        if(view == null) {
            view = layoutInflater.inflate(R.layout.university_listview_item, viewGroup, false);
        }

        TextView textView = (TextView) view.findViewById(R.id.university_list_item_text);
        ImageView imageView = (ImageView) view.findViewById(R.id.university_list_item_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onIconClick(i);
            }
        });

        textView.setText(currentItem.getName());
        changeImageViewSrc(imageView, currentItem.isSelected);

        if(!searchPattern.isEmpty()) {
            if(currentItem.getName().toLowerCase().startsWith(searchPattern.toLowerCase())) {
                view.setVisibility(View.VISIBLE);
            }
            else {
                view.setVisibility(View.GONE);
            }
        }
        else {
            view.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private void changeImageViewSrc(ImageView imageView, boolean newState) {
        imageView.setImageResource(newState ? R.mipmap.ic_star_filled : R.mipmap.ic_star_border);
    }

    public void onIconClick(int position) {
        UniversityItem item = universityItems.get(position);
        item.changeSelectedStatus();
        sortUniversities();
        notifyDataSetChanged();
    }

    private void sortUniversities() {
        Collections.sort(universityItems, new Comparator<UniversityItem>() {
            @Override
            public int compare(UniversityItem o1, UniversityItem o2) {
                if (o1.isSelected() == o2.isSelected()) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                } else {
                    return o1.isSelected() ? -1 : 1;
                }
            }
        });
    }

    public void onSearchChange(String newText) {
        this.searchPattern = newText;
        notifyDataSetChanged();
    }

    public boolean onSelect(int position) {
        UniversityItem item = universityItems.get(position);
        if(current != null && current.equals(item)) {
            return false;
        }

        current = item;
        return true;
    }

    public UniversityItem getCurrent() {
        return current;
    }
}
