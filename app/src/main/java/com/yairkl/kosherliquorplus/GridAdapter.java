package com.yairkl.kosherliquorplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class GridAdapter extends ArrayAdapter<ListItem> {
    Context context;
    List<ListItem> listItems;
    TextView nameTextView;
    ImageView imageView;
    ProgressBar progress;

    public GridAdapter(Context context, List<ListItem> items) {
        super(context, R.layout.costum_image_button,items);
        this.context = context;
        listItems = items;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.costum_image_button, parent, false);

        nameTextView = (TextView) rowView.findViewById(R.id.buttonText);
        imageView = (ImageView) rowView.findViewById(R.id.buttonImage);
        progress = (ProgressBar) rowView.findViewById(R.id.progressBar);

        nameTextView.setText(listItems.get(position).getName());
//        imageView.setImageBitmap(listItems.get(position).getImage(1,imageView));
        listItems.get(position).getImage(1,imageView,progress);

        return rowView;
    }

//    private class LoadImage extends AsyncTask<Integer,Bitmap,Void>{
//
//        @Override
//        protected Void doInBackground(Integer... integers) {
//            Bitmap bitmap = listItems.get(integers[0]).getImage(1);
//            publishProgress(bitmap);
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(Bitmap... bitmaps){
//            imageView.setImageBitmap(bitmaps[0]);
//        }
//    }

}
