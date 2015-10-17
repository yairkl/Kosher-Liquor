package com.yairkl.kosherliquorplus;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ItemCursorAdapter extends CursorAdapter {
    private List<ListItem> items;
    TextView text;
    ImageView image;

    public static Cursor getCursor(Context context, List<? extends Identifiable> items){
        List<ListItem> listItems = ProductList.getAllNames(context, new ArrayList<Identifiable>(), items);
        String[] columns = new String[]{"_id", "listItem"};
        Object[] temp = new Object[]{0, new ListItem(Identifiable.EMPTY, context)};

        MatrixCursor cursor = new MatrixCursor(columns);

        for (int i = 0; i < listItems.size(); i++) {
            temp[0] = i;
            temp[1] = listItems.get(i);

            cursor.addRow(temp);
        }
        return cursor;
    }

    public ItemCursorAdapter(Context context, Cursor cursor,List<ListItem> items) {
        super(context, cursor, false);
        this.items = items;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row, viewGroup, false);

        text = (TextView) view.findViewById(R.id.textView);
        image = (ImageView)view.findViewById(R.id.imageView);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        text.setText(items.get(cursor.getPosition()).getName());
//        image.setImageBitmap(items.get(cursor.getPosition()).getImage(2,image));
        items.get(cursor.getPosition()).getImage(2,image,new ProgressBar(context));
    }
}
