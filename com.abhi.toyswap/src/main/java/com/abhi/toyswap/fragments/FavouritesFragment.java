package com.abhi.toyswap.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.Models.Filters.Filters;
import com.abhi.toyswap.Models.ProductDetails.ItemImage;
import com.abhi.toyswap.Models.ProductDetails.ProductItem;
import com.abhi.toyswap.R;
import com.abhi.toyswap.adapters.ProductContainerGridViewAdapter;
import com.abhi.toyswap.interfaces.OnFavouriteIconClickListener;
import com.abhi.toyswap.interfaces.OnItemClickInterface;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.GPSTracker;
import com.abhi.toyswap.utils.ItemDecorationAlbumColumns;
import com.abhi.toyswap.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FavouritesFragment extends Fragment implements View.OnClickListener, OnItemClickInterface, OnFavouriteIconClickListener {

    private LinearLayout noRecordsFoundLayout;
    private RecyclerView productsContainerRecylerView;
    private GridLayoutManager gridLayoutManager;
    private ProductContainerGridViewAdapter objProductsGridViewContainerAdapter;
    private ProgressDialog objProgressDialog;
    private GPSTracker objGpsTracker;
    private List<ProductItem> productsList;
    private int type = R.id.image_dashboard_gridview;
    private Filters filterData;
    ExecutorService executor = Executors.newFixedThreadPool(4);
    private View view;


    public static FavouritesFragment newInstance() {
        FavouritesFragment fragment = new FavouritesFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {

            view = inflater.inflate(R.layout.fragment_favourites, container, false);
            noRecordsFoundLayout = (LinearLayout) view.findViewById(R.id.linear_dashboard_no_items_available);
            productsContainerRecylerView = (RecyclerView) view.findViewById(R.id.gridview_dashboard_container);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);

            productsContainerRecylerView.addItemDecoration(dividerItemDecoration);

            productsContainerRecylerView.addItemDecoration(new ItemDecorationAlbumColumns(
                    getResources().getDimensionPixelSize(R.dimen.space_0), 2));
            gridLayoutManager = new GridLayoutManager(getContext(), 2);
            objGpsTracker = GPSTracker.getInstance(getContext(), getActivity());

            filterData = new Filters();
            new getItemsTask().execute();

        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            filterData = data.getParcelableExtra("FilterData");
            new getItemsTask().execute();
        } else if (resultCode == Constants.BACK_DETAILS_FRAGMENT) {
            ProductItem item = data.getParcelableExtra("ProductItem");
            if (!item.isFavourite()) {
                productsList.remove(item);
                if (productsList.size() == 0) {
                    noRecordsFoundLayout.setVisibility(View.VISIBLE);
                }
                objProductsGridViewContainerAdapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {


        }
    }


    class getItemsTask extends AsyncTask<Void, Void, String> {
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
                fetchItemsJson.put("category_id", filterData.getCategory() == null ? "" : filterData.getCategory().replaceAll("C", ""));
                fetchItemsJson.put("recent_date", "0");
                fetchItemsJson.put("distance", filterData.getDistanceLimit() == 0 ? "" : filterData.getDistanceLimit());
                fetchItemsJson.put("usertimeZone", TimeZone.getDefault().getID());
                fetchItemsJson.put("popular_item", "0");
                fetchItemsJson.put("following_item", "0");
                fetchItemsJson.put("user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                fetchItemsJson.put("item_id", "");
                fetchItemsJson.put("itemType_id", filterData.getType());
                fetchItemsJson.put("price_lower", filterData.getPriceLower() == 0 ? "" : filterData.getPriceLower());
                fetchItemsJson.put("price_upper", filterData.getPriceUpper() == 0 ? "" : filterData.getPriceUpper());
                fetchItemsJson.put("date_limit", filterData.getDateLimit() == null ? "" : filterData.getDateLimit());
                fetchItemsJson.put("age_group_id", "");
                fetchItemsJson.put("search_keyword", "");
                fetchItemsJson.put("lattitude", "");
                fetchItemsJson.put("longitude", "");
                fetchItemsJson.put("myfav", "1");

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
                        item.setUserName(itemJsonObject.getString("user_name"));
                        item.setPostedTime(itemJsonObject.getString("postedtime"));
                        item.setNumberOfLikes(itemJsonObject.getInt("user_likes"));
                        item.setNumberOfDislikes(itemJsonObject.getInt("user_dislikes"));
                        item.setFavourite(itemJsonObject.getInt("item_islike") == 1 ? true : false);
                        item.setFollowed(itemJsonObject.getInt("item_isfollow") == 1 ? true : false);

                        itemTypesJsonArray = itemJsonObject.getJSONArray("item_type");
                        for (int index2 = 0; index2 < itemTypesJsonArray.length(); index2++) {
                            if (itemTypesJsonArray.getJSONObject(index2).getString("name").equalsIgnoreCase("sell")) {
                                item.setAvailableForBuy(true);
                                item.setPrice(itemTypesJsonArray.getJSONObject(index2).getString("price"));
                            }
                            if (itemTypesJsonArray.getJSONObject(index2).getString("name").equalsIgnoreCase("swap")) {
                                item.setAvailableForSwap(true);
                                item.setPrice(itemTypesJsonArray.getJSONObject(index2).getString("price"));
                            }
                            if (itemTypesJsonArray.getJSONObject(index2).getString("name").equalsIgnoreCase("bid")) {
                                item.setAvailableForBid(true);
                                item.setPrice(itemTypesJsonArray.getJSONObject(index2).getString("price"));
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

                        productsList.add(item);
                    }
                    if (objProductsGridViewContainerAdapter == null) {
                        objProductsGridViewContainerAdapter = new ProductContainerGridViewAdapter(getFragmentManager(), getContext(), productsList, 0, FavouritesFragment.this, getActivity(), executor, FavouritesFragment.this);

                        productsContainerRecylerView.setLayoutManager(gridLayoutManager);

                        productsContainerRecylerView.setAdapter(objProductsGridViewContainerAdapter);
                        productsContainerRecylerView.setHasFixedSize(true);
                    } else {
                        objProductsGridViewContainerAdapter.notifyDataSetChanged();

                    }


                } else if (jsonObj.getString("status_message").equalsIgnoreCase("No Record Found")) {
                    noRecordsFoundLayout.setVisibility(View.VISIBLE);
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
        ProductItem item = (ProductItem) data;
        ItemDetailsFragment fragment2 = ItemDetailsFragment.newInstance(item);
        Bundle itemDetailsBundle = new Bundle();
        itemDetailsBundle.putParcelable("ItemDetails", item);
        fragment2.setArguments(itemDetailsBundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragment2.setTargetFragment(FavouritesFragment.this, Constants.BACK_DETAILS_FRAGMENT);
        fragmentTransaction.replace(R.id.frame_layout, fragment2);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onFavIconClicked() {
        noRecordsFoundLayout.setVisibility(View.VISIBLE);
    }
}