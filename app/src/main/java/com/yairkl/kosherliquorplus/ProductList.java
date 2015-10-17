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
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

import static com.yairkl.kosherliquorplus.DataBaseHandler.database;

public class ProductList extends Activity implements SearchView.OnQueryTextListener {
    GridView gridView;
    Context context;
    private List<Product> proArr = new ArrayList<Product>();
    private List<Category> catArr = new ArrayList<Category>();
    private List<Identifiable> allItems = new ArrayList<Identifiable>();
    private int id;
    SearchView searchView;
    public String language = MainActivity.language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_main);
        gridView = (GridView)findViewById(R.id.gridView);
        context = this;
        id = intent.getIntExtra("CatId", 0);
        getCategoryTable();
        Category fatherCategory = database.selectCategory(id,language);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(fatherCategory.getName());

        try {
            GridAdapter adapter = new GridAdapter(this, getAllNames(this, catArr, proArr));
            setOnListItemClick();
            gridView.setAdapter(adapter);
        } catch (Exception e) {
            err(e.getMessage(), 1);
        }
    }

    public static List<ListItem> getAllNames(Context context, List<? extends Identifiable>... idss) {
        if (idss!=null) {
            List<ListItem> res = new ArrayList<ListItem>();
            for (List<? extends Identifiable> ids : idss) {
                if (ids != null) {
                    for (Identifiable e : ids)
                            res.add(new ListItem(e, context));
                }
            }
            if (res.isEmpty())
                return new ArrayList<ListItem>();
            else
                return res;
        }
        return new ArrayList<ListItem>();
    }


    public void setOnListItemClick() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                allItems.addAll(catArr);
                allItems.addAll(proArr);

                if (allItems.get(position).isCategory()) {
                    try {
                        Intent intent = new Intent(context, ProductList.class);
                        intent.putExtra("CatId", allItems.get(position).getId());
                        startActivity(intent);
                    } catch (Exception a) {
                        err("no such thing " + a.getMessage(), 3);
                    }
                } else {
                    try {
                        Log.v("list", "product selected");
                        Intent intent = new Intent(context, ProductInfo.class);
                        intent.putExtra("id", allItems.get(position).getId());
                        intent.putExtra("father_id", id);
                        startActivity(intent);
                    } catch (Exception a) {
                        err("no such thing " + a.getMessage(), 3);
                    }
                }
            }
        });

//        try {
//            Intent intent = new Intent(this, ProductList.class);
//            intent.putExtra("CatId", allItems.get(position).getId());
//            startActivity(intent);
//        } catch (Exception e) {
//            try {
//                Log.v("list","product selected");
//                Intent intent = new Intent(this, ProductInfo.class);
//                intent.putExtra("id", allItems.get(position).getId());
//                intent.putExtra("father_id",this.id);
//                startActivity(intent);
//            } catch (Exception a) {
//                err("no such thing " + a.getMessage(), 3);
//            }
//        }
    }

    private void getCategoryTable() {
        try {
            catArr.addAll(database.selectCategoriesByFather(id,language));
        } catch (Exception e) {
            err(e.getMessage(), 21);
        }
        try {
            proArr.addAll(database.selectProductsByCategory(database.selectCategory(id,language),language));
        } catch (Exception e) {
            err(e.getMessage(), 22);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list, menu);
        MenuItem searchItem = menu.findItem(R.id.searchList);
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.listScan:
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("request", 1);
                startActivity(intent);
                return true;
            case R.id.listUpdate:
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("request", 2);
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.ListInfo:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.info_title)
                            .setMessage(R.string.info_message)
                            .setCancelable(true)
                            .show();
                return true;
            case R.id.listLanguage:
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

    private void err(String str, Integer line) {
        Log.v("error_list " + line.toString(), str);
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
        List<ListItem> listItems = getAllNames(this, fProducts);
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