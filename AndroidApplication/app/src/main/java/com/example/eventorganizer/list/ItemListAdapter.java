package com.example.eventorganizer.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Adapter class for displaying custom containers with UI elements.
 * @param <T> Any class inheriting from {@link ItemLayout}
 */
public class ItemListAdapter<T extends ItemLayout> extends BaseAdapter {

    /** List with UI elements container */
    private final ArrayList<T> layoutList;
    /** Application context */
    private final Context context;

    /**
     * Constructor to initialize custom Adapter
     * @param context Application context
     * @param layoutList List with UI elements container
     */
    public ItemListAdapter(Context context, ArrayList<T> layoutList) {
        this.layoutList = layoutList;
        this.context = context;
    }

    /**
     * Returns used list with UI elements container
     * @return List with UI elements container
     */
    public ArrayList<T> getLayoutList() {
        return layoutList;
    }

    /**
     * Returns size of list.
     * @return Size of list
     */
    @Override
    public int getCount() {
        return layoutList.size();
    }

    /**
     * Returns element from list at specified position.
     * @param position Position of element
     * @return Element from given position
     */
    @Override
    public Object getItem(int position) {
        return layoutList.get(position);
    }

    /**
     * Returns element ID at specified position in list.
     * @param position Position of element
     * @return Element ID
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Returns created view for specified layout from list.
     * @param position Position in list
     * @param convertView View with created layout (may be null)
     * @param parent Parent of <b>convertView</b>
     * @return Created view with specified layout from list
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        T layout = layoutList.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(T.getResourceId(), parent, false);
            layout.createItemHolder(convertView);
            convertView.setTag(layout.getItemHolder());
        } else {
            layout.setItemHolderFromView(convertView);
        }

        layout.setItemHolderAttributes(context);

        return convertView;
    }
}
