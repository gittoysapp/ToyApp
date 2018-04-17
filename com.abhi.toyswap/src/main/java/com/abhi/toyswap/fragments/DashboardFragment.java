package com.abhi.toyswap.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.Models.Filters.Filters;
import com.abhi.toyswap.Models.ProductDetails.ItemImage;
import com.abhi.toyswap.Models.ProductDetails.ProductItem;
import com.abhi.toyswap.R;
import com.abhi.toyswap.activity.DashboardActivity;
import com.abhi.toyswap.activity.FiltersActivity;
import com.abhi.toyswap.adapters.ProductContainerGridViewAdapter;
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


public class DashboardFragment extends Fragment implements View.OnClickListener, OnItemClickInterface {

    private ImageView filtersImageView;
    private ImageView gridViewImageView;
    private ImageView listViewImageView;
    private EditText searchEditText;
    private LinearLayout noRecordsFoundLayout;
    private TabLayout tabLayout;
    private RecyclerView productsContainerRecylerView;
    private GridLayoutManager gridLayoutManager;
    private LinearLayoutManager linearLayoutManager;
    private ProductContainerGridViewAdapter objProductsGridViewContainerAdapter;
    private ProgressDialog objProgressDialog;
    private GPSTracker objGpsTracker;
    private List<ProductItem> productsList;
    private int type = R.id.image_dashboard_gridview;
    private Filters filterData;
    ExecutorService executor = Executors.newFixedThreadPool(4);
    private View view;
    DividerItemDecoration dividerItemDecoration;

