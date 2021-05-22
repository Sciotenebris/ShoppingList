package com.rc.shoppinglist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    //region Variables
    private Button addItemButton, backButton, confirmAddButton, changeColourButton, deleteButton,
            infoBackButton, deleteAllButton, sortButton, confirmChangesButton, editColourButton;
    private LinearLayout shoppingListLayout, addItemTab, itemInfoTab, titleBar, buttonBar;
    private EditText itemNameView, userEnterItemNameBox;
    private ArrayList<ShoppingItem> shoppingList;
    private AdView adView;
    private int colourNumber, colourCounter;
    private boolean tabActive;
    //endregion

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        Load();

        //AdMob init
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //Init adView
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        //Init views
        shoppingListLayout = findViewById(R.id.ShoppingListLayout);
        addItemTab = findViewById(R.id.AddItemTab);
        confirmAddButton = findViewById(R.id.ConfrimAddButton);
        backButton = findViewById(R.id.BackButton);
        changeColourButton = findViewById(R.id.ChangeColourButton);
        userEnterItemNameBox = findViewById(R.id.UserEnterItemNameBox);
        itemInfoTab = findViewById(R.id.ItemInfoTab);
        itemNameView = findViewById(R.id.ItemNameView);
        deleteButton = findViewById(R.id.DeleteButton);
        infoBackButton = findViewById(R.id.InfoTabBackButton);
        deleteAllButton = findViewById(R.id.DeleteAllButton);
        titleBar = findViewById(R.id.TitleBarLayout);
        sortButton = findViewById(R.id.SortButton);
        addItemButton = findViewById(R.id.AddItemButton);
        confirmChangesButton = findViewById(R.id.ConfirmChangesButton);
        editColourButton = findViewById(R.id.EditColourButton);
        buttonBar = findViewById(R.id.ButtonBar);

        tabActive = false;

        //OnClick
        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShoppingListDeleteAll();
            }
        });
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MakeAddItemTabVisible();
                userEnterItemNameBox.setText("");
            }
        });
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShoppingListSort();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetColour();
            }
        });
        confirmAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddItem();
            }
        });
        changeColourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeColour(changeColourButton);
            }
        });

        //Hides keyboard when enter is pressed
        userEnterItemNameBox.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View arg0, int arg1, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (arg1 == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(userEnterItemNameBox.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        DisplayShopping();
    }

    //region Visibility Methods
    private void MakeAddItemTabVisible() {
        shoppingListLayout.setVisibility(View.INVISIBLE);
        titleBar.setVisibility(View.INVISIBLE);
        buttonBar.setVisibility(View.INVISIBLE);
        itemInfoTab.setVisibility(View.INVISIBLE);
        addItemTab.setVisibility(View.VISIBLE);
        tabActive = true;
    }

    private void MakeEditItemTabVisible() {
        shoppingListLayout.setVisibility(View.INVISIBLE);
        titleBar.setVisibility(View.INVISIBLE);
        buttonBar.setVisibility(View.INVISIBLE);
        addItemTab.setVisibility(View.INVISIBLE);
        itemInfoTab.setVisibility(View.VISIBLE);
        tabActive = true;
    }

    private void MakeMainLayoutsVisible() {
        shoppingListLayout.setVisibility(View.VISIBLE);
        buttonBar.setVisibility(View.VISIBLE);
        titleBar.setVisibility(View.VISIBLE);
        addItemTab.setVisibility(View.INVISIBLE);
        itemInfoTab.setVisibility(View.INVISIBLE);
        tabActive = false;
    }
    //endregion

    /**
     * Saves the shopping list in Shared Preferences
     */
    private void Save() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(shoppingList);
        editor.putString("Shopping List", json);
        editor.apply();
    }

    /**
     * Loads the saved shopping list or creates a new shopping list if no data stored
     */
    private void Load() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared prefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Shopping List", null);
        Type type = new TypeToken<ArrayList<ShoppingItem>>() {
        }.getType();
        shoppingList = gson.fromJson(json, type);
        if (shoppingList == null) {
            shoppingList = new ArrayList<>();
        }
    }

    /**
     * Iterates through the shopping list and checks to see if the item has been checked, if it has
     * been checked it is then deleted
     */
    private void ShoppingListDeleteAll() {
        Iterator<ShoppingItem> itr = shoppingList.iterator();
        while (itr.hasNext()) {
            ShoppingItem item = itr.next();
            if (item.hasGotItem()) {
                itr.remove();
            }
        }
        Toast.makeText(getApplicationContext(), "All checked items deleted", Toast.LENGTH_SHORT).show();
        Save();
        DisplayShopping();
    }

    /**
     * Sorts through the shopping list: First by alphabetical order, secondly by colour, and finally
     * by the gotItem boolean
     */
    private void ShoppingListSort() {
        //Sorts into alphabetical order
        Collections.sort(shoppingList,
                new Comparator<ShoppingItem>() {
                    @Override
                    public int compare(ShoppingItem s1, ShoppingItem s2) {
                        return s1.getItemName().compareToIgnoreCase(s2.getItemName());
                    }
                });
        //Sorts through colour order
        Collections.sort(shoppingList, new Comparator<ShoppingItem>() {
            public int compare(ShoppingItem item1, ShoppingItem item2) {
                return item2.getItemColor() - item1.getItemColor();
            }
        });
        //Further sorts through whether the item has been marked as purchased
        Collections.sort(shoppingList, new Comparator<ShoppingItem>() {
            public int compare(ShoppingItem item1, ShoppingItem item2) {
                return Boolean.compare(item1.hasGotItem(), item2.hasGotItem());
            }
        });
        Save();
        DisplayShopping();
    }

    /**
     * Resets the button colour for the addItem tab
     */
    private void ResetColour() {
        MakeMainLayoutsVisible();
        colourCounter = 0;
        changeColourButton.setBackgroundColor(getResources().getColor(R.color.green));
    }

    /**
     * Changes the colourNumber each time it is called and resets it to 0 after 6. The colourNumber
     * can then be used to assign colour to buttons and shoppingList items
     */
    private void ChangeColour(Button button) {
        colourCounter++;
        if (colourCounter == 1) {
            colourNumber = getResources().getColor(R.color.red);
        } else if (colourCounter == 2) {
            colourNumber = getResources().getColor(R.color.blue);
        } else if (colourCounter == 3) {
            colourNumber = getResources().getColor(R.color.yellow);
        } else if (colourCounter == 4) {
            colourNumber = getResources().getColor(R.color.orange);
        } else if (colourCounter == 5) {
            colourNumber = getResources().getColor(R.color.purple);
        } else if (colourCounter == 6) {
            colourNumber = getResources().getColor(R.color.pink);
        } else {
            colourNumber = getResources().getColor(R.color.green);
            colourCounter = 0;
        }
        button.setBackgroundColor(colourNumber);
    }

    /**
     * Creates buttons for each ShoppingItem held in the shoppingList arraylist and controls the
     * onClick events for each. The editInfoTab is managed within here - there is probably a far
     * better way to do this but currently it works.
     */
    private void DisplayShopping() {
        shoppingListLayout.removeAllViews();
        CreateButton();

    }

    private void CreateButton() {
        for (int i = 0; i < shoppingList.size(); i++) {
            final Button itemButton = new Button(shoppingListLayout.getContext());
            shoppingListLayout.addView(itemButton);
            itemButton.setText(shoppingList.get(i).getItemName());
            itemButton.setAllCaps(false);
            itemButton.setTextSize(20);
            itemButton.setTextColor(Color.BLACK);

            if (shoppingList.get(i).hasGotItem()) {
                itemButton.setBackgroundColor(Color.GRAY);
                Drawable draw = ContextCompat.getDrawable(this, R.drawable.got_item);
                itemButton.setForeground(draw);
                itemButton.setTextColor(Color.LTGRAY);
                itemButton.setShadowLayer(1, 1, 1, Color.BLACK);

            } else {
                itemButton.setBackgroundColor(shoppingList.get(i).getItemColor());
                Drawable draw = ContextCompat.getDrawable(this, R.drawable.shop_item);
                itemButton.setForeground(draw);
                itemButton.setTextColor(Color.BLACK);
            }
            itemButton.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            final int finalI = i;

            itemButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ButtonLongPress(itemButton, finalI);
                    return true;
                }
            });

            itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ButtonShortPress(itemButton, finalI);
                }
            });
        }

    }

    public void ButtonShortPress(final Button button, final int i) {

        if (shoppingList.get(i).hasGotItem()) {
            shoppingList.get(i).setGotItem(false);
            button.setBackgroundColor(shoppingList.get(i).getItemColor());
            button.setTextColor(Color.BLACK);
            button.setForeground(ContextCompat.getDrawable(button.getContext(), R.drawable.shop_item));
        } else {
            shoppingList.get(i).setGotItem(true);
            button.setBackgroundColor(Color.GRAY);
            button.setForeground(ContextCompat.getDrawable(button.getContext(), R.drawable.got_item));
            button.setTextColor(Color.LTGRAY);
            button.setShadowLayer(1, 1, 1, Color.BLACK);
        }
        Save();
        DisplayShopping();
    }

    public void ButtonLongPress(Button button, final int i) {

        MakeEditItemTabVisible();
        itemNameView.setText(shoppingList.get(i).getItemName());
        editColourButton.setBackgroundColor(shoppingList.get(i).getItemColor());
        colourNumber = shoppingList.get(i).getItemColor();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), itemNameView.getText().toString() + " deleted", Toast.LENGTH_SHORT).show();
                itemNameView.setText("");
                shoppingList.remove(i);
                MakeMainLayoutsVisible();
                Save();
                DisplayShopping();
            }
        });

        infoBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MakeMainLayoutsVisible();
            }
        });

        editColourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeColour(editColourButton);
            }
        });

        //Hides keyboard when enter is pressed
        itemNameView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View arg0, int arg1, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (arg1 == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(itemNameView.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        confirmChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MakeMainLayoutsVisible();

                if (itemNameView.getText().toString().equals("")) {
                    shoppingList.get(i).setItemName("Item");
                } else {
                    shoppingList.get(i).setItemName(itemNameView.getText().toString());
                }
                shoppingList.get(i).setItemColor(colourNumber);
                Save();
                DisplayShopping();
            }
        });

    }

    /**
     * Creates a new ShoppingItem and adds it to the shoppingList arrayList.
     */
    private void AddItem() {

        String newItemName;

        int itemColor = Color.TRANSPARENT;
        Drawable background = changeColourButton.getBackground();

        if (background instanceof ColorDrawable)
            itemColor = ((ColorDrawable) background).getColor();

        if (userEnterItemNameBox.getText().toString().equals("")) {
            newItemName = "Item";
        } else {
            newItemName = userEnterItemNameBox.getText().toString();
        }

        shoppingList.add(new ShoppingItem(newItemName, false, itemColor));
        Save();
        DisplayShopping();
        ResetColour();
        MakeMainLayoutsVisible();
    }

    //region OverrideMethods
    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (tabActive) {
            MakeMainLayoutsVisible();
        } else {
            Save();
            if (adView != null) {
                adView.destroy();
            }
            super.onBackPressed();
        }
    }
    //endregion
}