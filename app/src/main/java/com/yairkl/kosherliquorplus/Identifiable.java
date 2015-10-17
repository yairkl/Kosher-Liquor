package com.yairkl.kosherliquorplus;

public interface Identifiable {
    int getId();

    String getName();

    boolean isCategory();

    Identifiable EMPTY = new Identifiable() {
        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getName() {
            return "temp";
        }

        @Override
        public boolean isCategory() {
            return false;
        }
    };
}
