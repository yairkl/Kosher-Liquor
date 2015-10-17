package com.yairkl.kosherliquorplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.yairkl.kosherliquorplus.DataBaseHandler.database;
import static com.yairkl.kosherliquorplus.ProductList.getAllNames;


public class MainActivity extends Activity implements SearchView.OnQueryTextListener {
    private List<Product> proArr = new ArrayList<Product>();
    private List<Category> catArr = new ArrayList<Category>();
    private final Integer CatId = 21;
    private GridView list;
    private Context context = this;
    private ProgressDialog progressDialog;
    private boolean firstRun;
    private SearchView mSearchView;
    public static String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageStorageManager imageStorageManager = new ImageStorageManager(this);
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.bottle_icon);
        Bitmap catBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.category_icon_grey);
        firstRun = FirstRun();
        try {
            imageStorageManager.saveImage("caTemp", catBitmap);
            imageStorageManager.saveImage("temp", bitmap);
        } catch (IOException e) {
            Log.e("save Image", e.getMessage());
        }

        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        database.openDB(this);
        language = database.getLanguage(this);
        list = (GridView) findViewById(R.id.gridView);
        int request = getIntent().getIntExtra("request", 0);
        switch (request) {
            case 1:
                try {
                    scanBarcode();
                } catch (Exception e) {
                    Log.e("barcode failed", e.getMessage());
                }
                break;
            case 2:
                update();
                break;
            default:
                try {
                    if (isOnline(this)) {
                        ShowProgressDialog();
                    }
                } catch (Exception e) {
                    err(e.getMessage());
                }

        }
        setListListener();
        List<Category> topCat = database.selectCategoriesByFather(CatId, language);
        if (topCat != null) {
            catArr = topCat;
        }

        setAdapter();
    }

    private void setAdapter() {
        try {
            list.setAdapter(new GridAdapter(this, getAllNames(this, catArr, proArr)));
        } catch (Exception e) {
            Log.e("adapter", e.getMessage());
        }
    }

    private void setListListener() {
        try {
            list.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                    Intent intent = new Intent(context, ProductList.class);

                    try {
                        int nCatId = catArr.get(position).getId();
                        intent.putExtra("CatId", nCatId);
                    } catch (Exception e) {
                        err(e.getMessage());
                    }

                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            err(e.getMessage());
        }

    }

    private void update() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.update_title)
                .setMessage(R.string.update_message)
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ShowProgressDialog();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();

        setAdapter();
        setListListener();
    }

    private void createDialog(int titleId, int messageId, final Callable<Void> onPositivePress) {
        new AlertDialog.Builder(this)
                .setTitle(titleId)
                .setMessage(messageId)
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            onPositivePress.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    private void ShowProgressDialog() {
        try {
            BackgroundTask backgroundTask = new BackgroundTask(this);
            backgroundTask.execute();
        } catch (Exception e) {
            Log.e("backgroundTask", e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        try {
            MenuItem searchItem = menu.findItem(R.id.search);
            mSearchView = (SearchView) searchItem.getActionView();
            setupSearchView(searchItem);
        } catch (Exception e) {
            Log.e("search", e.getMessage());
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.scan:
                try {
                    scanBarcode();
                } catch (Exception e) {
                    Log.e("barcode_Scan", e.getMessage());
                }
                return true;
            case R.id.update:
                createDialog(R.string.update_title, R.string.update_message, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        if (isOnline(context))
                            ShowProgressDialog();
                        else
                            Toast.makeText(context, R.string.update_network_down, Toast.LENGTH_LONG).show();
                        return null;
                    }
                });
                return true;
            case R.id.info:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.info_title)
                        .setMessage(R.string.info_message)
                        .setCancelable(true)
                        .show();
                return true;
            case R.id.language:
                AlertDialog.Builder langBuilder = new AlertDialog.Builder(this);
                langBuilder.setTitle(R.string.language_dialog_title)
                        .setView(languageDialog(this))
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

    public static View languageDialog(final Context context) {
        final RadioButton englishButton = new RadioButton(context);
        final RadioButton hebrewButton = new RadioButton(context);
        englishButton.setText("English");
        hebrewButton.setText("עברית");
        englishButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    database.saveAppLanguage("en", context);
                    language = "en";
                    hebrewButton.setChecked(false);
                }
            }
        });
        hebrewButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    database.saveAppLanguage("iw", context);
                    language = "iw";
                    englishButton.setChecked(false);

                }
            }
        });

        if (language.equals("iw"))
            hebrewButton.setChecked(true);
        else
            englishButton.setChecked(true);

        final RadioGroup radioGroup = new RadioGroup(context);
        radioGroup.addView(englishButton);
        radioGroup.addView(hebrewButton);

        return radioGroup;
    }

    private void scanBarcode() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            Toast.makeText(this, R.string.barcode_no_xzing, Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://search?q=pname:com.google.zxing.client.android");
            Intent uriIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(uriIntent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String barcode = intent.getStringExtra("SCAN_RESULT");
                try {
                    Product product = database.selectProductByBarcode(barcode, language);
                    if (!product.isEmpty()) {
                        int proId = product.getId();
                        onClickProduct(proId);
                    } else {
                        Toast.makeText(this, R.string.barcode_error, Toast.LENGTH_SHORT).show();
                        barcodeNotFoundDialog(barcode);
//                        Intent intent1 = new Intent(this,newProduct.class);
//                        intent1.putExtra("barcode",barcode);
//                        startActivity(intent1);
                    }
                } catch (Exception e) {
                    err(e.getMessage());
                }

            } else if (resultCode == RESULT_CANCELED) {
                err("Scan cancelled");
            }

        } else {
            String str = String.valueOf(requestCode);
            err(str);
        }
    }

    public void barcodeNotFoundDialog(final String barcode) {
        final EditText productName = new EditText(this);
        productName.setHint(R.string.barcode_not_found_hint);
        productName.setGravity(Gravity.CENTER);
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.barcode_not_found_title);
        builder.setMessage(barcode);
        builder.setIcon(R.drawable.barcode_icon);

        builder.setView(productName);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendNewProduct(productName.getText().toString(), barcode);
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void sendNewProduct(String name, String barcode) {
        if (name.equals("")) {
            Toast.makeText(this, R.string.barcode_name_not_inserted, Toast.LENGTH_SHORT).show();
            barcodeNotFoundDialog(barcode);
        } else {
            String newName = name.replaceAll(" ", "%20");
            final HttpClient httpClient = new DefaultHttpClient();
            final HttpGet get = new HttpGet("http://kosherliquorlist.com/send.php?name=" + newName + "&barcode=" + barcode);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isOnline(context)) {
                            httpClient.execute(get);
                        } else {
                            boolean looper = true;
                            while (looper) {
                                Thread.sleep(60000);
                                if (isOnline(context)) {
                                    httpClient.execute(get);
                                    looper = false;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private boolean FirstRun() {
        boolean firstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstRun", true);
        if (firstRun) {
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("firstRun", false)
                    .commit();
            return true;
        }
        return false;
    }

    private void onClickProduct(Integer proId) {
        try {
            Product pro = database.selectProduct(proId, language);

            if (pro != null) {
                Intent intent = new Intent(this, ProductInfo.class);
                intent.putExtra("name", pro.getName());
                intent.putExtra("id", pro.getId());
                startActivity(intent);
            } else {
                err("there is no such product");
            }
        } catch (Exception e) {
            err(e.getMessage());
        }
    }

    private void err(String str) {
        Log.v("error", str);
    }

    private void setupSearchView(MenuItem searchItem) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mSearchView.setIconifiedByDefault(false);
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
            mSearchView.setSearchableInfo(info);
        }

        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra("query", s);
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        Log.v("search TextChange", s);
        if (!s.equals("")) {
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
            mSearchView.setSuggestionsAdapter(new ItemCursorAdapter(this, cursor, listItems));
            mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionSelect(int i) {
                    return false;
                }

                @Override
                public boolean onSuggestionClick(int i) {

                    if (products.get(i).getName().equals(getResources().getString(R.string.search_no_suggestion)))
                        return true;
                    Intent intent = new Intent(context, ProductInfo.class);
                    intent.putExtra("id", products.get(i).getId());
                    intent.putExtra("father_id", 2);
                    startActivity(intent);
                    return true;
                }
            });
        }
        return true;

    }

    private class BackgroundTask extends AsyncTask<Void, String, Void> {
        Context context;

        BackgroundTask(Context mContext) {
            this.context = mContext;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(R.string.updating_title);
            progressDialog.setMessage(getResources().getString(R.string.updating_message));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(true);
            if (firstRun)
                progressDialog.setCancelable(false);
            else
                progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (isOnline(context)) {
                try {
                    fillDataBase();
                } catch (Exception e) {
                    Log.e("error Update", e.getMessage());
                }
                progressDialog.setIndeterminate(true);
                try {
                    publishProgress(getResources().getString(R.string.getting_language));
                    try {
                        getLang("iw");
                    } catch (Exception e) {
                        Log.e("error Update", e.getMessage());
                    }
                } catch (Exception e) {
                    Log.e("iw", e.getMessage());
                }
                publishProgress(getResources().getString(R.string.clean_message));
                database.cleanCategories();
                publishProgress(getResources().getString(R.string.background_done));
            } else
                publishProgress("TOAST", getResources().getString(R.string.update_network_down));
            return null;
        }

        @Override
        protected void onProgressUpdate(String... strings) {
            if (strings[0].equals("START")) {
                progressDialog.setIndeterminate(false);
                progressDialog.setProgress(0);
                progressDialog.setMax(Integer.parseInt(strings[1]));
            } else if (strings[0].equals("TOAST"))
                Toast.makeText(context, strings[1], Toast.LENGTH_LONG).show();
            else {
                incrementProgress();
                try {
                    progressDialog.setMessage(strings[0]);
                } catch (Exception e) {
                    Log.v("setDialogName", "name is null");
                    try {
                        progressDialog.setMessage(getResources().getString(R.string.updating_data));
                    } catch (Exception le) {
                        Log.e("noDialog", le.getMessage());
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Void n) {
            catArr = database.selectCategoriesByFather(CatId, language);
            list.setAdapter(new GridAdapter(context, getAllNames(context, catArr, proArr)));
            setListListener();
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                Log.e("no Dialog", e.getMessage());
            }
        }

        private String getValueByTagName(Element element, String tag) {
            NodeList nodeList = element.getElementsByTagName(tag);
            Element mElement = (Element) nodeList.item(0);
            nodeList = mElement.getChildNodes();
            return nodeList.item(0).getNodeValue();
        }

        private void fillDataBase() {
            final String productUrl = "http://kosherliquorlist.com/yairExport.php?req=product&date=" + database.getLastUpdateDate(context);
            final String key_product = "product";
            final String key_product_id = "id_product";
            final String key_product_barcode = "barcode";
            final String key_product_name = "name";
            final String key_product_category = "id_category";
            final String key_product_image = "image_url";
            final String key_product_kashrut = "kashrut";
            final String key_product_date = "date";
            final String categoryUrl = "http://kosherliquorlist.com/yairExport.php?req=category&date=" + database.getLastUpdateDate(context);
            final String key_category = "category";
            final String key_category_name = "name";
            final String key_category_id = "id_category";
            final String key_category_father = "id_parent";
            final String key_category_image = "image_url";

            Log.v("fillData","alla");

            try {
                XmlParser parser = new XmlParser();

                Document doc = parser.DOMParser(productUrl);
                NodeList productNL = doc.getElementsByTagName(key_product);

                Document catDoc = parser.DOMParser(categoryUrl);
                NodeList catNL = catDoc.getElementsByTagName(key_category);

                int updateLength = productNL.getLength() + catNL.getLength();
                if (updateLength == 0)
                    return;
                publishProgress("START", "" + updateLength);
                for (int i = 0; i < catNL.getLength(); i++) {
                    try {
                        Node node = catNL.item(i);
                        Element fstElmnt = (Element) node;
                        String catId = getValueByTagName(fstElmnt, key_category_id);
                        String fatherID = getValueByTagName(fstElmnt, key_category_father);
                        String catName = getValueByTagName(fstElmnt, key_category_name);
                        String catImage = getValueByTagName(fstElmnt, key_category_image);

                        publishProgress(getResources().getString(R.string.downloading_pre)
                                + " " + catName + " " + getResources().getString(R.string.downloading_post));

                        String name = DatabaseUtils.sqlEscapeString(catName);
                        Integer id = Integer.parseInt(catId);
                        Integer fatherId = Integer.parseInt(fatherID);
                        database.insertCategory(new Category(id, name, fatherId), catImage);
                    } catch (Exception e) {
                        Log.v("category load error" + i, e.getMessage());
                    }
                }
                for (int i = 0; i < productNL.getLength(); i++) {
                    try {
                        Node node = productNL.item(i);
                        Element fstElmnt = (Element) node;

                        String dateStr = getValueByTagName(fstElmnt, key_product_date);
                        String idStr = getValueByTagName(fstElmnt, key_product_id);

                        String proNameStr = getValueByTagName(fstElmnt, key_product_name);
                        String barcodeStr = getValueByTagName(fstElmnt, key_product_barcode);
                        String catIdStr = getValueByTagName(fstElmnt, key_product_category);
                        String imageStr = getValueByTagName(fstElmnt, key_product_image);
                        String kashStr = getValueByTagName(fstElmnt, key_product_kashrut);

                        Integer id = null;
                        try {
                            id = Integer.parseInt(idStr);
                            Integer date = Integer.parseInt(dateStr);
                            Product p = new Product(id,
                                    DatabaseUtils.sqlEscapeString(proNameStr),
                                    barcodeStr,
                                    DatabaseUtils.sqlEscapeString(kashStr), date);
                            database.insertProduct(p, imageStr);
                        } catch (Exception e) {
                            Log.v("failed insert pro " + id, e.getMessage());
                        }

                        publishProgress(getResources().getString(R.string.downloading_pre)
                                + " " + proNameStr + " " + getResources().getString(R.string.downloading_post));

                        try {
                            database.insertConnection(new ProCat(Integer.parseInt(idStr),
                                    Integer.parseInt(catIdStr)));
                        } catch (Exception e) {
                            err(e.getMessage() + idStr + ", " + catIdStr);
                        }

                    } catch (Exception e) {
                        err("product " + i + " is Corrupted ");
                    }
                }
                database.saveCurrentDate(context);
            } catch (Exception e) {
                Log.v("update Failed", e.getMessage());
                publishProgress("TOAST", getResources().getString(R.string.update_error));
            }
        }

        private void getLang(String lang) {
            final String productUrl = "http://kosherliquorlist.com/yairExport.php?req=product&date="
                    + database.getLastUpdateHebDate(context) + "&lang=" + lang;
            final String key_product = "product";
            final String key_product_id = "id_product";
            final String key_product_name = "name";
            final String key_product_kashrut = "kashrut";
            final String categoryUrl = "http://kosherliquorlist.com/yairExport.php?req=category&date="
                    + database.getLastUpdateHebDate(context) + "&lang=" + lang;
            final String key_category = "category";
            final String key_category_name = "name";
            final String key_category_id = "id_category";

            XmlParser parser = new XmlParser();

            Document doc = parser.DOMParser(productUrl);
            NodeList productNL = doc.getElementsByTagName(key_product);

            Document catDoc = parser.DOMParser(categoryUrl);
            NodeList catNL = catDoc.getElementsByTagName(key_category);

            int updateLength = productNL.getLength() + catNL.getLength();
            if (updateLength == 0)
                return;

            for (int i = 0; i < catNL.getLength(); i++) {
                Node node = catNL.item(i);
                Element fstElmnt = (Element) node;

                int catId = Integer.parseInt(getValueByTagName(fstElmnt, key_category_id));
                String catName = DatabaseUtils.sqlEscapeString(getValueByTagName(fstElmnt, key_category_name));
                Log.v("heb", catName);
                database.insertHebCategory(catId, catName);
            }

            for (int i = 0; i < productNL.getLength(); i++) {
                Node node = productNL.item(i);
                Element fstElmnt = (Element) node;

                int proId = Integer.parseInt(getValueByTagName(fstElmnt, key_product_id));
                String proName = DatabaseUtils.sqlEscapeString(getValueByTagName(fstElmnt, key_product_name));
                String proKash = DatabaseUtils.sqlEscapeString(getValueByTagName(fstElmnt, key_product_kashrut));
                Log.v("heb", proName);

                database.insertHebProduct(proId, proName, proKash);
            }
            database.saveCurrentHebDate(context);
        }

        private void incrementProgress() {
            new Handler().post(new Runnable() {
                public void run() {
                    progressDialog.incrementProgressBy(1);
                }
            });
        }
    }

}
