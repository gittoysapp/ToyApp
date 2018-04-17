package com.abhi.toyswap.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.Models.ProductDetails.ProductItem;
import com.abhi.toyswap.R;
import com.abhi.toyswap.interfaces.OnFavouriteIconClickListener;
import com.abhi.toyswap.interfaces.OnItemClickInterface;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.Utils;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * Created by Abhishek28.Gupta on 11-12-2017.
 */

public class ProductContainerGridViewAdapter extends RecyclerView.Adapter<ProductContainerGridViewAdapter.Holder> {

    private Context mContext;
    private Activity runningActivityInstance;
    public List<ProductItem> productsList;
    public ImageLoader imageLoader;
    private FragmentManager mFragmentManager;
    public int type;
    final int GRID = R.id.image_dashboard_gridview;
    final int LIST = R.id.image_dashboard_listview;
    private static LayoutInflater inflater = null;
    private OnItemClickInterface onItemClickInterface;
    private OnFavouriteIconClickListener onFavouriteIconClickListener;
    private ExecutorService executor;

    public ProductContainerGridViewAdapter(FragmentManager fragmentManager, Context context, List<ProductItem> productsList, int type, OnItemClickInterface onItemClickListener, Activity runningActivityInstance, ExecutorService executor, OnFavouriteIconClickListener onFavouriteIconClickListener) {
        mContext = context;
        this.onFavouriteIconClickListener = onFavouriteIconClickListener;
        this.productsList = productsList;
        this.runningActivityInstance = runningActivityInstance;
        this.executor = executor;
        this.mFragmentManager = fragmentManager;
        this.onItemClickInterface = onItemClickListener;
        this.type = type;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(context.getApplicationContext());

    }

