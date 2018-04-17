package com.abhi.toyswap.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.Models.ProductDetails.ItemImage;
import com.abhi.toyswap.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Abhishek28.Gupta on 11-12-2017.
 */

public class ItemDetailsPhotoAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private List<ItemImage> imagesList = new ArrayList<ItemImage>();
    private ImageLoader imageLoader;

    public ItemDetailsPhotoAdapter(Context context,List<ItemImage> imagesList) {
        this.context = context;
        this.imagesList=imagesList;
        imageLoader = new ImageLoader(context);

    }

    @Override
    public int getCount() {
        return (imagesList == null ||imagesList.size()==0 ? 1 : imagesList.size());
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.view_item_details_photo, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.image_view_item_photo);
        if(imagesList==null || imagesList.size()==0){
            imageView.setImageResource(R.drawable.no_image_available);
        }else{
            imageLoader.DisplayImage(imagesList.get(position).getImageUrl(), imageView, true);
        }

        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}

