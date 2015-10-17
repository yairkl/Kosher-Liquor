package com.yairkl.kosherliquorplus;

public class ProCat {
    private int productId, catId;

    public ProCat(int productId, int catId) {
        this.productId = productId;
        this.catId = catId;
    }

    public int getProId() {
        return productId;
    }

    public int getCatId() {
        return catId;
    }
}
