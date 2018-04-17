package com.abhi.toyswap.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.Models.ProductDetails.ProductItem;
import com.abhi.toyswap.R;
import com.abhi.toyswap.adapters.ItemDetailsPhotoAdapter;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.Utils;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MyItemDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyItemDetailsFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "ItemDetails";
    private static final String ARG_PARAM2 = "IsAlreadySold";
    private ImageView favouriteImageView;

    private ViewPager itemPhotosViewPager;
    private ImageView[] dotImageViews;
    private LinearLayout dotsContainerLayout;
    private ProgressDialog objProgressDialog;
    ExecutorService executor = Executors.newFixedThreadPool(4);
    private ProductItem mProductItem;
    private ImageLoader imageLoader;
    private boolean isAlreadySold=false;
    private boolean isImageFitToScreen = false;
    private ItemDetailsPhotoAdapter photosViewPagerAdapter;

    public MyItemDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ItemDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyItemDetailsFragment newInstance(ProductItem item,boolean isAlreadySold) {
        MyItemDetailsFragment fragment = new MyItemDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, item);
        args.putBoolean("IsAlreadySold",isAlreadySold);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLoader = new ImageLoader(getContext());

        if (getArguments() != null) {
            try {
                mProductItem = getArguments().getParcelable(ARG_PARAM1);
                isAlreadySold=getArguments().getBoolean("IsAlreadySold");
            } catch (ClassCastException e) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_item_details, container, false);

        TextView toolBarTitle = (TextView) view.findViewById(R.id.toolbar_title);
        favouriteImageView = (ImageView) view.findViewById(R.id.imageview_my_item_details_favourite);
        ImageView backImageView=(ImageView)view.findViewById(R.id.imageview_my_item_details_back);
        Button markAsSoldButton = (Button) view.findViewById(R.id.button_my_item_details_mark_as_sold);
        Button deleteButton = (Button) view.findViewById(R.id.button_my_items_details_delete);
        ImageView editItemImageView = (ImageView) view.findViewById(R.id.image_my_item_details_edit);
        itemPhotosViewPager=(ViewPager)view.findViewById(R.id.viewpager_my_item_details_photos);
        dotsContainerLayout=(LinearLayout)view.findViewById(R.id.linear_my_item_details_dots_container);
        TextView priceTextView = (TextView) view.findViewById(R.id.text_my_item_details_price);
        ImageView buyImageView = (ImageView) view.findViewById(R.id.imageview_my_item_details_buy);
        ImageView swapImageView = (ImageView) view.findViewById(R.id.imageview_my_item_details_swap);
        ImageView bidImageView = (ImageView) view.findViewById(R.id.imageview_my_item_details_bid);
        TextView timeTextView = (TextView) view.findViewById(R.id.text_my_item_details_time);
        ImageView userPhotoImageView = (ImageView) view.findViewById(R.id.image_my_item_details_user_photo);
        TextView postedByTextView = (TextView) view.findViewById(R.id.text_my_item_details_postedby);
        TextView locationTextView = (TextView) view.findViewById(R.id.text_my_item_details_location);
        final RelativeLayout visibilityAdjustableRelativeLayout = (RelativeLayout) view.findViewById(R.id.relative_my_item_details_visibility_adjustable_panel);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.text_my_item_details_description);
        final Button showHideButton = (Button) view.findViewById(R.id.button_my_item_details_show_or_hide_more);

        imageLoader.DisplayImage(mProductItem.getUserImage(), userPhotoImageView, false);
        photosViewPagerAdapter=new ItemDetailsPhotoAdapter(getContext(),mProductItem.getProductImageUrl());
        itemPhotosViewPager.setAdapter(photosViewPagerAdapter);

        dotImageViews = new ImageView[mProductItem.getProductImageUrl().size()];

        if(mProductItem.getProductImageUrl().size()>1) {
            for (int index = 0; index < mProductItem.getProductImageUrl().size(); index++) {
                dotImageViews[index] = new ImageView(getContext());
                dotImageViews[index].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.inactive_dot));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(8, 0, 8, 0);
                dotsContainerLayout.addView(dotImageViews[index], params);
            }

            dotImageViews[0].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot));
        }

        itemPhotosViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                for(int index = 0; index< mProductItem.getProductImageUrl().size(); index++){
                    dotImageViews[index].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.inactive_dot));
                }
                dotImageViews[position].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot));
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        favouriteImageView.setImageResource(mProductItem.isFavourite() ? R.drawable.icon_favourite : R.drawable.icon_unfavourite);
        favouriteImageView.setTag(mProductItem.isFavourite());
       /* itemImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isImageFitToScreen) {
                    isImageFitToScreen=false;
                    itemImageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int)getActivity().getResources().getDimension(R.dimen.space_240)));
                    itemImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }else{
                    isImageFitToScreen=true;
                    itemImageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                    itemImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }
        });*/


        buyImageView.setImageResource(mProductItem.isAvailableForBuy() ? R.drawable.icon_buy_enabled : R.drawable.icon_buy_disabled);
        swapImageView.setImageResource(mProductItem.isAvailableForSwap() ? R.drawable.icon_swap_enabled:R.drawable.icon_swap_disabled);
        bidImageView.setImageResource(mProductItem.isAvailableForBid() ? R.drawable.icon_bid_enabled:R.drawable.icon_bid_disabled);

        favouriteImageView.setOnClickListener(this);
        backImageView.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        markAsSoldButton.setOnClickListener(this);
        editItemImageView.setOnClickListener(this);
        toolBarTitle.setText(mProductItem.getProductName());
        priceTextView.setText("$ " + mProductItem.getPrice());
        timeTextView.setText(mProductItem.getPostedTime());
        postedByTextView.setText(mProductItem.getUserName());
        if (mProductItem.getItemAddress().trim().isEmpty()) {
            locationTextView.setVisibility(View.GONE);
        } else {
            locationTextView.setText(mProductItem.getItemAddress());
        }
        if(!mProductItem.getDescription().trim().isEmpty()) {
            descriptionTextView.setText(mProductItem.getDescription());
        }
        showHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibilityAdjustableRelativeLayout.getVisibility() == View.VISIBLE) {
                    visibilityAdjustableRelativeLayout.setVisibility(View.GONE);
                    showHideButton.setText(getString(R.string.more_details));
                } else {
                    visibilityAdjustableRelativeLayout.setVisibility(View.VISIBLE);
                    showHideButton.setText(getString(R.string.less_details));

                }
            }
        });
        if(isAlreadySold){
            markAsSoldButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            editItemImageView.setVisibility(View.GONE);
        }

        return view;

    }

    @Override
    public void onResume() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        getTargetFragment().onActivityResult(
                                getTargetRequestCode(),
                                Constants.BACK_DETAILS_FRAGMENT,
                                new Intent().putExtra("ProductItem", mProductItem)
                        );
                    }
                }
                return false;
            }
        });
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.imageview_my_item_details_favourite: {
                FavouriteUnfavouriteThread favouriteUnfavouriteThread = new FavouriteUnfavouriteThread();
                executor.execute(favouriteUnfavouriteThread);
                break;
            }case R.id.imageview_my_item_details_back:{
                getFragmentManager().popBackStack();
                executor.shutdownNow();
                break;
            }
            case R.id.button_my_item_details_mark_as_sold: {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to mark this item as Sold?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                SoldOrDeleteItemThread soldItemThread = new SoldOrDeleteItemThread("setItemsold");
                                executor.execute(soldItemThread);                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();

                break;
            } case R.id.button_my_items_details_delete: {

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to delete this Item?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                SoldOrDeleteItemThread deleteItemThread = new SoldOrDeleteItemThread("deleteItem");
                                executor.execute(deleteItemThread);                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();

                break;
            }

            case R.id.image_my_item_details_edit: {
                EditItemFragment fragment2 = EditItemFragment.newInstance(mProductItem);
                Bundle itemDetailsBundle = new Bundle();
                itemDetailsBundle.putParcelable("ProductItem", mProductItem);
                fragment2.setArguments(itemDetailsBundle);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragment2.setTargetFragment(MyItemDetailsFragment.this, Constants.BACK_DETAILS_FRAGMENT);
                fragmentTransaction.replace(R.id.frame_layout, fragment2);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            }

        }
    }

    public class FavouriteUnfavouriteThread implements Runnable {
        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();

            try {
                fetchItemsJson.put("task", "setItemlike");
                fetchItemsJson.put("user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                fetchItemsJson.put("item_id", mProductItem.getProductId());
                fetchItemsJson.put("is_like", Boolean.valueOf(favouriteImageView.getTag().toString()) ? "0" : "1");

                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.GET_ITEMS, fetchItemsJson);
                JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Item Liked")) {
                    mProductItem.setFavourite(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            favouriteImageView.setImageResource(R.drawable.icon_favourite);
                            favouriteImageView.setTag(true);
                        }
                    });
                } else if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Item Unliked")) {
                    mProductItem.setFavourite(false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            favouriteImageView.setImageResource(R.drawable.icon_unfavourite);
                            favouriteImageView.setTag(false);
                        }
                    });
                }
            } catch (Exception e) {
                Utils.log(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public class SoldOrDeleteItemThread implements Runnable {

        private String task;
        public SoldOrDeleteItemThread(String task){
            this.task=task;
        }
        @Override
        public void run() {
            JSONObject deleteorSoldItemJSon = new JSONObject();

            try {
                deleteorSoldItemJSon.put("task", task);
                deleteorSoldItemJSon.put("user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                deleteorSoldItemJSon.put("item_id", mProductItem.getProductId());

                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.DELETE_OR_SOLD_ITEM, deleteorSoldItemJSon);
                JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        && (jsonObj.getString("status_message").equalsIgnoreCase("Item sold")||jsonObj.getString("status_message").equalsIgnoreCase("Item Deleted"))) {
                  if(jsonObj.getString("status_message").equalsIgnoreCase("Item sold")){
                      mProductItem.setIsAlreadySold(true);
                  }
                    getTargetFragment().onActivityResult(
                            getTargetRequestCode(),
                            Constants.ITEM_DELETE_SOLD,
                            new Intent().putExtra("ProductItem", mProductItem)
                    );
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            getActivity().getSupportFragmentManager().popBackStack();
                            executor.shutdownNow();

                        }
                    });


                }
            } catch (Exception e) {
                Utils.log(e.getMessage());
                e.printStackTrace();
            }
        }
    }

}