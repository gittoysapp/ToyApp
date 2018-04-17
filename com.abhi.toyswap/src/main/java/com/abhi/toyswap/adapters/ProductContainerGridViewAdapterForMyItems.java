package com.abhi.toyswap.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.Models.ProductDetails.ProductItem;
import com.abhi.toyswap.R;
import com.abhi.toyswap.interfaces.OnItemClickInterface;

import java.util.List;
import java.util.concurrent.ExecutorService;


/**
 * Created by Abhishek28.Gupta on 11-12-2017.
 */

public class ProductContainerGridViewAdapterForMyItems extends RecyclerView.Adapter<ProductContainerGridViewAdapterForMyItems.Holder> {

    private Context mContext;
    private Activity runningActivityInstance;
    public List<ProductItem> productsList;
    public ImageLoader imageLoader;
    private FragmentManager mFragmentManager;
    public int type;
    final int GRID = R.id.image_profile_gridview;
    final int LIST = R.id.image_profile_listview;
    private static LayoutInflater inflater = null;
    private OnItemClickInterface onItemClickInterface;
    private ExecutorService executor;

    public ProductContainerGridViewAdapterForMyItems(FragmentManager fragmentManager, Context context, List<ProductItem> productsList, int type, OnItemClickInterface onItemClickListener, Activity runningActivityInstance, ExecutorService executor) {
        mContext = context;
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
    public ProductContainerGridViewAdapterForMyItems.Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;

        if (viewType == GRID) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_dashboard_grid_component_for_my_items, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_dashboard_listview_component_for_my_items, parent, false);
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
    public void onBindViewHolder(ProductContainerGridViewAdapterForMyItems.Holder holder, final int position) {


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
            if(!productsList.get(position).getDescription().trim().isEmpty()){
                holder.descriptionTextView.setText(productsList.get(position).getDescription());
            }
        }
        holder.productPriceTextView.setText("$" + productsList.get(position).getPrice());


        holder.bind(productsList.get(position));

    }

    public class Holder extends RecyclerView.ViewHolder {
        RelativeLayout topBarLayout;
        ImageView productImageView;
        TextView productNameTextView;
        TextView productPriceTextView;
        TextView descriptionTextView;
        ProgressBar progressBar;
        final int LIST = R.id.image_profile_listview;

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
                productNameTextView = (TextView) rowView.findViewById(R.id.text_dashboard_list_component_my_items_productname);
                productPriceTextView = (TextView) rowView.findViewById(R.id.text_dashboard_list_component_my_items_price);
                productImageView = (ImageView) rowView.findViewById(R.id.image_dashboard_list_component_my_items_product_image);
                descriptionTextView = (TextView) rowView.findViewById(R.id.text_dashboard_list_component_my_items_productdescription);
                progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);
            } else {
                productNameTextView = (TextView) rowView.findViewById(R.id.text_dashboard_grid_component_my_items_productname);
                topBarLayout = (RelativeLayout) rowView.findViewById(R.id.relative_dashboard_grid_component_my_items_topbar);
                productPriceTextView = (TextView) rowView.findViewById(R.id.text_dashboard_grid_component_my_items_price);
                productImageView = (ImageView) rowView.findViewById(R.id.image_dashboard_grid_component_my_items_product_image);
                progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);
            }
        }
    }


}