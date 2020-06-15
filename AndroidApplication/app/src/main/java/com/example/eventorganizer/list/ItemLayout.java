package com.example.eventorganizer.list;

import android.content.Context;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * Base abstract class for all mutable UI elements used as list element in {@link ItemListAdapter}
 */
public abstract class ItemLayout {

    /** Layout resource ID */
    private static int resourceId;
    /** Instance of {@link ItemHolder} */
    private ItemHolder itemHolder;

    /**
     * Protected constructor to initialize layout.
     * @param resId Layout resource ID
     */
    protected ItemLayout(int resId) {
        resourceId = resId;
    }

    /**
     * Returns used resource ID.
     * @return Used resource ID
     */
    public static int getResourceId() {
        return resourceId;
    }

    /**
     * Creates container with UI elements for this layout.
     * @param view View to search for UI elements
     */
    public abstract void createItemHolder(View view);

    /**
     * Returns instance of {@link ItemHolder}.
     * @return Instance of {@link ItemHolder}
     */
    protected ItemHolder getItemHolder() {
        return itemHolder;
    }

    /**
     * Assigns new reference to <b>itemHolder</b>.
     * @param itemHolder Reference to {@link ItemHolder}
     */
    protected void setItemHolder(ItemHolder itemHolder) {
        this.itemHolder = itemHolder;
    }

    /**
     * Assigns new reference to <b>itemHolder</b> from given view.
     * @param view Given view
     */
    protected void setItemHolderFromView(View view) {
        this.itemHolder = (ItemHolder) view.getTag();
    }

    /**
     * Sets initial values for UI elements such as displayed text and eventually creates listeners for buttons.
     * @param context Context of application
     */
    protected abstract void setItemHolderAttributes(@Nullable Context context);

    /**
     * Base container class for UI elements
     */
    public abstract class ItemHolder {

    }
}
