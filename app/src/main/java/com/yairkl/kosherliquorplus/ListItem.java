package com.yairkl.kosherliquorplus;

import android.content.Context;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ListItem {
    Identifiable item;
    private ImageStorageManager imageStorageManager;

    public ListItem(Identifiable item,Context context){
        this.item = item;
        imageStorageManager = new ImageStorageManager(context);
    }

    public String getName(){
        return item.getName();
    }

    public void getImage(int rez,ImageView imageView,ProgressBar progress){
        imageStorageManager.getImage(item,rez,imageView, progress);
    }
}
