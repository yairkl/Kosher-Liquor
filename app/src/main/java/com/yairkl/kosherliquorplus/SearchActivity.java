package com.yairkl.kosherliquorplus;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.yairkl.kosherliquorplus.DataBaseHandler.database;

public class SearchActivity extends ListActivity implements SearchView.OnQueryTextListener{
    SearchView searchView;
    String query;
    List<Product> products;
    String language = MainActivity.language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        query = intent.getStringExtra("query");
        setTitle("search: "+query+"...");
        ActionBar actionBar = getActionBar();
        if(actionBar !=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setCustomView(searchView);
        }
        products = database.searchProducts(query);
        if (products.get(0)==Product.NULL)
            products.set(0,new Product(0,getResources().getString(R.string.search_no_suggestion),"","",0));

        if (products!=null)
            setListAdapter(new ItemAdapter(this,ProductList.getAllNames(this,null, products)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.searchS);
        searchView = (SearchView) searchItem.getActionView();
        setupSearchView(searchItem);
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (products.get(position).getId()!=0) {
            Intent intent = new Intent(this, ProductInfo.class);
            intent.putExtra("id", products.get(position).getId());
            intent.putExtra("father_id", 1);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
//            Intent intent = new Intent(this,MainActivity.class);
//            startActivity(intent);
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Intent intent = new Intent(this,SearchActivity.class);
        intent.putExtra("query",s);
        startActivity(intent);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        final List<Product> products = database.searchProducts(s);
        if (products.get(0)==Product.NULL)
            products.set(0,new Product(0,getResources().getString(R.string.search_no_suggestion),"","",0));
        List<Product> fProducts = new ArrayList<Product>();
        for (int i = 0;i<5;i++)
            if (products.size()>i)
                fProducts.add(products.get(i));
        List<ListItem> listItems = ProductList.getAllNames(this,fProducts);
        String[] columns = new String[]{"_id","listItem"};
        Object[] temp = new Object[] { 0, new ListItem(Identifiable.EMPTY,this) };

        MatrixCursor cursor = new MatrixCursor(columns);

        for (int i = 0;i<listItems.size();i++){
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
                intent.putExtra("father_id", 1);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

    private void setupSearchView(MenuItem searchItem) {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
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

}
