package com.rc.shoppinglist;

public class ShoppingItem {

    //region Variables
    private String itemName;
    private boolean gotItem;
    private int itemColor;
    //endregion

    public ShoppingItem(String itemName, boolean gotItem, int itemColor) {
        this.itemName = itemName;
        this.gotItem = gotItem;
        this.itemColor = itemColor;
    }

    //region Getters and Setters
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean hasGotItem() {
        return gotItem;
    }

    public void setGotItem(boolean gotItem) {
        this.gotItem = gotItem;
    }

    public int getItemColor() {
        return itemColor;
    }

    public void setItemColor(int itemColor) {
        this.itemColor = itemColor;
    }
    //endregion
}
