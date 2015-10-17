package com.yairkl.kosherliquorplus;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.yairkl.kosherliquorplus.DataBaseHandler.database;


public class ProductInfo extends Activity implements SearchView.OnQueryTextListener {
    private Product product;
    SearchView searchView;
    String language = MainActivity.language;
    ImageStorageManager ism;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        ActionBar actionBar = getActionBar();
        ism = new ImageStorageManager(this);

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (id != 0) {
            try {
                product = database.selectProduct(id, language);

                setTitle(product.getName());
                Log.v("productId", "" + id);
            } catch (Exception e) {
                Log.v("infoError", e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.product_info_not_exist, Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }
        image = (ImageView) findViewById(R.id.imageView1);
        ProgressBar progress = (ProgressBar) findViewById(R.id.imgProgress);
        TextView name = (TextView) findViewById(R.id.product_name);
        TextView barcode = (TextView) findViewById(R.id.product_barcode);
        TextView kashrut = (TextView) findViewById(R.id.product_kashrut);
        try {
            if (id != 0) {
                ism.getImage(product,1,image,progress);
                String[] strings = product.getKashrut().split("\\,");
                List<String> kashList = new ArrayList<String>();
                String strkash = "";
                for (String str : strings) {
                    if (!kashList.contains(str)) {
                        kashList.add(str);
                        strkash = strkash + "*" + str + "\n";
                    }
                }


                name.setText(product.getName());
                barcode.setText(product.getBarcode());
                kashrut.setText(strkash);
            }
        } catch (Exception e) {
            Log.v("lalalala", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.product_info, menu);
        MenuItem searchItem = menu.findItem(R.id.searchInf);
        searchView = (SearchView) searchItem.getActionView();
        setupSearchView(searchItem);
        return true;
    }

    private void setupSearchView(MenuItem searchItem) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            searchView.setIconifiedByDefault(false);
        } else {
            searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();

            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null
                        && inf.getSuggestAuthority().startsWith("applications")) {
                    info = inf;
                }
            }
            searchView.setSearchableInfo(info);
        }

        searchView.setOnQueryTextListener(this);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        final Intent intent;
        switch (item.getItemId()) {
            case R.id.iScan:
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("request", 1);
                startActivity(intent);
                return true;
            case R.id.iUpdate:
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("request", 2);
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
//                if (father.getId() == 1) {
//                    onBackPressed();
//                    return true;
//                }
//                if (father.getId() == 2){
//                    intent = new Intent(this,MainActivity.class);
//                    startActivity(intent);
//                    return true;
//                }
//                intent = new Intent(this,ProductList.class);
//                intent.putExtra("CatId",father.getId());
//                startActivity(intent);
                return true;
            case R.id.iInfo:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                if (Locale.getDefault().getLanguage().equals("iw"))
//                    builder.setTitle("Information")
//                            .setMessage("הרשימה מנוהלת על ידי הרב שמואל זמלמן מרבנות ירושלים")
//                            .setCancelable(true)
//                            .show();
//                else
                builder.setTitle(R.string.info_title)
                        .setMessage(R.string.info_message)
                        .setCancelable(true)
                        .show();
                return true;
            case R.id.iLanguage:
                AlertDialog.Builder langBuilder = new AlertDialog.Builder(this);
                langBuilder.setTitle(R.string.language_dialog_title)
                        .setView(MainActivity.languageDialog(this))
                        .setCancelable(true).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });
                langBuilder.show();
                return true;


        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
        intent.putExtra("query", s);
        startActivity(intent);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        final List<Product> products = database.searchProducts(s);

        if (products.get(0) == Product.NULL)
            products.set(0, new Product(0, getResources().getString(R.string.search_no_suggestion), "", "", 0));
        List<Product> fProducts = new ArrayList<Product>();
        for (int i = 0; i < 5; i++)
            if (products.size() > i)
                fProducts.add(products.get(i));
        List<ListItem> listItems = ProductList.getAllNames(this, new ArrayList<Identifiable>(), fProducts);
        String[] columns = new String[]{"_id", "listItem"};
        Object[] temp = new Object[]{0, new ListItem(Identifiable.EMPTY, this)};

        MatrixCursor cursor = new MatrixCursor(columns);

        for (int i = 0; i < listItems.size(); i++) {
            temp[0] = i;
            temp[1] = listItems.get(i);

            cursor.addRow(temp);
        }
        searchView.setSuggestionsAdapter(new ItemCursorAdapter(this, cursor, listItems));
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {

                if (products.get(i).getName().equals(getResources().getString(R.string.search_no_suggestion)))
                    return true;
                Intent intent = new Intent(getApplicationContext(), ProductInfo.class);
                intent.putExtra("id", products.get(i).getId());
                intent.putExtra("father_id", 2);
                startActivity(intent);
                return true;
            }
        });
        return false;
    }
}