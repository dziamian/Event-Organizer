package com.example.eventorganizer;

import android.content.Context;
import android.view.View;

public abstract class ItemLayout {

    private final int resourceId;

    protected ItemLayout(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getResourceId() { return resourceId; }

    protected abstract Object getItemHolder();

    public abstract void setLayout(View view, Context context);

    public abstract void setItemHolder(Object itemHolder);

    protected abstract static class ItemHolder extends Object {

    }
}
