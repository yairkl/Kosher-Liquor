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

public class ItemAdapter extends ArrayAdapter<ListItem> {
    Context context;
    private List<ListItem> itemsList;

    public ItemAdapter(Context context, List<ListItem> itemsArrayList) {
        super(context, R.layout.row, itemsArrayList);
        this.context = context;
        this.itemsList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.row, parent, false);

        TextView nameTextView = (TextView) rowView.findViewById(R.id.textView);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);

        nameTextView.setText(itemsList.get(position).getName());
//        imageView.setImageBitmap(itemsList.get(position).getImage(2,imageView));
        itemsList.get(position).getImage(2,imageView,progressBar);

        return rowView;
    }
}
