package com.example.eventorganizer;

import android.content.Context;
import android.view.View;

public abstract class ItemLayout {

    private static int resourceId;
    private ItemHolder itemHolder;

    protected ItemLayout(int resId) {
        resourceId = resId;
    }

    public static int getResourceId() {
        return resourceId;
    }

    public abstract void createItemHolder(View view);

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

    /*private static int resourceId;

    protected ItemLayout(int resId) {
        resourceId = resId;
    }

    public static int getResourceId() { return resourceId; }

    protected abstract Object getItemHolder();

    public abstract void setLayout(View view, Context context);

    public abstract void setItemHolder(Object itemHolder);

    protected abstract static class ItemHolder extends Object {

    }*/
}
