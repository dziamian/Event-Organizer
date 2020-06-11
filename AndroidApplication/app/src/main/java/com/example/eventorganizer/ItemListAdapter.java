package com.example.eventorganizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ItemListAdapter<T extends ItemLayout> extends BaseAdapter {

    public ArrayList<T> layoutList;
    public Context context;

    public ItemListAdapter(Context context, ArrayList<T> layoutList) {
        this.layoutList = layoutList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return layoutList.size();
    }

    @Override
    public Object getItem(int position) {
        return layoutList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        T layout = layoutList.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(ItemLayout.getResourceId(), null);
            layout.setLayout(convertView, context);

            convertView.setTag(layout.getItemHolder());
        } else {
            layout.setItemHolder(convertView.getTag());
        }

        return convertView;
    }
}
