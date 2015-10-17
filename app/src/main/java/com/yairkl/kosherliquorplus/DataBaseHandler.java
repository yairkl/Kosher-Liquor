package com.yairkl.kosherliquorplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public enum DataBaseHandler {
    database;

    private SQLiteDatabase sql = null;
    private static final String key_DataBase_name = "my_liquor_shop";
    private static final String key_table_product = "product";
    private static final String key_table_categories = "categories";
    private static final String key_table_proCat = "proCat";
    private static final String key_table_heb_product = "pro_heb";
    private static final String key_table_heb_cat = "cat_heb";
    private static final String prefName = "catdate";
    private static final String key_product_images = "proImg";
    private static final String key_category_images = "catImg";



    void openDB(Context context) {
        try {
            sql = context.openOrCreateDatabase(key_DataBase_name, 0, null);
            createTables();
        } catch (Exception e) {
            Log.e("openDB",e.getMessage());
        }
    }

    public void createTables() {
        sql.execSQL("CREATE TABLE IF NOT EXISTS " + key_table_product +
                "(id INT UNIQUE, product_name VARCHAR(60) , barcode VARCHAR(20) ," +
                " kashrut VARCHAR , date INT DEFAULT 0);");

        sql.execSQL("CREATE TABLE IF NOT EXISTS " + key_table_categories +
                " (id INT UNIQUE, cat_name VARCHAR(60), father_id INT);");

        sql.execSQL("CREATE TABLE IF NOT EXISTS " + key_table_proCat +
                "(product_id INT, cat_id INT, UNIQUE (product_id, cat_id))");

        sql.execSQL("CREATE TABLE IF NOT EXISTS " + key_table_heb_product +
                "(id INT UNIQUE, name VARCHAR(60),kashrut VARCHAR);");

        sql.execSQL("CREATE TABLE IF NOT EXISTS " + key_table_heb_cat +
                "(id INT UNIQUE, name VARCHAR(60));");

        sql.execSQL("CREATE TABLE IF NOT EXISTS " + key_product_images +
                "(id INT UNIQUE, url VARCHAR, isDownloaded INT(1) DEFAULT 0);");

        sql.execSQL("CREATE TABLE IF NOT EXISTS " + key_category_images +
                "(id INT UNIQUE, url VARCHAR, isDownloaded INT(1) DEFAULT 0);");
    }

    public void insertProduct(Product product,String url) {
        if (product.isEmpty())
            return;
        sql.execSQL("INSERT OR REPLACE INTO " + key_table_product + " VALUES (" + product.getId() + ", "
                + product.getName() + ", " + product.getBarcode() + ", " + product.getKashrut() + ", " + product.getDate() + ");");
        insertImage(product,url);
    }

    public void insertCategory(Category category, String url) {
        if (category.isEmpty()){
            Log.v("category insert error","category empty");
            return;
        }
        sql.execSQL("INSERT OR REPLACE INTO " + key_table_categories + " VALUES ("
                + category.getId() + ", " + category.getName() + ", " + category.getFatherId() + ");");

        insertImage(category,url);
    }

    public void insertConnection(ProCat connection) {
        sql.execSQL("INSERT OR REPLACE INTO " + key_table_proCat + " VALUES ("
                + connection.getProId() + ", " + connection.getCatId() + ");");
    }

    public void insertImage(Identifiable i,String url){
        if (i.isCategory())
            sql.execSQL("INSERT OR REPLACE INTO " + key_category_images + " VALUES ("+i.getId()+", "+DatabaseUtils.sqlEscapeString(url)+", 0"+");");
        else
            sql.execSQL("INSERT OR REPLACE INTO " + key_product_images + " VALUES ("+i.getId()+", "+DatabaseUtils.sqlEscapeString(url)+", 0"+");");
    }

    public String getImageUrl(Identifiable i){
        Cursor cursor;
        if (i.isCategory())
            cursor = sql.rawQuery("SELECT url FROM " + key_category_images + " WHERE id = " + i.getId(), null);
        else
            cursor = sql.rawQuery("SELECT url FROM " + key_product_images + " WHERE id = " + i.getId(), null);
        if (cursor == null || !cursor.moveToFirst()) {
            return "";
        }
        String url = cursor.getString(cursor.getColumnIndex("url"));
        cursor.close();
        return url;
    }

    public void setImageDownloaded(Identifiable i){
        if (i.isCategory())
            sql.execSQL("UPDATE "+ key_category_images +" SET isDownloaded = 1 WHERE id="+i.getId());
        else
            sql.execSQL("UPDATE "+ key_product_images +" SET isDownloaded = 1 WHERE id="+i.getId());
    }

    public boolean isImageDownloaded(Identifiable i){
        Cursor cursor;
        if (i.isCategory())
            cursor = sql.rawQuery("SELECT isDownloaded FROM " + key_category_images + " WHERE id = " + i.getId(), null);
        else
            cursor = sql.rawQuery("SELECT isDownloaded FROM " + key_product_images + " WHERE id = " + i.getId(), null);
        if (cursor == null || !cursor.moveToFirst())
            return false;
        int b = cursor.getInt(cursor.getColumnIndex("isDownloaded"));
        cursor.close();
        return !(b == 0);
    }

    public Product selectProduct(int id,String lang) {

        Cursor cursor = sql.rawQuery("SELECT * FROM " + key_table_product + " WHERE id = " + id, null);
        if (cursor == null || !cursor.moveToFirst())
            return Product.NULL;
        return getProduct(cursor,lang);
    }

    public List<Product> searchProducts(String query) {
        if (!isEnglish(query))
            return searchHebProducts(query);

        String name = DatabaseUtils.sqlEscapeString(query);
        List<Product> products = new ArrayList<Product>();
        if (!name.equals("")) {
            name = DatabaseUtils.sqlEscapeString(query+"%");
            Cursor cursor = sql.rawQuery("SELECT DISTINCT * FROM " + key_table_product + " WHERE product_name LIKE " + name + " GROUP BY product_name ORDER BY product_name", null);
            if (cursor!=null&&cursor.moveToFirst()) {
                do {
                    products.add(getProduct(cursor, "en"));
                } while (cursor.moveToNext());
                return products;
            }
            name = DatabaseUtils.sqlEscapeString("%"+query+"%");
            cursor = sql.rawQuery("SELECT DISTINCT * FROM " + key_table_product + " WHERE product_name LIKE " + name + " GROUP BY product_name ORDER BY product_name", null);
            if (cursor!=null&&cursor.moveToFirst()) {
                do {
                    products.add(getProduct(cursor, "en"));
                } while (cursor.moveToNext());
                return products;
            }
        }
        products.add(Product.NULL);
        return products;
    }

    private List<Product> searchHebProducts(String query) {
        String name = query;

        List<Product> products = new ArrayList<Product>();
        if (name.equals("")){
            products.add(Product.NULL);
            return products;
        }
        name = DatabaseUtils.sqlEscapeString(query+"%");
        Cursor cursor = sql.rawQuery("SELECT DISTINCT id FROM " + key_table_heb_product + " WHERE name LIKE " + name + " GROUP BY name ORDER BY name", null);
        if (cursor!=null&&cursor.moveToFirst()) {
            do {
                products.add(selectProduct(cursor.getInt(cursor.getColumnIndex("id")), "iw"));
            } while (cursor.moveToNext());
            cursor.close();
            return products;
        }
        name = DatabaseUtils.sqlEscapeString("%"+query+"%");
        cursor = sql.rawQuery("SELECT DISTINCT * FROM " + key_table_heb_product + " WHERE name LIKE " + name + " GROUP BY name ORDER BY name", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                products.add(selectProduct(cursor.getInt(cursor.getColumnIndex("id")), "iw"));
            } while (cursor.moveToNext());
            cursor.close();
            return products;
        }
        if (products.isEmpty())
            products.add(Product.NULL);
            return products;
    }

    public Product selectProductByBarcode(String barcode,String lang) {
        Cursor cursor = sql.rawQuery("SELECT * FROM " + key_table_product + " WHERE barcode = " + barcode, null);
        if (cursor == null || !cursor.moveToFirst()) {
            cursor = sql.rawQuery("SELECT * FROM " + key_table_product + " WHERE kashrut LIKE '%" + barcode + "%'",null);
            if (cursor == null || !cursor.moveToFirst())
                return Product.NULL;
        }
        return getProduct(cursor,lang);
    }

    private Product getProduct(Cursor cursor,String lang) {
        if (lang.equals("iw")){
                return new Product(cursor.getInt(cursor.getColumnIndex("id")),
                        getProductHebNameById(cursor.getInt(cursor.getColumnIndex("id"))),
                        cursor.getString(cursor.getColumnIndex("barcode")),
                        getProductHebKashrutById(cursor.getInt(cursor.getColumnIndex("id"))),
                        cursor.getInt(cursor.getColumnIndex("date")));
        }
        return new Product(cursor.getInt(cursor.getColumnIndex("id")),
                cursor.getString(cursor.getColumnIndex("product_name")),
                cursor.getString(cursor.getColumnIndex("barcode")),
                cursor.getString(cursor.getColumnIndex("kashrut")),
                cursor.getInt(cursor.getColumnIndex("date")));
    }

    public List<Product> selectAllProduct(String lang) {
        List<Product> allProduct = new ArrayList<Product>();
        Cursor cursor = sql.rawQuery("SELECT * FROM " + key_table_product, null);
        if (cursor == null || !cursor.moveToFirst())
            return allProduct;
        do {
            allProduct.add(getProduct(cursor,lang));
        } while (cursor.moveToNext());
        return allProduct;
    }

    public List<Product> selectProductsByCategory(Category category,String lang) {
        List<Product> products = new ArrayList<Product>();
        List<ProCat> proCats = selectProductCategory(category);
        for (ProCat p : proCats)
            products.add(selectProduct(p.getProId(),lang));
        return products;
    }

    public Category selectCategory(int id,String lang) {
        Cursor cursor = sql.rawQuery("SELECT * FROM " + key_table_categories + " WHERE id = " + id, null);
        //Log.v("selectCategory cursor",""+cursor.getCount());
        if (cursor != null && cursor.moveToFirst()) {
            return getCategory(cursor,lang);
        }
        return Category.NULL;
    }

    public List<Category> selectAllCategories() {
        List<Category> categories = new ArrayList<Category>();
        Cursor cursor = sql.rawQuery("SELECT * FROM " + key_table_categories, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                categories.add(getCategory(cursor,"en"));
            } while (cursor.moveToNext());
        }
        return categories;
    }

    public void cleanCategories(){
        try {
            List<Category> categories = selectAllCategories();
            for (Category category : categories) {
                if (selectProductCategory(category).size() == 0 && selectCategoriesByFather(category.getId(),"en").size() == 0) {
                    sql.execSQL("DELETE FROM " + key_table_categories + " WHERE id = " + category.getId());
                }
            }
        }catch (Exception e){
            Log.e("clean",e.getMessage());
        }
    }

    private Category getCategory(Cursor cursor,String lang) {
        if (lang.equals("iw")&&!getCategoryHebNameById(cursor.getInt(cursor.getColumnIndex("id"))).equals("")){
            return new Category(cursor.getInt(cursor.getColumnIndex("id")),
                    getCategoryHebNameById(cursor.getInt(cursor.getColumnIndex("id"))),
                    cursor.getInt(cursor.getColumnIndex("father_id")));
        }
        return new Category(cursor.getInt(cursor.getColumnIndex("id")),
                cursor.getString(cursor.getColumnIndex("cat_name")),
                cursor.getInt(cursor.getColumnIndex("father_id")));
    }

    public List<Category> selectCategoriesByFather(int father_id,String lang) {
        List<Category> categories = new ArrayList<Category>();
//        String[] colnames = new String[]{"id", "cat_name", "father_id"};
//        sql.query(key_table_categories, colnames,
//                "father_id=?", new String[]{father_id.toString()}, null, null, null, null);
        Cursor cursor = sql.rawQuery("SELECT * FROM " + key_table_categories + " WHERE father_id = " + father_id, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                categories.add(getCategory(cursor,lang));
            } while (cursor.moveToNext());
        }
        return categories;
    }

    public ArrayList<ProCat> selectProductCategory(Category category) {
        Cursor cursor = sql.rawQuery("SELECT * FROM " + key_table_proCat + " WHERE cat_id = " + category.getId(), null);
        ArrayList<ProCat> proFromCat = new ArrayList<ProCat>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                proFromCat.add(new ProCat(cursor.getInt(cursor.getColumnIndex("product_id")),
                        cursor.getInt(cursor.getColumnIndex("cat_id"))));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return proFromCat;
    }

    public void insertHebProduct(int id, String hebName,String kashrut){
        sql.execSQL("INSERT OR REPLACE INTO " + key_table_heb_product + " VALUES ("
                +id +", "+ hebName + ","+kashrut+");");

    }

    public String getProductHebNameById(int id){
        try {
            Cursor cursor = sql.rawQuery("SELECT name FROM " + key_table_heb_product + " WHERE id=" + id, null);
            if (cursor != null && cursor.moveToFirst()) {
                String s = cursor.getString(cursor.getColumnIndex("name"));
                cursor.close();
                return s;
            }
        }catch (Exception e){
            return selectProduct(id,"en").getName();
        }
        return selectProduct(id,"en").getName();
    }

    public String getProductHebKashrutById(int id){
        try {
            Cursor cursor = sql.rawQuery("SELECT kashrut FROM " + key_table_heb_product + " WHERE id=" + id, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex("kashrut"));
            }
        }catch (Exception e){
            return selectProduct(id,"en").getKashrut();
        }
        return selectProduct(id,"en").getKashrut();
    }

    public void insertHebCategory(int id, String hebName){
        sql.execSQL("INSERT OR REPLACE INTO " + key_table_heb_cat + " VALUES ("
                +id+", "+ hebName + ");");
    }

    public String getCategoryHebNameById(int id){
        try {
            Cursor cursor = sql.rawQuery("SELECT name FROM " + key_table_heb_cat + " WHERE id=" + id, null);
            if (cursor != null && cursor.moveToFirst()) {
                String s = cursor.getString(cursor.getColumnIndex("name"));
                cursor.close();
                return s;
            }
        }catch (Exception e){
            selectCategory(id,"en").getName();
        }
        return selectCategory(id,"en").getName();
    }

    public void saveCurrentDate(Context context){
        SharedPreferences.Editor editor;
        editor = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        editor.putString("lastUpdate",ts);
        editor.apply();
        Log.v("date", ts);
    }

    public int getLastUpdateDate(Context context){
        SharedPreferences pref;
        pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return Integer.parseInt(pref.getString("lastUpdate", "0"));
    }

    public void saveCurrentHebDate(Context context){
        SharedPreferences.Editor editor;
        editor = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        editor.putString("lastHebUpdate",ts);
        editor.apply();
        Log.v("date",ts);
    }

    public int getLastUpdateHebDate(Context context){
        SharedPreferences pref;
        pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return Integer.parseInt(pref.getString("lastHebUpdate", "0"));
    }

    public void saveAppLanguage(String language,Context context){
        SharedPreferences.Editor editor;
        editor = context.getSharedPreferences(prefName,Context.MODE_PRIVATE).edit();
        editor.putString("language",language);
        editor.apply();
    }

    public String getLanguage(Context context){
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(prefName,Context.MODE_PRIVATE);
        return preferences.getString("language", Locale.getDefault().getLanguage());
    }

    private boolean isEnglish(String query){
        boolean isEnglish = true;
        for ( char c : query.toCharArray() ) {
            if ( Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN ) {
                isEnglish = false;
                break;
            }
        }
        return isEnglish;
    }

}
