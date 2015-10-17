package com.yairkl.kosherliquorplus;

public class Product implements Identifiable {
    private final String name;
    private final String barcode;
    private final int id;
    private final String kash;
    private final int date;

    public Product(int id, String name, String barcode, String kash, int date) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.kash = kash;
        this.date = date;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isCategory(){
        return false;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getKashrut() {
        return kash;
    }

    public int getDate() {
        return date;
    }

    boolean isEmpty() {
        return false;
    }

    static class EmptyProduct extends Product {
        boolean isEmpty() {
            return true;
        }

        public EmptyProduct() {
            super(0, "", "", "", 0);
        }

        @Override
        public String getName(){
            //throw new RuntimeException("get empty name");
            return "empty product";
        }
    }

    static final Product NULL = new EmptyProduct();
}
