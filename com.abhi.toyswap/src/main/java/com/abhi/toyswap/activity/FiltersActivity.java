package com.abhi.toyswap.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.CustomViews.RangeSeekBar;
import com.abhi.toyswap.Models.Filters.Filters;
import com.abhi.toyswap.R;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.PixelUtil;

import org.json.JSONArray;
import org.json.JSONObject;


public class FiltersActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private Button resetButton;
    private Button applyButton;
    private ImageView backImageView;
    private LinearLayout categoriesLayoutContainer;
    private CheckBox[] categoriesCheckboxes;

    private RangeSeekBar priceRangeSeekBar;
    private RangeSeekBar ageRangeSeekBar;
    private RangeSeekBar distanceRangeSeekBar;
    private RadioGroup postedWithinRadioGroup;
    private CheckBox[] typeCheckboxes;
    private Filters objFilters;
    private String selectedTypes = "";
    private String selectedCategories = "";
    private boolean isCheckboxChangedProgramatically = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_filters);
        final ScrollView main = (ScrollView) findViewById(R.id.scrollview_filters);
        objFilters = this.getIntent().getParcelableExtra("FilterData");
        main.post(new Runnable() {
            public void run() {
                main.scrollTo(0, 0);
            }
        });

        typeCheckboxes = new CheckBox[4];
        backImageView = (ImageView) this.findViewById(R.id.imageview_filter_back);
        backImageView.setOnClickListener(this);
        resetButton = (Button) this.findViewById(R.id.button_filters_reset);
        applyButton = (Button) this.findViewById(R.id.button_filters_apply);
        categoriesLayoutContainer = (LinearLayout) this.findViewById(R.id.linear_filters_categories);
        postedWithinRadioGroup = (RadioGroup) this.findViewById(R.id.radiogroup_filters_posted_within);
        priceRangeSeekBar = (RangeSeekBar) this.findViewById(R.id.slidingbar_filters_price);
        ageRangeSeekBar = (RangeSeekBar) this.findViewById(R.id.slidingbar_filters_age);
        distanceRangeSeekBar = (RangeSeekBar) this.findViewById(R.id.slidingbar_filters_distance);
        typeCheckboxes[0] = (CheckBox) this.findViewById(R.id.checkbox_filters_toy_type_buy);
        typeCheckboxes[1] = (CheckBox) this.findViewById(R.id.checkbox_filters_toy_type_swap);
        typeCheckboxes[2] = (CheckBox) this.findViewById(R.id.checkbox_filters_toy_type_bid);
        typeCheckboxes[3] = (CheckBox) this.findViewById(R.id.checkbox_filters_toy_type_all);
        typeCheckboxes[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isCheckboxChangedProgramatically = true;
                    for (int index = 0; index < typeCheckboxes.length - 1; index++) {
                        typeCheckboxes[index].setChecked(false);
                    }
                    isCheckboxChangedProgramatically = false;
                }
            }
        });
        for (int index = 0; index <= 2; index++) {
            typeCheckboxes[index].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isCheckboxChangedProgramatically) {
                        typeCheckboxes[3].setChecked(false);
                    }

                }
            });
        }
        resetButton.setOnClickListener(this);
        applyButton.setOnClickListener(this);
        new GetCategoriesTask().execute();
        if (objFilters != null) {
            ageRangeSeekBar.setSelectedMinValue(objFilters.getAgeLower());
            ageRangeSeekBar.setSelectedMaxValue(objFilters.getAgeUpper());
            distanceRangeSeekBar.setSelectedMaxValue(objFilters.getDistanceLimit());
            priceRangeSeekBar.setSelectedMaxValue(objFilters.getPriceUpper());
            priceRangeSeekBar.setSelectedMinValue(objFilters.getPriceLower());

            if (objFilters.getType() != null) {
                if (objFilters.getType().isEmpty()) {
                    typeCheckboxes[3].setChecked(true);
                } else {
                    if (!objFilters.getType().contains(",")) {
                        if (objFilters.getType().equalsIgnoreCase("1")) {
                            typeCheckboxes[0].setChecked(true);
                        } else if (objFilters.getType().equalsIgnoreCase("2")) {
                            typeCheckboxes[1].setChecked(true);
                        } else if (objFilters.getType().equalsIgnoreCase("3")) {
                            typeCheckboxes[2].setChecked(true);
                        }
                    } else {
                        for (String type : objFilters.getType().split(",")) {
                            if (type.equalsIgnoreCase("1")) {
                                typeCheckboxes[0].setChecked(true);
                            } else if (type.equalsIgnoreCase("2")) {
                                typeCheckboxes[1].setChecked(true);
                            } else if (type.equalsIgnoreCase("3")) {
                                typeCheckboxes[2].setChecked(true);
                            }
                        }
                    }
                }
            }
            if (objFilters.getDateLimit() != null && !objFilters.getDateLimit().isEmpty()) {
                if (objFilters.getDateLimit().equals("0")) {
                    ((RadioButton) findViewById(R.id.radiobutton_filters_posted_within_last_one_day)).setChecked(true);
                } else if (objFilters.getDateLimit().equals("7")) {
                    ((RadioButton) findViewById(R.id.radiobutton_filters_posted_within_last_7_days)).setChecked(true);
                } else if (objFilters.getDateLimit().equals("30")) {
                    ((RadioButton) findViewById(R.id.radiobutton_filters_posted_within_last_30_days)).setChecked(true);
                }
            }

        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_filter_back: {
                finish();
                break;
            }
            case R.id.button_filters_reset: {
                priceRangeSeekBar.resetSelectedValues();
                ageRangeSeekBar.resetSelectedValues();
                distanceRangeSeekBar.resetSelectedValues();
                typeCheckboxes[3].setChecked(true);
                categoriesCheckboxes[0].setChecked(true);
                postedWithinRadioGroup.clearCheck();
                objFilters.setType("");
                objFilters.setCategory(null);
                objFilters.setDateLimit(null);

                break;
            }
            case R.id.button_filters_apply: {
                objFilters.setAgeLower(Integer.parseInt(String.valueOf(ageRangeSeekBar.getSelectedMinValue())));
                objFilters.setAgeUpper(Integer.parseInt(String.valueOf(ageRangeSeekBar.getSelectedMaxValue())));
                objFilters.setDistanceLimit(Integer.parseInt(String.valueOf(distanceRangeSeekBar.getSelectedMaxValue())));
                objFilters.setPriceLower(Integer.parseInt(String.valueOf(priceRangeSeekBar.getSelectedMinValue())));
                objFilters.setPriceUpper(Integer.parseInt(String.valueOf(priceRangeSeekBar.getSelectedMaxValue())));

                if (!typeCheckboxes[typeCheckboxes.length - 1].isChecked()) {
                    for (int index = 0; index < typeCheckboxes.length - 1; index++) {
                        if (typeCheckboxes[index].isChecked()) {
                            if (selectedTypes.isEmpty()) {
                                selectedTypes = typeCheckboxes[index].getTag().toString();
                            } else {
                                selectedTypes = selectedTypes + "," + typeCheckboxes[index].getTag().toString();
                            }
                        }
                    }
                }
                objFilters.setType(selectedTypes);


                if (!categoriesCheckboxes[0].isChecked()) {
                    for (int index = 1; index < categoriesCheckboxes.length; index++) {
                        if (categoriesCheckboxes[index].isChecked()) {
                            if (selectedCategories.isEmpty()) {
                                selectedCategories = categoriesCheckboxes[index].getTag().toString();
                            } else {
                                selectedCategories = selectedCategories + "," + categoriesCheckboxes[index].getTag().toString();
                            }
                        }
                    }
                } else {
                    selectedCategories = "";
                }
                objFilters.setCategory(selectedCategories);

                if (postedWithinRadioGroup.getCheckedRadioButtonId() != -1) {
                    objFilters.setDateLimit((findViewById(postedWithinRadioGroup.getCheckedRadioButtonId())).getTag().toString());
                }
                Intent resultIntent = new Intent();
                resultIntent.putExtra("FilterData", objFilters);
                setResult(RESULT_OK, resultIntent);
                finish();
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            isCheckboxChangedProgramatically = true;
            for (int index = 1; index < categoriesCheckboxes.length; index++) {
                categoriesCheckboxes[index].setChecked(false);
            }
            isCheckboxChangedProgramatically = false;
        }
    }


    class GetCategoriesTask extends AsyncTask<Void, Void, String> {
        ProgressDialog objProgressDialog;

        @Override
        protected void onPreExecute() {
            objProgressDialog = new ProgressDialog(FiltersActivity.this);
            objProgressDialog.setMessage("Please wait..");
            objProgressDialog.setCanceledOnTouchOutside(false);
            objProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject registrationJson = new JSONObject();
            try {
                registrationJson.put("task", "getAllCategories");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Connection objConnection = new Connection();
            String response = objConnection.getResponseFromWebservice(Constants.GET_CATEGORIES, registrationJson);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONArray categoriesJsonArray;
                JSONObject categoryJsonObject;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Record Found")) {
                    categoriesJsonArray = jsonObj.getJSONArray("data");
                    categoriesCheckboxes = new CheckBox[categoriesJsonArray.length() + 1];

                    categoriesCheckboxes[0] = new CheckBox(FiltersActivity.this);
                    categoriesCheckboxes[0].setText("All");
                    categoriesCheckboxes[0].setId(0);
                    categoriesCheckboxes[0].setTextSize(12);
                    categoriesCheckboxes[0].setTag("All");
                    categoriesCheckboxes[0].setButtonDrawable(android.R.color.transparent);

                    categoriesCheckboxes[0].setOnCheckedChangeListener(FiltersActivity.this);
                    categoriesCheckboxes[0].setPadding(PixelUtil.dpToPx(FiltersActivity.this, 20), 5, PixelUtil.dpToPx(FiltersActivity.this, 20), 5);

                    categoriesCheckboxes[0].setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.btn_star, 0);
                    categoriesCheckboxes[0].setLayoutParams(lp);
                    categoriesLayoutContainer.addView(categoriesCheckboxes[0], lp);

                    for (int index = 0; index < categoriesJsonArray.length(); index++) {
                        categoryJsonObject = categoriesJsonArray.getJSONObject(index);
                        categoriesCheckboxes[index + 1] = new CheckBox(FiltersActivity.this);
                        categoriesCheckboxes[index + 1].setText(categoryJsonObject.getString("category_name"));
                        categoriesCheckboxes[index + 1].setId(Integer.parseInt(categoryJsonObject.getString("category_id")));
                        categoriesCheckboxes[index + 1].setTextSize(12);
                        categoriesCheckboxes[index + 1].setTag("C" + categoryJsonObject.getString("category_id") + "C");
                        categoriesCheckboxes[index + 1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (!isCheckboxChangedProgramatically) {
                                    categoriesCheckboxes[0].setChecked(false);
                                }
                            }
                        });
                        categoriesCheckboxes[index + 1].setButtonDrawable(android.R.color.transparent);
                        if (objFilters != null && objFilters.getCategory() != null) {
                            if (objFilters.getCategory().contains("C" + categoryJsonObject.getString("category_id") + "C")) {
                                categoriesCheckboxes[index + 1].setChecked(true);
                            }
                        }

                        categoriesCheckboxes[index + 1].setPadding(PixelUtil.dpToPx(FiltersActivity.this, 20), 5, PixelUtil.dpToPx(FiltersActivity.this, 20), 5);

                        categoriesCheckboxes[index + 1].setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.btn_star, 0);
                        categoriesCheckboxes[index + 1].setLayoutParams(lp);
                        categoriesLayoutContainer.addView(categoriesCheckboxes[index + 1], lp);
                    }
                    if (objFilters != null && objFilters.getCategory() != null) {
                        if (objFilters.getCategory().trim().isEmpty()) {
                            categoriesCheckboxes[0].setChecked(true);
                        }
                    }
                }
            } catch (Exception e) {
                Toast.makeText(FiltersActivity.this, "Network Failure,please try again", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                objProgressDialog.cancel();
            }
            super.onPostExecute(result);
        }
    }
}