    public static DashboardFragment newInstance() {
        DashboardFragment fragment = new DashboardFragment();

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
            view = inflater.inflate(R.layout.fragment_dashboard, container, false);
            tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
            filtersImageView = (ImageView) view.findViewById(R.id.image_dashboard_filters);
            noRecordsFoundLayout = (LinearLayout) view.findViewById(R.id.linear_dashboard_no_items_available);
            searchEditText = (EditText) view.findViewById(R.id.edit_dashboard_search);
            gridViewImageView = (ImageView) view.findViewById(R.id.image_dashboard_gridview);
            listViewImageView = (ImageView) view.findViewById(R.id.image_dashboard_listview);
            productsContainerRecylerView = (RecyclerView) view.findViewById(R.id.gridview_dashboard_container);
             dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
            //  dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider_recyclerview));

            // productsContainerRecylerView.addItemDecoration(dividerItemDecoration);

            productsContainerRecylerView.addItemDecoration(new ItemDecorationAlbumColumns(
                    getResources().getDimensionPixelSize(R.dimen.space_0), 2));

            gridLayoutManager = new GridLayoutManager(getContext(), 2);
            linearLayoutManager = new LinearLayoutManager(getContext());

            objGpsTracker = GPSTracker.getInstance(getContext(), getActivity());
            searchEditText.addTextChangedListener(new TextWatcher() {

                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() != 0) {
                        searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_clear_text, 0);
                    } else {
                        searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.search_icon, 0);
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }
            });
            searchEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;

                    if (event.getAction() == MotionEvent.ACTION_UP && !searchEditText.getText().toString().isEmpty()) {
                        if (event.getRawX() >= (searchEditText.getRight() - searchEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            searchEditText.setText("");
                            ((DashboardActivity) getActivity()).hideKeyboard();
                            new getItemsTask().execute();
                            return true;
                        }
                    }
                    return false;
                }
            });
            searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        ((DashboardActivity) getActivity()).hideKeyboard();

                        new getItemsTask().execute();

                        return true;
                    }
                    return false;
                }
            });
            filterData = new Filters();
            new getItemsTask().execute();

            filtersImageView.setOnClickListener(this);
            listViewImageView.setOnClickListener(this);
            gridViewImageView.setOnClickListener(this);
            tabLayout.addTab(tabLayout.newTab().setText("All"));
            tabLayout.addTab(tabLayout.newTab().setText("Recent"));
            tabLayout.addTab(tabLayout.newTab().setText("Popular"));
            tabLayout.addTab(tabLayout.newTab().setText("Following"));
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    new getItemsTask().execute();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
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
            productsList.set(productsList.indexOf(item), item);
            objProductsGridViewContainerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_dashboard_filters:
                Intent filtersIntent = new Intent(getActivity(), FiltersActivity.class);
                filtersIntent.putExtra("FilterData", filterData);
                startActivityForResult(filtersIntent, 2);
                break;
            case R.id.image_dashboard_gridview:
                type = R.id.image_dashboard_gridview;
                objProductsGridViewContainerAdapter.changeViewType(R.id.image_dashboard_gridview);
                productsContainerRecylerView.setLayoutManager(gridLayoutManager);
                objProductsGridViewContainerAdapter.notifyDataSetChanged();

                break;
            case R.id.image_dashboard_listview:
                type = R.id.image_dashboard_listview;
                objProductsGridViewContainerAdapter.changeViewType(R.id.image_dashboard_listview);
                productsContainerRecylerView.setLayoutManager(linearLayoutManager);
                objProductsGridViewContainerAdapter.notifyDataSetChanged();
                break;
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
                fetchItemsJson.put("recent_date", tabLayout.getSelectedTabPosition() == 1 ? "1" : "0");
                fetchItemsJson.put("distance", String.valueOf(filterData.getDistanceLimit() == 0 ? "" : filterData.getDistanceLimit()));
                fetchItemsJson.put("usertimeZone", TimeZone.getDefault().getID());
                fetchItemsJson.put("popular_item", tabLayout.getSelectedTabPosition() == 2 ? "1" : "0");
                fetchItemsJson.put("following_item", tabLayout.getSelectedTabPosition() == 3 ? "1" : "0");
                fetchItemsJson.put("user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                fetchItemsJson.put("item_id", "");
                fetchItemsJson.put("itemType_id", filterData.getType());
                if(filterData.getPriceLower() == 0 && filterData.getPriceUpper() == 0){
                    fetchItemsJson.put("price_lower", "");
                    fetchItemsJson.put("price_upper","");
                }else{
                    fetchItemsJson.put("price_lower", String.valueOf(filterData.getPriceLower()));
                    fetchItemsJson.put("price_upper",  filterData.getPriceUpper()==1000?"99999":String.valueOf(filterData.getPriceUpper()));
                }

                if(filterData.getAgeLower() == 0 && filterData.getAgeUpper() == 0){
                    fetchItemsJson.put("age_group_lower", "");
                    fetchItemsJson.put("age_group_upper","");
                }else{
                    fetchItemsJson.put("age_group_lower", String.valueOf(filterData.getAgeLower()));
                    fetchItemsJson.put("age_group_upper",  String.valueOf(filterData.getAgeUpper()));
                }
                fetchItemsJson.put("date_limit", filterData.getDateLimit() == null ? "" : filterData.getDateLimit());
                //fetchItemsJson.put("age_group_id", "");
                fetchItemsJson.put("search_keyword", searchEditText.getText().toString());
                fetchItemsJson.put("lattitude", "");
                fetchItemsJson.put("longitude", "");
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
                        item.setIsAlreadyReported(itemJsonObject.getInt("item_isreport") == 1 ? true : false);

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
                        objProductsGridViewContainerAdapter = new ProductContainerGridViewAdapter(getFragmentManager(), getContext(), productsList, type, DashboardFragment.this, getActivity(), executor, null);
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

                    productsList.clear();

                    objProductsGridViewContainerAdapter.notifyDataSetChanged();
                }

            } catch (Exception e) {
                Utils.log("Exception e:" + e.getMessage());
                //    Toast.makeText(getContext(), "Network Failure,please try again", Toast.LENGTH_SHORT).show();
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
        ItemDetailsFragment fragment2 = ItemDetailsFragment.newInstance(item);
        Bundle itemDetailsBundle = new Bundle();
        itemDetailsBundle.putParcelable("ItemDetails", item);
        fragment2.setArguments(itemDetailsBundle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragment2.setTargetFragment(DashboardFragment.this, Constants.BACK_DETAILS_FRAGMENT);
        fragmentTransaction.replace(R.id.frame_layout, fragment2);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();


    }
}