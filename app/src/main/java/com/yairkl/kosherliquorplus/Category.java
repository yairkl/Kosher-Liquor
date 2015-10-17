package com.yairkl.kosherliquorplus;

public class Category implements Identifiable {
    private final String name;
    private final int id;
    private final int fatherId;

    public Category(int id, String name, int fatherId) {
        this.name = name;
        this.id = id;
        this.fatherId = fatherId;
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
        return true;
    }

    public int getFatherId() {
        return fatherId;
    }

    boolean isEmpty() {
        return false;
    }

    public static class EmptyCategory extends Category {
        boolean isEmpty() {
            return true;
        }

        public EmptyCategory() {
            super(0, "", 0);
        }

        @Override
        public String getName() {
            return "empty Category";
        }
    }

    static final Category NULL = new EmptyCategory();
}
