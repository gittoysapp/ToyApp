package com.abhi.toyswap.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.abhi.toyswap.Models.Category;
import com.abhi.toyswap.R;

/**
 * Created by Abhishek28.Gupta on 08-01-2018.
 */

public class CategoriesSpinnerAdapter extends ArrayAdapter<Category> {


        private Context context;
        private Category[] values;

        public CategoriesSpinnerAdapter(Context context, int textViewResourceId,
                           Category[] values) {
            super(context, textViewResourceId, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public int getCount(){
            return values.length;
        }

        @Override
        public Category getItem(int position){
            return values[position];
        }

        @Override
        public long getItemId(int position){
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).
                        inflate(R.layout.custom_spinner_item_view, parent, false);
            }

            TextView label = (TextView) convertView.findViewById(android.R.id.text1);
            label.setPadding(8,8,8,8);
            label.setTextColor(Color.BLACK);

            label.setText(values[position].getCategoryName());
            return convertView;
        }

       @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
           if (convertView == null) {
               convertView = LayoutInflater.from(context).
                       inflate(R.layout.custom_spinner_item_view, parent, false);
           }

           TextView label = (TextView) convertView.findViewById(android.R.id.text1);
           label.setPadding(12,12,12,12);
           label.setTextColor(Color.BLACK);

           label.setText(values[position].getCategoryName());
           return convertView;

    }
}
