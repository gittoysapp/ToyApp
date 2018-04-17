package com.abhi.toyswap.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.Models.Category;
import com.abhi.toyswap.Models.ProductDetails.ItemImage;
import com.abhi.toyswap.Models.ProductDetails.ProductItem;
import com.abhi.toyswap.R;
import com.abhi.toyswap.activity.AddItemActivity;
import com.abhi.toyswap.activity.DashboardActivity;
import com.abhi.toyswap.adapters.CategoriesSpinnerAdapter;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.GPSTracker;
import com.abhi.toyswap.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link EditItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditItemFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private View view;
    private String mFilePath;
    private ProductItem mProductItem;
    private Spinner agegroupSpinner;
    private Spinner categorySpinner;
    private EditText priceEditText;
    private EditText nameEditText;
    private EditText descriptionEditText;

    private EditText addressEditText;
    private EditText cityEditText;
    private EditText stateEditText;
    private EditText countryEditText;
    private EditText postCodeEditText;
    private EditText customAddressEditText;
    private RadioGroup locationRadioGroup;
    private CheckBox[] typeCheckboxes;
    private GPSTracker objGpsTracker;
    private String selectedTypes = "";
    private ImageLoader imageLoader;

    private ImageView[] photosImageViews;
    private CardView[] deletePhotoImageViews;

    ExecutorService executor = Executors.newFixedThreadPool(4);
    private UploadPhotoThread uploadPhotoThread;
    private EditPhotoThread editPhotoThread;

    File imageFile;
    private String itemUniqueKey;
    private String deletedImageIds = null;
    private LocationManager locationManager;
    private DeleteImagesThread deleteImagesThread;

    public EditItemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ItemDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditItemFragment newInstance(ProductItem item) {
        EditItemFragment fragment = new EditItemFragment();
        Bundle args = new Bundle();

        args.putParcelable("ProductItem", item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mProductItem = getArguments().getParcelable("ProductItem");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_item, container, false);
        objGpsTracker = GPSTracker.getInstance(getContext(), getActivity());
        imageLoader = new ImageLoader(getContext());
        ImageView backImageView = (ImageView) view.findViewById(R.id.imageview_new_item_details_back);
        agegroupSpinner = (Spinner) view.findViewById(R.id.spinner_new_item_agegroup);
        categorySpinner = (Spinner) view.findViewById(R.id.spinner_new_item_category);
        priceEditText = (EditText) view.findViewById(R.id.edit_new_item_price);
        nameEditText = (EditText) view.findViewById(R.id.edit_new_item_name);
        descriptionEditText = (EditText) view.findViewById(R.id.edit_new_item_description);
        TextView toolBarTitle = (TextView) view.findViewById(R.id.toolbar_title);
        toolBarTitle.setText("Edit Item");
        new GetCategoriesTask().execute();
        Calendar currentDate = Calendar.getInstance();

        imageFile = new File(getContext().getCacheDir() + "/" + mFilePath);
        addressEditText = (EditText) view.findViewById(R.id.edit_new_item_address);
        cityEditText = (EditText) view.findViewById(R.id.edit_new_item_city);
        stateEditText = (EditText) view.findViewById(R.id.edit_new_item_state);
        countryEditText = (EditText) view.findViewById(R.id.edit_new_item_country);
        postCodeEditText = (EditText) view.findViewById(R.id.edit_new_item_postcode);
        customAddressEditText = (EditText) view.findViewById(R.id.edit_new_item_custom_address);

        final LinearLayout customAddressLayout = (LinearLayout) view.findViewById(R.id.linear_new_item_custom_address_layout);
        locationRadioGroup = (RadioGroup) view.findViewById(R.id.radiogroup_new_item_location);
        typeCheckboxes = new CheckBox[4];

        typeCheckboxes[0] = (CheckBox) view.findViewById(R.id.check_new_item_all);
        typeCheckboxes[1] = (CheckBox) view.findViewById(R.id.check_new_item_sell);
        typeCheckboxes[2] = (CheckBox) view.findViewById(R.id.check_new_item_swap);
        typeCheckboxes[3] = (CheckBox) view.findViewById(R.id.check_new_item_bid);

        photosImageViews = new ImageView[5];
        photosImageViews[0] = (ImageView) view.findViewById(R.id.image_new_item_photo1);
        photosImageViews[1] = (ImageView) view.findViewById(R.id.image_new_item_photo2);
        photosImageViews[2] = (ImageView) view.findViewById(R.id.image_new_item_photo3);
        photosImageViews[3] = (ImageView) view.findViewById(R.id.image_new_item_photo4);
        photosImageViews[4] = (ImageView) view.findViewById(R.id.image_new_item_photo5);

        deletePhotoImageViews = new CardView[5];
        deletePhotoImageViews[0] = (CardView) view.findViewById(R.id.cardview_new_item_delete1);
        deletePhotoImageViews[1] = (CardView) view.findViewById(R.id.cardview_new_item_delete2);
        deletePhotoImageViews[2] = (CardView) view.findViewById(R.id.cardview_new_item_delete3);
        deletePhotoImageViews[3] = (CardView) view.findViewById(R.id.cardview_new_item_delete4);
        deletePhotoImageViews[4] = (CardView) view.findViewById(R.id.cardview_new_item_delete5);
        Button doneButton = (Button) view.findViewById(R.id.button_new_item_done);
        for (ImageView imageView : photosImageViews) {
            imageView.setOnClickListener(this);
        }
        doneButton.setOnClickListener(this);
        backImageView.setOnClickListener(this);
        doneButton.setText("Update");
        itemUniqueKey = mProductItem.getItemUniqueKey();


        locationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radiobutton_new_item_gps: {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Do you really want to disclose your location?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                        customAddressLayout.setVisibility(View.GONE);
                                        customAddressEditText.setVisibility(View.GONE);
                                        locationManager = (LocationManager) getContext()
                                                .getSystemService(Context.LOCATION_SERVICE);
                                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                                            getActivity().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                                            dialog.cancel();
                                                        }
                                                    })
                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                                            ((RadioButton) view.findViewById(R.id.radiobutton_new_item_custom)).setChecked(true);

                                                            dialog.cancel();
                                                        }
                                                    });
                                            final AlertDialog alert = builder.create();
                                            alert.show();
                                        }
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                        ((RadioButton) view.findViewById(R.id.radiobutton_new_item_custom)).setChecked(true);

                                        dialog.cancel();
                                    }
                                });
                        final AlertDialog alert = builder.create();
                        alert.show();

                        break;
                    }

                    case R.id.radiobutton_new_item_custom: {
                        customAddressLayout.setVisibility(View.VISIBLE);
                        customAddressEditText.setVisibility(View.GONE);
                        addressEditText.setVisibility(View.VISIBLE);
                        cityEditText.setVisibility(View.VISIBLE);
                        stateEditText.setVisibility(View.VISIBLE);
                        countryEditText.setVisibility(View.VISIBLE);
                        postCodeEditText.setVisibility(View.VISIBLE);
                        break;
                    }
                    case R.id.radiobutton_new_item_custom_location: {
                        customAddressLayout.setVisibility(View.GONE);
                        customAddressEditText.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
        });

        if (mProductItem.getIsCustom().equals("0") && objGpsTracker.isGPSEnabled) {
            ((RadioButton) view.findViewById(R.id.radiobutton_new_item_gps)).setChecked(true);

        } else if (mProductItem.getIsCustom().equals("1")) {
            ((RadioButton) view.findViewById(R.id.radiobutton_new_item_custom)).setChecked(true);

            customAddressLayout.setVisibility(View.VISIBLE);
            addressEditText.setText(mProductItem.getItemAddress());
            addressEditText.setMaxLines(4);
            addressEditText.setSingleLine(false);
            cityEditText.setVisibility(View.GONE);
            stateEditText.setVisibility(View.GONE);
            countryEditText.setVisibility(View.GONE);
            postCodeEditText.setVisibility(View.GONE);
        } else {
            ((RadioButton) view.findViewById(R.id.radiobutton_new_item_custom_location)).setChecked(true);
            customAddressEditText.setVisibility(View.VISIBLE);
            customAddressEditText.setText(mProductItem.getItemAddress());
        }

        typeCheckboxes[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int index = 0;
                for (CheckBox checkbox : typeCheckboxes) {
                    if (index != 0) {
                        checkbox.setChecked(isChecked);
                        checkbox.setEnabled(!isChecked);
                    }
                    index++;
                }
            }
        });


        ArrayAdapter<String> agegroupSpinnerAdapter = new ArrayAdapter<String>(
                getContext(),
                R.layout.custom_spinner_item_view,
                getResources().getStringArray(R.array.age_groups)
        );

        agegroupSpinner.setAdapter(agegroupSpinnerAdapter);


        priceEditText.setText(mProductItem.getPrice());
        nameEditText.setText(mProductItem.getProductName());
        descriptionEditText.setText(mProductItem.getDescription());
        int index = 0;
        agegroupSpinner.setSelection(1);
        for (String ageGroup : getResources().getStringArray(R.array.age_groups)) {
            if (ageGroup.contains(mProductItem.getAgeGroup())) {
                agegroupSpinner.setSelection(index);
                break;
            }
            index++;
        }
        if (mProductItem.isAvailableForSwap()) {
            typeCheckboxes[2].setChecked(true);
        }
        if (mProductItem.isAvailableForBid()) {
            typeCheckboxes[3].setChecked(true);
        }
        if (mProductItem.isAvailableForBuy()) {
            typeCheckboxes[1].setChecked(true);
        }
        if (mProductItem.isAvailableForSwap() && mProductItem.isAvailableForBid() && mProductItem.isAvailableForBuy()) {
            typeCheckboxes[0].setChecked(true);
        }
        index = 0;
        for (ItemImage itemImage : mProductItem.getProductImageUrl()) {
            imageLoader.DisplayImage(itemImage.getImageUrl(), photosImageViews[index], true);
            deletePhotoImageViews[index].setVisibility(View.VISIBLE);
            deletePhotoImageViews[index].setOnClickListener(this);
            photosImageViews[index].setTag(R.id.image_id, itemImage.getImageId());
            photosImageViews[index].setTag(R.id.image_is_already_pushed, true);
            index++;
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
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setMessage("Are you sure, You want to cancel");
                        alertDialogBuilder.setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        getFragmentManager().popBackStack();
                                        executor.shutdownNow();
                                    }
                                });

                        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
                return true;
            }
        });
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_new_item_done: {
                if (priceEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter Item Price!", Toast.LENGTH_SHORT).show();
                    priceEditText.requestFocus();
                } else if (nameEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter Item Name!", Toast.LENGTH_SHORT).show();
                    nameEditText.requestFocus();
                } else if (agegroupSpinner.getSelectedItemPosition() == 0) {
                    Toast.makeText(getContext(), "Please select Age Group!", Toast.LENGTH_SHORT).show();
                    agegroupSpinner.requestFocus();
                } else if (categorySpinner.getSelectedItemPosition() == 0) {
                    Toast.makeText(getContext(), "Please select any Category!", Toast.LENGTH_SHORT).show();
                    categorySpinner.requestFocus();
                } else if (locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom && addressEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter Address!", Toast.LENGTH_SHORT).show();
                    addressEditText.requestFocus();
                } else if (cityEditText.getVisibility() == View.VISIBLE && locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom && cityEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter City!", Toast.LENGTH_SHORT).show();
                    cityEditText.requestFocus();
                } else if (stateEditText.getVisibility() == View.VISIBLE && locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom && stateEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter State!", Toast.LENGTH_SHORT).show();
                    stateEditText.requestFocus();
                } else if (countryEditText.getVisibility() == View.VISIBLE && locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom && countryEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter Country!", Toast.LENGTH_SHORT).show();
                    countryEditText.requestFocus();
                } else if (postCodeEditText.getVisibility() == View.VISIBLE && locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom && postCodeEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter Postcode!", Toast.LENGTH_SHORT).show();
                    postCodeEditText.requestFocus();

                } else {
                    for (int index = 1; index < typeCheckboxes.length; index++) {
                        if (typeCheckboxes[index].isChecked()) {
                            if (selectedTypes.isEmpty()) {
                                selectedTypes = typeCheckboxes[index].getTag().toString();
                            } else {
                                selectedTypes = selectedTypes + "," + typeCheckboxes[index].getTag().toString();
                            }
                        }
                    }
                    if (selectedTypes.isEmpty()) {
                        Toast.makeText(getContext(), "Please select any option for the item!", Toast.LENGTH_SHORT).show();
                        typeCheckboxes[0].requestFocus();
                    } else {
                        new saveItemTask().execute();

                    }
                }

                break;
            }
            case R.id.imageview_new_item_details_back: {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage("Are you sure, You want to cancel");
                alertDialogBuilder.setPositiveButton("yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                getFragmentManager().popBackStack();
                                executor.shutdownNow();
                            }
                        });

                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                break;
            }
            case R.id.cardview_new_item_delete1: {
                if ((boolean) photosImageViews[0].getTag(R.id.image_is_already_pushed)) {
                    if (deletedImageIds == null || deletedImageIds.isEmpty()) {
                        deletedImageIds = String.valueOf(photosImageViews[0].getTag(R.id.image_id));
                    } else {
                        deletedImageIds = deletedImageIds + "," + String.valueOf(photosImageViews[0].getTag(R.id.image_id));
                    }
                } else {
                    deleteImagesThread = new DeleteImagesThread(String.valueOf(photosImageViews[0].getTag(R.id.image_id)));
                    executor.execute(deleteImagesThread);
                }
                refreshPhotoImageViewInstances(0);
                break;
            }
            case R.id.cardview_new_item_delete2: {
                if ((boolean) photosImageViews[1].getTag(R.id.image_is_already_pushed)) {
                    if (deletedImageIds == null || deletedImageIds.isEmpty()) {
                        deletedImageIds = String.valueOf(photosImageViews[1].getTag(R.id.image_id));
                    } else {
                        deletedImageIds = deletedImageIds + "," + String.valueOf(photosImageViews[1].getTag(R.id.image_id));
                    }
                } else {
                    deleteImagesThread = new DeleteImagesThread(String.valueOf(photosImageViews[1].getTag(R.id.image_id)));
                    executor.execute(deleteImagesThread);
                }
                refreshPhotoImageViewInstances(1);
                break;
            }
            case R.id.cardview_new_item_delete3: {
                if ((boolean) photosImageViews[2].getTag(R.id.image_is_already_pushed)) {
                    if (deletedImageIds == null || deletedImageIds.isEmpty()) {
                        deletedImageIds = String.valueOf(photosImageViews[2].getTag(R.id.image_id));
                    } else {
                        deletedImageIds = deletedImageIds + "," + String.valueOf(photosImageViews[2].getTag(R.id.image_id));
                    }
                } else {
                    deleteImagesThread = new DeleteImagesThread(String.valueOf(photosImageViews[2].getTag(R.id.image_id)));
                    executor.execute(deleteImagesThread);
                }
                refreshPhotoImageViewInstances(2);

                break;
            }
            case R.id.cardview_new_item_delete4: {
                if ((boolean) photosImageViews[3].getTag(R.id.image_is_already_pushed)) {
                    if (deletedImageIds == null || deletedImageIds.isEmpty()) {
                        deletedImageIds = String.valueOf(photosImageViews[3].getTag(R.id.image_id));
                    } else {
                        deletedImageIds = deletedImageIds + "," + String.valueOf(photosImageViews[3].getTag(R.id.image_id));
                    }
                } else {
                    deleteImagesThread = new DeleteImagesThread(String.valueOf(photosImageViews[3].getTag(R.id.image_id)));
                    executor.execute(deleteImagesThread);
                }
                refreshPhotoImageViewInstances(3);
                break;
            }
            case R.id.cardview_new_item_delete5: {
                if ((boolean) photosImageViews[4].getTag(R.id.image_is_already_pushed)) {
                    if (deletedImageIds == null || deletedImageIds.isEmpty()) {
                        deletedImageIds = String.valueOf(photosImageViews[4].getTag(R.id.image_id));
                    } else {
                        deletedImageIds = deletedImageIds + "," + String.valueOf(photosImageViews[4].getTag(R.id.image_id));
                    }
                } else {
                    deleteImagesThread = new DeleteImagesThread(String.valueOf(photosImageViews[4].getTag(R.id.image_id)));
                    executor.execute(deleteImagesThread);
                }
                refreshPhotoImageViewInstances(4);
                break;
            }
            default: {
                Intent newItemIntent = new Intent(getActivity(), AddItemActivity.class);
                startActivityForResult(newItemIntent, Integer.parseInt(v.getTag().toString()));
                break;
            }
        }
    }

    public void refreshPhotoImageViewInstances(int index) {
        photosImageViews[index].setTag(R.id.image_id, null);
        photosImageViews[index].setTag(R.id.image_is_already_pushed, null);
        photosImageViews[index].setImageResource(R.drawable.icon_photo);
        deletePhotoImageViews[index].setVisibility(View.GONE);
        photosImageViews[index].setClickable(true);
    }

    public void refreshInstancesAfterImageUploaded(int index) {
        photosImageViews[index].setClickable(false);
        deletePhotoImageViews[index].setVisibility(View.VISIBLE);
        deletePhotoImageViews[index].setOnClickListener(EditItemFragment.this);
        photosImageViews[index].setTag(R.id.image_is_already_pushed, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            mFilePath = data.getStringExtra("FilePath");
            imageFile = new File(getContext().getCacheDir() + "/" + mFilePath);

            if (requestCode == Integer.parseInt(photosImageViews[0].getTag().toString())) {
                imageLoader.DisplayImage(mFilePath, photosImageViews[0], true);
                refreshInstancesAfterImageUploaded(0);
                if (photosImageViews[0].getTag(R.id.image_id) != null) {
                    editPhotoThread = new EditPhotoThread(String.valueOf(photosImageViews[0].getTag(R.id.image_id)));
                    executor.execute(editPhotoThread);
                } else {
                    uploadPhotoThread = new UploadPhotoThread(photosImageViews[0]);
                    executor.execute(uploadPhotoThread);
                }
            }
            if (requestCode == Integer.parseInt(photosImageViews[1].getTag().toString())) {
                imageLoader.DisplayImage(mFilePath, photosImageViews[1], true);
                refreshInstancesAfterImageUploaded(1);
                if (photosImageViews[1].getTag(R.id.image_id) != null) {
                    editPhotoThread = new EditPhotoThread(String.valueOf(photosImageViews[1].getTag(R.id.image_id)));
                    executor.execute(editPhotoThread);
                } else {
                    uploadPhotoThread = new UploadPhotoThread(photosImageViews[1]);
                    executor.execute(uploadPhotoThread);
                }


            } else if (requestCode == Integer.parseInt(photosImageViews[2].getTag().toString())) {
                imageLoader.DisplayImage(mFilePath, photosImageViews[2], true);
                refreshInstancesAfterImageUploaded(2);
                if (photosImageViews[2].getTag(R.id.image_id) != null) {
                    editPhotoThread = new EditPhotoThread(String.valueOf(photosImageViews[2].getTag(R.id.image_id)));
                    executor.execute(editPhotoThread);
                } else {
                    uploadPhotoThread = new UploadPhotoThread(photosImageViews[2]);
                    executor.execute(uploadPhotoThread);
                }


            } else if (requestCode == Integer.parseInt(photosImageViews[3].getTag().toString())) {
                imageLoader.DisplayImage(mFilePath, photosImageViews[3], true);
                refreshInstancesAfterImageUploaded(3);
                if (photosImageViews[3].getTag(R.id.image_id) != null) {
                    editPhotoThread = new EditPhotoThread(String.valueOf(photosImageViews[3].getTag(R.id.image_id)));
                    executor.execute(editPhotoThread);
                } else {
                    uploadPhotoThread = new UploadPhotoThread(photosImageViews[3]);
                    executor.execute(uploadPhotoThread);
                }

            } else if (requestCode == Integer.parseInt(photosImageViews[4].getTag().toString())) {
                imageLoader.DisplayImage(mFilePath, photosImageViews[4], true);
                refreshInstancesAfterImageUploaded(4);
                if (photosImageViews[4].getTag(R.id.image_id) != null) {
                    editPhotoThread = new EditPhotoThread(String.valueOf(photosImageViews[4].getTag(R.id.image_id)));
                    executor.execute(editPhotoThread);
                } else {
                    uploadPhotoThread = new UploadPhotoThread(photosImageViews[4]);
                    executor.execute(uploadPhotoThread);
                }

            } else if (requestCode == Constants.LOCATION_CHANGE_SETTINGS) {
                objGpsTracker.resetInstance();
                objGpsTracker = GPSTracker.getInstance(getContext(), getActivity());
            }
        }
    }

    class saveItemTask extends AsyncTask<Void, Void, String> {
        ProgressDialog objProgressDialog;

        @Override
        protected void onPreExecute() {
            if (locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_gps) {
                objGpsTracker.getLocation();
            }
            objProgressDialog = new ProgressDialog(getContext());
            objProgressDialog.setMessage("Please wait..");
            objProgressDialog.setCanceledOnTouchOutside(false);
            objProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject postItemJson = new JSONObject();
            try {
                postItemJson.put("task", "editItem");
                postItemJson.put("category_id", ((Category) categorySpinner.getSelectedItem()).getCategoryId());
                postItemJson.put("item_name", nameEditText.getText().toString());
                postItemJson.put("item_id", mProductItem.getProductId());
                postItemJson.put("item_description", descriptionEditText.getText().toString());
                postItemJson.put("item_price", priceEditText.getText().toString());
                if (locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_gps) {
                    postItemJson.put("lattitude", String.valueOf(objGpsTracker.getLatitude()));
                    postItemJson.put("longitude", String.valueOf(objGpsTracker.getLongitude()));
                } else {
                    postItemJson.put("lattitude", 0);
                    postItemJson.put("longitude", 0);
                }
                postItemJson.put("is_custom", locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_gps ? "0" : locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom ? "1" : "2");

                postItemJson.put("item_for", selectedTypes);
                postItemJson.put("image_unique_key", itemUniqueKey);
                postItemJson.put("address", addressEditText.getText().toString());
                postItemJson.put("city", cityEditText.getText().toString());
                postItemJson.put("state", stateEditText.getText().toString());
                postItemJson.put("country", countryEditText.getText().toString());
                postItemJson.put("postcode", postCodeEditText.getText().toString());
                postItemJson.put("custom_address", customAddressEditText.getText().toString());
                postItemJson.put("deleted_image_ids", deletedImageIds);
                postItemJson.put("usertimeZone", TimeZone.getDefault().getID());
                postItemJson.put("user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                postItemJson.put("age_group_id", getResources().getIntArray(R.array.age_groups_id)[agegroupSpinner.getSelectedItemPosition()]);

            } catch (Exception e) {
                e.printStackTrace();
                Utils.log(e.getMessage());
            }

            Connection objConnection = new Connection();
            String response = objConnection.getResponseFromWebservice(Constants.POST_ITEM, postItemJson);
            Log.i("Abhi", "Response=" + response);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);

                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Updated Item")) {
                    Toast.makeText(getContext(), "Item Updated Successfully!", Toast.LENGTH_SHORT).show();
                    getActivity().getFragmentManager().popBackStack();
                    ((DashboardActivity) getActivity()).backPressedFromEditItem();
                    executor.shutdownNow();

                } else if (jsonObj.getString("status_message").equalsIgnoreCase("No Record Found")) {

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

    public class UploadPhotoThread implements Runnable {
        ImageView photoImageView;

        public UploadPhotoThread(ImageView photoImageView) {
            this.photoImageView = photoImageView;
        }

        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();
            try {

                byte[] byteArray = Utils.convertFileToByteArray(imageFile);

                String encodedProfileImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                fetchItemsJson.put("task", "addItemImage");
                fetchItemsJson.put("item_image", encodedProfileImage);
                fetchItemsJson.put("image_name", mFilePath);
                fetchItemsJson.put("image_unique_key", itemUniqueKey);

                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.POST_ITEM, fetchItemsJson);
                final JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Added Image")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                photoImageView.setTag(R.id.image_id, jsonObj.getJSONObject("data").getString("image_id"));
                            } catch (JSONException exc) {

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

    public class EditPhotoThread implements Runnable {
        String imageId;

        public EditPhotoThread(String imageId) {
            this.imageId = imageId;
        }

        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();
            try {
                byte[] byteArray = Utils.convertFileToByteArray(imageFile);

                String encodedProfileImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                fetchItemsJson.put("task", "editItemImage");
                fetchItemsJson.put("item_image", encodedProfileImage);
                fetchItemsJson.put("image_name", mFilePath);
                fetchItemsJson.put("item_image_id", imageId);

                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.POST_ITEM, fetchItemsJson);
                JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Updated Image")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }
            } catch (Exception e) {
                Utils.log(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    class GetCategoriesTask extends AsyncTask<Void, Void, String> {
        ProgressDialog objProgressDialog;

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
                Category[] categoryList;
                int selectedIndex = 0;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Record Found")) {
                    categoriesJsonArray = jsonObj.getJSONArray("data");
                    categoryList = new Category[categoriesJsonArray.length() + 1];
                    Category category = new Category();
                    category.setCategoryName("Select Category");
                    category.setCategoryId("0");
                    categoryList[0] = category;

                    for (int index = 0; index < categoriesJsonArray.length(); index++) {
                        category = new Category();
                        categoryJsonObject = categoriesJsonArray.getJSONObject(index);
                        category.setCategoryName(categoryJsonObject.getString("category_name"));
                        category.setCategoryId(categoryJsonObject.getString("category_id"));
                        categoryList[index + 1] = category;
                        if (mProductItem.getCategoryName().equals(category.getCategoryName())) {
                            selectedIndex = index + 1;
                        }
                    }
                    CategoriesSpinnerAdapter adapter = new CategoriesSpinnerAdapter(getContext(),
                            R.layout.custom_spinner_item_view,
                            categoryList);
                    categorySpinner.setAdapter(adapter);
                    categorySpinner.setSelection(selectedIndex);
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Network Failure,please try again", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                objProgressDialog.cancel();
            }
            super.onPostExecute(result);
        }
    }

    public class DeleteImagesThread implements Runnable {

        private String imageId;

        public DeleteImagesThread(String imageId) {
            this.imageId = imageId;
        }

        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();
            try {

                fetchItemsJson.put("task", "deleteItemimage");
                fetchItemsJson.put("image_id", imageId);

                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.POST_ITEM, fetchItemsJson);
                JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        ) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

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