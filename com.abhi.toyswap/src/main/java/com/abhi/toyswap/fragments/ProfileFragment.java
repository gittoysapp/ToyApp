package com.abhi.toyswap.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.Models.ProductDetails.ItemImage;
import com.abhi.toyswap.Models.ProductDetails.ProductItem;
import com.abhi.toyswap.R;
import com.abhi.toyswap.activity.LoginActivity;
import com.abhi.toyswap.adapters.ProductContainerGridViewAdapterForMyItems;
import com.abhi.toyswap.interfaces.OnItemClickInterface;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.GPSTracker;
import com.abhi.toyswap.utils.ItemDecorationAlbumColumns;
import com.abhi.toyswap.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ProfileFragment extends Fragment implements View.OnClickListener, OnItemClickInterface {

    private LinearLayout noRecordsFoundLayout;
    private RecyclerView productsContainerRecylerView;
    private GridLayoutManager gridLayoutManager;
    private ProductContainerGridViewAdapterForMyItems objProductsGridViewContainerAdapter;
    private ProgressDialog objProgressDialog;
    private RelativeLayout likesLayout;
    private RelativeLayout dislikesLayout;
    private TextView likesCountTextView;
    private TextView dislikesCountTextView;
    private TextView usersNameTextView;
    private ImageView editProfileImageView;
    private ImageView usersProfileImageView;
    private ImageView showItemsInGridImageView;
    private ImageView showItemsInListImageView;
    private ImageView logoutImageView;
    TextView usersLocationTextView;
    private GPSTracker objGpsTracker;
    private List<ProductItem> productsList;
    private int type = R.id.image_dashboard_gridview;
    ExecutorService executor = Executors.newFixedThreadPool(4);
    private View view;
    private ImageLoader imageLoader;
    private LinearLayoutManager linearLayoutManager;
    // Tab titles
    private TextView itemTypeAll;
    private TextView itemTypeSold;

    private TextView itemTypeSelling;
    private TextView itemTypeBidding;
    private TextView itemTypeSwapping;
    private View previousSelectedItemType;
    private String selectedItemType = "";

    private List<String> allItemsList = new ArrayList<String>();

    private List<String> itemsForSaleList = new ArrayList<String>();
    private List<String> itemsForBidList = new ArrayList<String>();
    private List<String> itemsForSwapList = new ArrayList<String>();
    private List<String> alreadySoldItemsList = new ArrayList<String>();

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLoader = new ImageLoader(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {

            view = inflater.inflate(R.layout.fragment_profile, container, false);

            likesLayout = (RelativeLayout) view.findViewById(R.id.relative_profile_likes_layout);
            dislikesLayout = (RelativeLayout) view.findViewById(R.id.relative_profile_dislikes_layout);
            likesCountTextView = (TextView) view.findViewById(R.id.text_profile_number_of_likes);
            dislikesCountTextView = (TextView) view.findViewById(R.id.text_profile_number_of_dislikes);
            usersNameTextView = (TextView) view.findViewById(R.id.text_profile_username);
            editProfileImageView = (ImageView) view.findViewById(R.id.image_profile_edit_profile);
            usersProfileImageView = (ImageView) view.findViewById(R.id.image_profile_user_photo);
            showItemsInGridImageView = (ImageView) view.findViewById(R.id.image_profile_gridview);
            showItemsInListImageView = (ImageView) view.findViewById(R.id.image_profile_listview);
            logoutImageView = (ImageView) view.findViewById(R.id.image_profile_logout);
            usersLocationTextView = (TextView) view.findViewById(R.id.text_profile_location);
            itemTypeAll = (TextView) view.findViewById(R.id.text_profile_item_type_all);
            itemTypeSold = (TextView) view.findViewById(R.id.text_profile_item_type_sold);
            itemTypeSelling = (TextView) view.findViewById(R.id.text_profile_item_type_selling);
            itemTypeBidding = (TextView) view.findViewById(R.id.text_profile_item_type_bidding);
            itemTypeSwapping = (TextView) view.findViewById(R.id.text_profile_item_type_swapping);
            previousSelectedItemType = itemTypeAll;
            noRecordsFoundLayout = (LinearLayout) view.findViewById(R.id.linear_profile_no_items_available);
            productsContainerRecylerView = (RecyclerView) view.findViewById(R.id.recylerview_profile_items_container);
            usersNameTextView.setText(Utils.getDataFromSharedPreferences(getActivity(), "Name"));
            usersLocationTextView.setVisibility(View.GONE);
            imageLoader.DisplayImage(Utils.getDataFromSharedPreferences(getActivity(), "UserImage"), usersProfileImageView, true);
            linearLayoutManager = new LinearLayoutManager(getContext());

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);

            productsContainerRecylerView.addItemDecoration(dividerItemDecoration);

            productsContainerRecylerView.addItemDecoration(new ItemDecorationAlbumColumns(
                    getResources().getDimensionPixelSize(R.dimen.space_0), 2));

            gridLayoutManager = new GridLayoutManager(getContext(), 2);
            objGpsTracker = GPSTracker.getInstance(getContext(), getActivity());
            logoutImageView.setOnClickListener(this);
            likesLayout.setOnClickListener(this);
            dislikesLayout.setOnClickListener(this);
            editProfileImageView.setOnClickListener(this);
            showItemsInGridImageView.setOnClickListener(this);
            showItemsInListImageView.setOnClickListener(this);
            itemTypeAll.setOnClickListener(this);
            itemTypeSold.setOnClickListener(this);
            itemTypeSelling.setOnClickListener(this);
            itemTypeBidding.setOnClickListener(this);
            itemTypeSwapping.setOnClickListener(this);
            GetLikesDislikesThread getLikesDislikesThread = new GetLikesDislikesThread();
            executor.execute(getLikesDislikesThread);
            new getItemsTask(true).execute();


        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            new getItemsTask(true).execute();
        } else if (resultCode == Constants.ITEM_DELETE_SOLD) {

            ProductItem item = data.getParcelableExtra("ProductItem");
            itemsForSaleList.remove(item.getProductId() + item.getProductName());
            itemsForBidList.remove(item.getProductId() + item.getProductName());
            itemsForSwapList.remove(item.getProductId() + item.getProductName());


            if (item.getIsAlreadySold()) {
                if (!selectedItemType.trim().isEmpty()) {
                    productsList.remove(item);
                    if (productsList.size() == 0) {
                        noRecordsFoundLayout.setVisibility(View.VISIBLE);
                    }
                    objProductsGridViewContainerAdapter.notifyDataSetChanged();
                }
                alreadySoldItemsList.add(item.getProductId() + item.getProductName());
            } else {
                productsList.remove(item);
                if (productsList.size() == 0) {
                    noRecordsFoundLayout.setVisibility(View.VISIBLE);
                }
                objProductsGridViewContainerAdapter.notifyDataSetChanged();
                allItemsList.remove(item.getProductId() + item.getProductName());
            }
            itemTypeAll.setText(allItemsList.size() != 0 ? getString(R.string.all) + "(" + allItemsList.size() + ")" : getString(R.string.all));
            itemTypeSold.setText(alreadySoldItemsList.size() != 0 ? getString(R.string.sold) + "(" + alreadySoldItemsList.size() + ")" : getString(R.string.sold));

            itemTypeBidding.setText(itemsForBidList.size() != 0 ? getString(R.string.bidding) + "(" + itemsForBidList.size() + ")" : getString(R.string.bidding));
            itemTypeSelling.setText(itemsForSaleList.size() != 0 ? getString(R.string.selling) + "(" + itemsForSaleList.size() + ")" : getString(R.string.selling));
            itemTypeSwapping.setText(itemsForSwapList.size() != 0 ? getString(R.string.swapping) + "(" + itemsForBidList.size() + ")" : getString(R.string.swapping));


        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.relative_profile_likes_layout: {

                break;
            }
            case R.id.image_profile_logout: {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to Logout from the application?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                if(!Utils.getBooleanDataFromSharedPreferences(getActivity(),"IsRememberMe")){
                                    Utils.deleteDataFromSharedPreferences(getActivity(), "EmailAddress");
                                    Utils.deleteDataFromSharedPreferences(getActivity(), "Password");
                                    Utils.deleteDataFromSharedPreferences(getActivity(), "IsRememberMe");
                                }
                                Utils.deleteDataFromSharedPreferences(getActivity(), "IsUserLoggedIn");
                                Utils.deleteDataFromSharedPreferences(getActivity(), "UserId");
                                Utils.deleteDataFromSharedPreferences(getActivity(), "Name");
                                Utils.deleteDataFromSharedPreferences(getActivity(), "MemberType");
                                Utils.deleteDataFromSharedPreferences(getActivity(), "UserImage");

                                Intent signOutIntent = new Intent(getActivity(), LoginActivity.class);
                                signOutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                startActivity(signOutIntent);
                                getActivity().finish();
                            }
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
            case R.id.image_profile_edit_profile: {

                UserDetailsFragment fragment2 = UserDetailsFragment.newInstance();

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragment2.setTargetFragment(ProfileFragment.this, Constants.BACK_DETAILS_FRAGMENT);
                fragmentTransaction.replace(R.id.frame_layout, fragment2);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            }
            case R.id.relative_profile_dislikes_layout: {

                break;
            }
            case R.id.text_profile_item_type_all: {
                selectedItemType = "";
                new getItemsTask(false).execute();
                itemTypeAll.setTypeface(itemTypeAll.getTypeface(), Typeface.BOLD);
                itemTypeAll.setTextSize(14);
                itemTypeAll.setTextColor(getResources().getColor(R.color.white));
                ((TextView) previousSelectedItemType).setTypeface(null, Typeface.NORMAL);
                ((TextView) previousSelectedItemType).setTextSize(12);
                ((TextView) previousSelectedItemType).setTextColor(getResources().getColor(R.color.white_grey));
                previousSelectedItemType = itemTypeAll;
                break;
            }
            case R.id.text_profile_item_type_sold: {
                selectedItemType = "4";
                new getItemsTask(false).execute();

                itemTypeSold.setTextSize(14);
                itemTypeSold.setTypeface(itemTypeSold.getTypeface(), Typeface.BOLD);
                itemTypeSold.setTextColor(getResources().getColor(R.color.white));

                ((TextView) previousSelectedItemType).setTypeface(null, Typeface.NORMAL);
                ((TextView) previousSelectedItemType).setTextSize(12);
                ((TextView) previousSelectedItemType).setTextColor(getResources().getColor(R.color.white_grey));
                previousSelectedItemType = itemTypeSold;
                break;
            }
            case R.id.text_profile_item_type_selling: {
                selectedItemType = "1";
                new getItemsTask(false).execute();

                itemTypeSelling.setTextSize(13);
                itemTypeSelling.setTypeface(itemTypeSelling.getTypeface(), Typeface.BOLD);
                itemTypeSelling.setTextColor(getResources().getColor(R.color.white));

                ((TextView) previousSelectedItemType).setTypeface(null, Typeface.NORMAL);
                ((TextView) previousSelectedItemType).setTextSize(12);
                ((TextView) previousSelectedItemType).setTextColor(getResources().getColor(R.color.white_grey));
                previousSelectedItemType = itemTypeSelling;
                break;
            }
            case R.id.text_profile_item_type_bidding: {
                selectedItemType = "3";
                new getItemsTask(false).execute();

                itemTypeBidding.setTextSize(13);
                itemTypeBidding.setTypeface(itemTypeBidding.getTypeface(), Typeface.BOLD);
                itemTypeBidding.setTextColor(getResources().getColor(R.color.white));

                ((TextView) previousSelectedItemType).setTypeface(null, Typeface.NORMAL);
                ((TextView) previousSelectedItemType).setTextSize(12);
                ((TextView) previousSelectedItemType).setTextColor(getResources().getColor(R.color.white_grey));
                previousSelectedItemType = itemTypeBidding;
                break;
            }
            case R.id.text_profile_item_type_swapping: {
                selectedItemType = "2";
                new getItemsTask(false).execute();

                itemTypeSwapping.setTextSize(13);
                itemTypeSwapping.setTypeface(itemTypeSwapping.getTypeface(), Typeface.BOLD);
                itemTypeSwapping.setTextColor(getResources().getColor(R.color.white));

                ((TextView) previousSelectedItemType).setTypeface(null, Typeface.NORMAL);
                ((TextView) previousSelectedItemType).setTextSize(12);
                ((TextView) previousSelectedItemType).setTextColor(getResources().getColor(R.color.white_grey));
                previousSelectedItemType = itemTypeSwapping;
                break;
            }

            case R.id.image_profile_gridview: {
                if(objProductsGridViewContainerAdapter!=null) {
                    type = R.id.image_profile_gridview;
                    objProductsGridViewContainerAdapter.changeViewType(R.id.image_profile_gridview);
                    productsContainerRecylerView.setLayoutManager(gridLayoutManager);
                    objProductsGridViewContainerAdapter.notifyDataSetChanged();
                }
                break;
            }
            case R.id.image_profile_listview: {
                if(objProductsGridViewContainerAdapter!=null) {
                    type = R.id.image_profile_listview;
                    objProductsGridViewContainerAdapter.changeViewType(R.id.image_profile_listview);
                    productsContainerRecylerView.setLayoutManager(linearLayoutManager);
                    objProductsGridViewContainerAdapter.notifyDataSetChanged();
                }
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        usersNameTextView.setText(Utils.getDataFromSharedPreferences(getActivity(), "Name"));
        imageLoader.DisplayImage(Utils.getDataFromSharedPreferences(getActivity(), "UserImage"), usersProfileImageView, true);
    }

    public class GetLikesDislikesThread implements Runnable {

        public GetLikesDislikesThread() {

        }

        @Override
        public void run() {
            JSONObject fetchLikesDislikesJson = new JSONObject();

            try {
                fetchLikesDislikesJson.put("task", "getUserlikes");
                fetchLikesDislikesJson.put("user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.GET_LIKES_DISLIKES, fetchLikesDislikesJson);

                final JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("User exists"))
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                likesCountTextView.setText(String.valueOf(jsonObj.getJSONObject("data").getInt("user_likes")));
                                dislikesCountTextView.setText(String.valueOf(jsonObj.getJSONObject("data").getInt("user_dislikes")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
            } catch (Exception e) {
                Utils.log(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    class getItemsTask extends AsyncTask<Void, Void, String> {
        boolean isHeaderToRefresh;

        public getItemsTask(boolean isHeaderToRefresh) {
            this.isHeaderToRefresh = isHeaderToRefresh;
        }

        @Override
        protected void onPreExecute() {
            objProgressDialog = new ProgressDialog(getContext());
            objProgressDialog.setMessage("Please wait..");
            objProgressDialog.setCanceledOnTouchOutside(false);
            objProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject fetchItemsJson = new JSONObject();

            try {
                fetchItemsJson.put("task", "getAllItems");
                fetchItemsJson.put("category_id", "");
                fetchItemsJson.put("recent_date", "0");
                fetchItemsJson.put("distance", "");
                fetchItemsJson.put("usertimeZone", TimeZone.getDefault().getID());
                fetchItemsJson.put("popular_item", "0");
                fetchItemsJson.put("following_item", "0");
                // fetchItemsJson.put("user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                fetchItemsJson.put("item_id", "");
                fetchItemsJson.put("itemType_id", "");
                fetchItemsJson.put("price_lower", "");
                fetchItemsJson.put("price_upper", "");
                fetchItemsJson.put("date_limit", "");
                fetchItemsJson.put("age_group_id", "");
                fetchItemsJson.put("search_keyword", "");
                fetchItemsJson.put("lattitude", "");
                fetchItemsJson.put("longitude", "");
                if (selectedItemType.equals("4")) {
                    fetchItemsJson.put("mysold_items", "1");

                } else {
                    fetchItemsJson.put("myItemtype_id", selectedItemType);

                }
                fetchItemsJson.put("myuser_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));


            } catch (Exception e) {
                e.printStackTrace();
                Utils.log(e.getMessage());
            }

            Connection objConnection = new Connection();
            String response = objConnection.getResponseFromWebservice(Constants.GET_ITEMS, fetchItemsJson);
            Log.i("Abhi", "Response=" + response);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                ProductItem item;
                JSONObject itemJsonObject;
                JSONArray itemsJsonArray;
                JSONArray itemTypesJsonArray;

                JSONArray itemImagesJsonArray;
                List<ItemImage> itemImagesList;
                ItemImage itemImage;

                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Record Found")) {
                    noRecordsFoundLayout.setVisibility(View.GONE);

                    if (isHeaderToRefresh) {
                        allItemsList.clear();
                        itemsForSwapList.clear();
                        itemsForBidList.clear();
                        itemsForSaleList.clear();
                        alreadySoldItemsList.clear();
                    }
                    if (productsList != null) {
                        productsList.clear();
                    } else {
                        productsList = new ArrayList<ProductItem>();
                    }
                    itemsJsonArray = jsonObj.getJSONArray("data");
                    for (int index = 0; index < itemsJsonArray.length(); index++) {
                        item = new ProductItem();

                        itemJsonObject = itemsJsonArray.getJSONObject(index);
                        item.setProductName(itemJsonObject.getString("item_name"));
                        item.setProductId(itemJsonObject.getString("item_id"));
                        item.setAgeGroup(itemJsonObject.getString("age_group"));
                        item.setCategoryId(itemJsonObject.getString("category_id"));
                        item.setCategoryName(itemJsonObject.getString("category_name"));
                        item.setDescription(itemJsonObject.getString("item_description"));
                        item.setItemAddress(itemJsonObject.getString("item_address"));
                        item.setUserId(itemJsonObject.getString("user_id"));
                        item.setUserImage(itemJsonObject.getString("user_image"));
                        item.setItemUniqueKey(itemJsonObject.getString("image_unique_key"));
                        item.setUserName(itemJsonObject.getString("user_name"));
                        item.setPostedTime(itemJsonObject.getString("postedtime"));
                        item.setNumberOfLikes(itemJsonObject.getInt("user_likes"));
                        item.setNumberOfDislikes(itemJsonObject.getInt("user_dislikes"));
                        item.setIsCustom(itemJsonObject.getString("is_custom"));
                        item.setFavourite(itemJsonObject.getInt("item_islike") == 1 ? true : false);
                        item.setFollowed(itemJsonObject.getInt("item_isfollow") == 1 ? true : false);

                        if (itemJsonObject.getString("item_status").equals("sold")) {
                            if (!alreadySoldItemsList.contains(item.getProductId() + item.getProductName())) {
                                alreadySoldItemsList.add(item.getProductId() + item.getProductName());
                            }
                            item.setIsAlreadySold(true);
                        }
                        itemTypesJsonArray = itemJsonObject.getJSONArray("item_type");
                        for (int index2 = 0; index2 < itemTypesJsonArray.length(); index2++) {
                            if (itemTypesJsonArray.getJSONObject(index2).getString("name").equalsIgnoreCase("sell")) {
                                item.setAvailableForBuy(true);
                                item.setPrice(itemTypesJsonArray.getJSONObject(index2).getString("price"));
                                if (!itemJsonObject.getString("item_status").equals("sold")) {
                                    if (!itemsForSaleList.contains(item.getProductId() + item.getProductName())) {
                                        itemsForSaleList.add(item.getProductId() + item.getProductName());
                                    }
                                }
                            }
                            if (itemTypesJsonArray.getJSONObject(index2).getString("name").equalsIgnoreCase("swap")) {
                                item.setAvailableForSwap(true);
                                item.setPrice(itemTypesJsonArray.getJSONObject(index2).getString("price"));
                                if (!itemJsonObject.getString("item_status").equals("sold")) {
                                    if (!itemsForSwapList.contains(item.getProductId() + item.getProductName())) {
                                        itemsForSwapList.add(item.getProductId() + item.getProductName());
                                    }
                                }
                            }
                            if (itemTypesJsonArray.getJSONObject(index2).getString("name").equalsIgnoreCase("bid")) {
                                item.setAvailableForBid(true);
                                item.setPrice(itemTypesJsonArray.getJSONObject(index2).getString("price"));
                                if (!itemJsonObject.getString("item_status").equals("sold")) {
                                    if (!itemsForBidList.contains(item.getProductId() + item.getProductName())) {
                                        itemsForBidList.add(item.getProductId() + item.getProductName());
                                    }
                                }
                            }
                        }


                        itemImagesJsonArray = itemJsonObject.getJSONArray("item_image");
                        itemImagesList = new ArrayList<>();
                        for (int index2 = 0; index2 < itemImagesJsonArray.length(); index2++) {
                            itemImage = new ItemImage();
                            itemImage.setImageId(itemImagesJsonArray.getJSONObject(index2).getString("image_id"));
                            itemImage.setImageUrl(itemImagesJsonArray.getJSONObject(index2).getString("image_name"));
                            itemImagesList.add(itemImage);
                        }
                        item.setProductImageUrl(itemImagesList);
                        if (!allItemsList.contains(item.getProductId() + item.getProductName())) {
                            allItemsList.add(item.getProductId() + item.getProductName());
                        }
                        productsList.add(item);
                    }
                    if (isHeaderToRefresh) {
                        itemTypeAll.setText(allItemsList.size() != 0 ? getString(R.string.all) + "(" + allItemsList.size() + ")" : getString(R.string.all));
                        itemTypeSold.setText(alreadySoldItemsList.size() != 0 ? getString(R.string.sold) + "(" + alreadySoldItemsList.size() + ")" : getString(R.string.sold));

                        itemTypeBidding.setText(itemsForBidList.size() != 0 ? getString(R.string.bidding) + "(" + itemsForBidList.size() + ")" : getString(R.string.bidding));
                        itemTypeSelling.setText(itemsForSaleList.size() != 0 ? getString(R.string.selling) + "(" + itemsForSaleList.size() + ")" : getString(R.string.selling));
                        itemTypeSwapping.setText(itemsForSwapList.size() != 0 ? getString(R.string.swapping) + "(" + itemsForSwapList.size() + ")" : getString(R.string.swapping));
                    }
                    if (objProductsGridViewContainerAdapter == null) {
                        objProductsGridViewContainerAdapter = new ProductContainerGridViewAdapterForMyItems(getFragmentManager(), getContext(), productsList, 0, ProfileFragment.this, getActivity(), executor);
                        if (type == R.id.image_dashboard_gridview) {
                            productsContainerRecylerView.setLayoutManager(gridLayoutManager);
                        } else {
                            productsContainerRecylerView.setLayoutManager(linearLayoutManager);
                        }

                        productsContainerRecylerView.setAdapter(objProductsGridViewContainerAdapter);
                        productsContainerRecylerView.setHasFixedSize(true);
                    } else {
                        objProductsGridViewContainerAdapter.notifyDataSetChanged();

                    }


                } else if (jsonObj.getString("status_message").equalsIgnoreCase("No Record Found")) {
                    noRecordsFoundLayout.setVisibility(View.VISIBLE);
                    if (selectedItemType.trim().isEmpty()) {
                        itemTypeAll.setText(getString(R.string.all));
                        itemTypeSold.setText(getString(R.string.sold));

                        itemTypeBidding.setText(getString(R.string.bidding));
                        itemTypeSelling.setText(getString(R.string.selling));
                        itemTypeSwapping.setText(getString(R.string.swapping));
                    }
                    if (productsList != null) {
                        productsList.clear();
                    }
                    if (objProductsGridViewContainerAdapter != null) {
                        objProductsGridViewContainerAdapter.notifyDataSetChanged();
                    }
                }

            } catch (Exception e) {
                Utils.log("Exception e:" + e.getMessage());
                Toast.makeText(getContext(), "Network Failure,please try again", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                objProgressDialog.cancel();
            }
            super.onPostExecute(result);
        }
    }

    @Override
    public void onItemClick(Object data) {
        //    objProductsGridViewContainerAdapter = null;
        ProductItem item = (ProductItem) data;
        MyItemDetailsFragment fragment2 = MyItemDetailsFragment.newInstance(item, (selectedItemType.equals("4") || item.getIsAlreadySold()));
        Bundle itemDetailsBundle = new Bundle();
        itemDetailsBundle.putBoolean("IsAlreadySold", (selectedItemType.equals("4") || item.getIsAlreadySold()));

        itemDetailsBundle.putParcelable("ItemDetails", item);
        fragment2.setArguments(itemDetailsBundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragment2.setTargetFragment(ProfileFragment.this, Constants.ITEM_DELETE_SOLD);
        fragmentTransaction.replace(R.id.frame_layout, fragment2);
       fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}