    public void changeViewType(int type) {
        this.type = type;
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public ProductContainerGridViewAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;

        if (viewType == GRID) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_dashboard_grid_component, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_dashboard_listview_component, parent, false);
        }

        return new Holder(itemView, viewType);


    }

    @Override
    public int getItemViewType(int position) {
        if (type == LIST) {
            return LIST;
        } else {
            return GRID;
        }
    }

    @Override
    public void onBindViewHolder(ProductContainerGridViewAdapter.Holder holder, final int position) {


        holder.productNameTextView.setText(productsList.get(position).getProductName());
        try {
            if (productsList.get(position).getProductImageUrl().size() > 0) {
                imageLoader.DisplayImage(productsList.get(position).getProductImageUrl().get(0).getImageUrl(), holder.productImageView, true, holder.progressBar);
            } else {
                holder.productImageView.setImageResource(R.drawable.no_image_available);
                holder.progressBar.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (type == LIST) {
            holder.descriptionTextView.setText(!productsList.get(position).getDescription().isEmpty() ? productsList.get(position).getDescription().trim() : "No description is available");

            holder.postedByTextView.setText(productsList.get(position).getUserName());
        }
        holder.favouriteImageview.setImageResource(productsList.get(position).isFavourite() ? R.drawable.icon_favourite : R.drawable.icon_unfavourite);
        holder.favouriteImageview.setTag(R.id.is_favourite, productsList.get(position).isFavourite());
        holder.favouriteImageview.setTag(R.id.product_id, position);
        holder.locationImageView.setTag(productsList.get(position).getItemAddress());

        holder.locationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.getTag().toString().trim().isEmpty()) {
                    String map = "http://maps.google.co.in/maps?q=" + v.getTag().toString().replaceAll("#", "");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
                    mapIntent.setPackage("com.google.android.apps.maps");
                    mapIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    runningActivityInstance.startActivity(mapIntent);
                } else {
                    Toast.makeText(mContext, "No Location Details available for this Product!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        holder.buyButton.setImageResource(productsList.get(position).isAvailableForBuy() ? R.drawable.icon_buy_enabled : R.drawable.icon_buy_disabled);
        holder.swapButton.setImageResource(productsList.get(position).isAvailableForSwap() ? R.drawable.icon_swap_enabled : R.drawable.icon_swap_disabled);
        holder.bidButton.setImageResource(productsList.get(position).isAvailableForBid() ? R.drawable.icon_bid_enabled : R.drawable.icon_bid_disabled);

        holder.productPriceTextView.setText("$" + productsList.get(position).getPrice());
        holder.favouriteImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavouriteUnfavouriteThread favouriteUnfavouriteThread = new FavouriteUnfavouriteThread(v);
                executor.execute(favouriteUnfavouriteThread);
            }
        });


        holder.buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        holder.swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        holder.bidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        holder.bind(productsList.get(position));

    }

    public class Holder extends RecyclerView.ViewHolder {
        RelativeLayout topBarLayout;
        ImageView favouriteImageview;
        ImageView locationImageView;
        ImageView productImageView;
        TextView productNameTextView;
        TextView productPriceTextView;
        ImageView swapButton;
        ImageView buyButton;
        ImageView bidButton;
        TextView descriptionTextView;
        TextView postedByTextView;
        ProgressBar progressBar;
        final int LIST = R.id.image_dashboard_listview;

        public void bind(final ProductItem item) {
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    onItemClickInterface.onItemClick(item);
                }

            });
        }

        public Holder(View rowView, int type) {
            super(rowView);

            if (type == LIST) {
                productNameTextView = (TextView) rowView.findViewById(R.id.text_dashboard_list_component_productname);
                productPriceTextView = (TextView) rowView.findViewById(R.id.text_dashboard_list_component_price);
                favouriteImageview = (ImageView) rowView.findViewById(R.id.image_dashboard_list_component_favourite);
                productImageView = (ImageView) rowView.findViewById(R.id.image_dashboard_list_component_product_image);
                locationImageView = (ImageView) rowView.findViewById(R.id.image_dashboard_grid_component_location);
                swapButton = (ImageView) rowView.findViewById(R.id.imageview_dashboard_list_component_swap);
                buyButton = (ImageView) rowView.findViewById(R.id.imageview_dashboard_list_component_buy);
                bidButton = (ImageView) rowView.findViewById(R.id.imageview_dashboard_list_component_bid);
                descriptionTextView = (TextView) rowView.findViewById(R.id.text_dashboard_list_component_productdescription);
                postedByTextView = (TextView) rowView.findViewById(R.id.text_dashboard_list_component_productbyowner);
                progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);
            } else {
                productNameTextView = (TextView) rowView.findViewById(R.id.text_dashboard_grid_component_productname);
                topBarLayout = (RelativeLayout) rowView.findViewById(R.id.relative_dashboard_grid_component_topbar);
                productPriceTextView = (TextView) rowView.findViewById(R.id.text_dashboard_grid_component_price);
                favouriteImageview = (ImageView) rowView.findViewById(R.id.image_dashboard_grid_component_favourite);
                locationImageView = (ImageView) rowView.findViewById(R.id.image_dashboard_grid_component_location);
                productImageView = (ImageView) rowView.findViewById(R.id.image_dashboard_grid_component_product_image);
                swapButton = (ImageView) rowView.findViewById(R.id.imageview_dashboard_grid_component_swap);
                buyButton = (ImageView) rowView.findViewById(R.id.imageview_dashboard_grid_component_buy);
                bidButton = (ImageView) rowView.findViewById(R.id.imageview_dashboard_grid_component_bid);
                progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);
            }
        }
    }

    public class FavouriteUnfavouriteThread implements Runnable {

        private ImageView favouriteImageView;

        public FavouriteUnfavouriteThread(View favouriteImageView) {
            this.favouriteImageView = (ImageView) favouriteImageView;
        }

        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();

            try {
                fetchItemsJson.put("task", "setItemlike");
                fetchItemsJson.put("user_id", Utils.getDataFromSharedPreferences(runningActivityInstance, "UserId"));
                fetchItemsJson.put("item_id", productsList.get(Integer.parseInt(String.valueOf(favouriteImageView.getTag(R.id.product_id)))).getProductId());
                fetchItemsJson.put("is_like", Boolean.valueOf(favouriteImageView.getTag(R.id.is_favourite).toString()) ? "0" : "1");
                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.GET_ITEMS, fetchItemsJson);
                JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Item Liked")) {
                    runningActivityInstance.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            favouriteImageView.setImageResource(R.drawable.icon_favourite);
                            favouriteImageView.setTag(R.id.is_favourite, true);
                            productsList.get(Integer.parseInt(String.valueOf(favouriteImageView.getTag(R.id.product_id)))).setFavourite(true);

                        }
                    });
                } else if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Item Unliked")) {
                    runningActivityInstance.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (type == 0) {
                                productsList.remove(Integer.parseInt(String.valueOf(favouriteImageView.getTag(R.id.product_id))));
                                notifyDataSetChanged();
                                if (productsList.size() == 0) {
                                    onFavouriteIconClickListener.onFavIconClicked();
                                }
                            } else {
                                favouriteImageView.setImageResource(R.drawable.icon_unfavourite);
                                favouriteImageView.setTag(R.id.is_favourite, false);
                                productsList.get(Integer.parseInt(String.valueOf(favouriteImageView.getTag(R.id.product_id)))).setFavourite(false);
                            }

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