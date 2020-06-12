package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import androidx.annotation.Nullable;

public abstract class ItemLayout {

    private static int resourceId;
    private ItemHolder itemHolder;

    protected ItemLayout(int resId) {
        resourceId = resId;
    }

    public static int getResourceId() {
        return resourceId;
    }

    public abstract void createItemHolder(View view, @Nullable Context context);

    protected ItemHolder getItemHolder() {
        return itemHolder;
    }

    protected void setItemHolder(ItemHolder itemHolder) {
        this.itemHolder = itemHolder;
    }

    protected void setItemHolderFromView(View view) {
        this.itemHolder = (ItemHolder) view.getTag();
    }

    protected abstract void setItemHolderAttributes();

    public abstract class ItemHolder {

    }
}
