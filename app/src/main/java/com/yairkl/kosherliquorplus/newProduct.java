package com.yairkl.kosherliquorplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.List;

import static com.yairkl.kosherliquorplus.DataBaseHandler.database;


public class newProduct extends Activity {
    AutoCompleteTextView nameText;
    ImageView image;
    EditText barcodeText;
    String name;
    String barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);
        final ImageStorageManager manager = new ImageStorageManager(this);
        final List<Product> products = database.selectAllProduct(MainActivity.language);
        nameText = (AutoCompleteTextView)findViewById(R.id.textName);
        barcodeText = (EditText)findViewById(R.id.textBarcode);
        image = (ImageView)findViewById(R.id.imageView1);
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        barcode = intent.getStringExtra("barcode");
        if (name != null)
            nameText.setText(name);
        if (barcode != null)
            barcodeText.setText(barcode);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.send) {
            sendProduct();
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendProduct() {
        String n = nameText.getText().toString();
        String b = barcodeText.getText().toString();
        //todo send product
    }
}
