package com.abhi.toyswap.activity;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.abhi.toyswap.R;
import com.abhi.toyswap.Service.MyFirebaseMessagingService;
import com.abhi.toyswap.fragments.AddItemFragment;
import com.abhi.toyswap.fragments.DashboardFragment;
import com.abhi.toyswap.fragments.FavouritesFragment;
import com.abhi.toyswap.fragments.MessagesFragment;
import com.abhi.toyswap.fragments.ProfileFragment;
import com.abhi.toyswap.utils.Constants;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;


public class DashboardActivity extends AppCompatActivity {

    BottomNavigationViewEx bottomNavigationView;
    private  TextView mtxtnotificationsbadge;
    String message;
    //to enable indicator when message recieves
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {

        public void run() {
            message=MyFirebaseMessagingService.message;
            if(message!=""){
                mtxtnotificationsbadge.setVisibility(View.VISIBLE);
            }
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        MyFirebaseMessagingService.message = "";

        setContentView(R.layout.activity_dashboard);
        bottomNavigationView = (BottomNavigationViewEx) this.findViewById(R.id.bottom_navigation);
        bottomNavigationView.enableAnimation(false);
        bottomNavigationView.enableShiftingMode(false);
        bottomNavigationView.enableItemShiftingMode(false);
        bottomNavigationView.setIconSize(28, 28);

        //add the indicator for message notification
        BottomNavigationMenuView bottomNavigationMenuView = (BottomNavigationMenuView)bottomNavigationView.getChildAt(0);
        View v=bottomNavigationMenuView.getChildAt(3);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;

        //set the icons in centre
        for (int i = 0; i < bottomNavigationMenuView.getChildCount(); i++) {
            BottomNavigationItemView item = (BottomNavigationItemView) bottomNavigationMenuView.getChildAt(i);

            //centre icon of bottom navigation
            item.setPadding(0, 20, 0, 0);

        }

        View badge = LayoutInflater.from(this)
                .inflate(R.layout.notification_badge, bottomNavigationView, false);

        itemView.addView(badge);

         mtxtnotificationsbadge = (TextView) findViewById(R.id.txtnotificationsbadge);
//        mtxtnotificationsbadge.setText("5");
        mtxtnotificationsbadge.setBackgroundResource(R.drawable.custom_circle_shape);


        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;

                        switch (item.getItemId()) {
                            case R.id.action_dashboard:
                                selectedFragment = DashboardFragment.newInstance();
                                break;
                            case R.id.action_favourite:
                                selectedFragment = FavouritesFragment.newInstance();
                                break;
                            case R.id.action_post_new_add:
                                Intent newItemIntent = new Intent(DashboardActivity.this, AddItemActivity.class);
                                newItemIntent.putExtra("IsPrimaryImage", true);
                                startActivityForResult(newItemIntent, Constants.POST_NEW_ITEM);
                                break;
                            case R.id.action_messages:
                                selectedFragment = MessagesFragment.newInstance();
                                break;
                            case R.id.action_profile:
                                selectedFragment = ProfileFragment.newInstance();

                                break;
                        }
                        if (item.getItemId() == R.id.action_dashboard || item.getItemId() == R.id.action_favourite || item.getItemId() == R.id.action_messages || item.getItemId() == R.id.action_profile) {
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            // getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            transaction.addToBackStack(null);

                            transaction.replace(R.id.frame_layout, selectedFragment);
                            transaction.commit();
                        }

                        return true;
                    }
                });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, DashboardFragment.newInstance());
        transaction.commit();

        if (this.getIntent().getBooleanExtra("IsFromNotification", false) && this.getIntent().getStringExtra("ItemId")!=null) {
            Intent chatActivityIntent = new Intent(DashboardActivity.this, ChatActivity.class);
            chatActivityIntent.putExtra("IsFromNotification", true);
            Log.i("Abhi","ItemUSerID="+this.getIntent().getStringExtra("ItemUserId"));
            chatActivityIntent.putExtra("notificationId", 0);
            chatActivityIntent.putExtra("ItemId",this.getIntent().getStringExtra("ItemId"));
            chatActivityIntent.putExtra("ItemName",this.getIntent().getStringExtra("ItemName"));
            chatActivityIntent.putExtra("ItemUserId",this.getIntent().getStringExtra("ItemUserId"));

            chatActivityIntent.putExtra("SendersUserId",this.getIntent().getStringExtra("SendersUserId"));
            chatActivityIntent.putExtra("SendersImageUrl",this.getIntent().getStringExtra("SendersImageUrl"));

            startActivity(chatActivityIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.postDelayed(runnable, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 5000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.POST_NEW_ITEM: {
                if (resultCode == RESULT_OK) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    AddItemFragment addItemFragment = AddItemFragment.newInstance(data.getStringExtra("FilePath"));
                    transaction.replace(R.id.frame_layout, addItemFragment);
                    //  transaction.addToBackStack(null);

                    transaction.commit();
                } else {
                    backPressedFromAddNewItem();
                }
                break;
            }
            case Constants.ITEM_ADDED_SUCCESSFULLY: {
                if (resultCode == RESULT_OK) {
                    Intent newItemIntent = new Intent(DashboardActivity.this, AddItemActivity.class);
                    newItemIntent.putExtra("IsPrimaryImage", true);
                    startActivityForResult(newItemIntent, Constants.POST_NEW_ITEM);
                } else {
                    backPressedFromAddNewItem();
                }
                break;
            }
        }
    }

    public void callItemAddedSuccessfullyScreen() {
        // FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // transaction.remove(AddItemFragment.newInstance(""));
        Intent newItemIntent = new Intent(DashboardActivity.this, ItemAddedSuccessfullyActivity.class);
        startActivityForResult(newItemIntent, Constants.ITEM_ADDED_SUCCESSFULLY);

    }

    public void backPressedFromAddNewItem() {
        bottomNavigationView.setCurrentItem(0);
    }

    public void backPressedFromEditItem() {
        bottomNavigationView.setCurrentItem(4);
    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }
}
