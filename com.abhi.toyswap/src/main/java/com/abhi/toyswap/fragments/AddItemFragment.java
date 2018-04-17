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
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.Models.Category;
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
 * Use the {@link AddItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddItemFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private View view;
    private String mFilePath;
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
    private EditText customAddrssEditText;
    private RadioGroup locationRadioGroup;
    private CheckBox[] typeCheckboxes;
    private GPSTracker objGpsTracker;
    private String selectedTypes = "";
    private ImageLoader imageLoader;
    private ImageView[] photosImageViews;

    ExecutorService executor = Executors.newFixedThreadPool(4);
    private UploadPhotoThread uploadPhotoThread;
    File imageFile;
    private String itemUniqueKey;
    private boolean isFromGallery;
    private int imagesUploaded = 0;
    //private String ImageIdsUploaded = "";
    private CardView[] deletePhotoImageViews;
    private DeleteImagesThread deleteImagesThread;
    private LocationManager locationManager;

    public AddItemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ItemDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddItemFragment newInstance(String filePath) {
        AddItemFragment fragment = new AddItemFragment();
        Bundle args = new Bundle();

        args.putString("FilePath", filePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mFilePath = getArguments().getString("FilePath");
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
        customAddrssEditText = (EditText) view.findViewById(R.id.edit_new_item_custom_address);
        descriptionEditText = (EditText) view.findViewById(R.id.edit_new_item_description);
        new GetCategoriesTask().execute();
        Calendar currentDate = Calendar.getInstance();

        itemUniqueKey = "Item_" + currentDate.get(Calendar.HOUR_OF_DAY) + currentDate.get(Calendar.MINUTE) + currentDate.get(Calendar.SECOND) + currentDate.get(Calendar.MILLISECOND) + currentDate.get(Calendar.DATE) + currentDate.get(Calendar.MONTH) + currentDate.get(Calendar.YEAR);
        imageFile = new File(getContext().getCacheDir() + "/" + mFilePath);
        addressEditText = (EditText) view.findViewById(R.id.edit_new_item_address);
        cityEditText = (EditText) view.findViewById(R.id.edit_new_item_city);
        stateEditText = (EditText) view.findViewById(R.id.edit_new_item_state);
        countryEditText = (EditText) view.findViewById(R.id.edit_new_item_country);
        postCodeEditText = (EditText) view.findViewById(R.id.edit_new_item_postcode);
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

        imageLoader.DisplayImage(mFilePath, photosImageViews[0], true);
        Button doneButton = (Button) view.findViewById(R.id.button_new_item_done);
        for (ImageView imageView : photosImageViews) {
            imageView.setOnClickListener(this);
        }
        refreshInstancesAfterImageUploaded(0);
        backImageView.setOnClickListener(this);

        doneButton.setOnClickListener(this);
        uploadPhotoThread = new UploadPhotoThread(photosImageViews[0]);
        executor.execute(uploadPhotoThread);
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
                                        customAddrssEditText.setVisibility(View.GONE);
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
                        customAddrssEditText.setVisibility(View.GONE);

                        customAddressLayout.setVisibility(View.VISIBLE);

                        break;
                    }
                    case R.id.radiobutton_new_item_custom_location: {
                        customAddrssEditText.setVisibility(View.VISIBLE);
                        customAddressLayout.setVisibility(View.GONE);

                    }
                }
            }
        });
        typeCheckboxes[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    typeCheckboxes[1].setChecked(true);
                    typeCheckboxes[2].setChecked(true);
                    typeCheckboxes[3].setChecked(true);
                    typeCheckboxes[1].setEnabled(false);
                    typeCheckboxes[2].setEnabled(false);
                    typeCheckboxes[3].setEnabled(false);
                } else {
                    typeCheckboxes[1].setEnabled(true);
                    typeCheckboxes[2].setEnabled(true);
                    typeCheckboxes[3].setEnabled(true);
                    typeCheckboxes[1].setChecked(false);
                    typeCheckboxes[2].setChecked(false);
                    typeCheckboxes[3].setChecked(false);

                }
            }
        });


        ArrayAdapter<String> agegroupSpinnerAdapter = new ArrayAdapter<String>(
                getContext(),
                R.layout.custom_spinner_item_view,
                getResources().getStringArray(R.array.age_groups)
        );

        agegroupSpinner.setAdapter(agegroupSpinnerAdapter);

        if (!objGpsTracker.isGPSEnabled) {
            ((RadioButton) view.findViewById(R.id.radiobutton_new_item_custom)).setChecked(true);
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
                                        if (imagesUploaded != 0) {

                                            DeleteImagesThread deleteImagesThread = new DeleteImagesThread();
                                            executor.execute(deleteImagesThread);

                                        } else {
                                            getActivity().getFragmentManager().popBackStack();
                                            ((DashboardActivity) getActivity()).backPressedFromAddNewItem();
                                            executor.shutdownNow();
                                        }
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

    public void refreshPhotoImageViewInstances(int index) {
        photosImageViews[index].setTag(R.id.image_id, null);
        photosImageViews[index].setTag(R.id.image_is_already_pushed, null);
        photosImageViews[index].setImageResource(R.drawable.icon_photo);
        deletePhotoImageViews[index].setVisibility(View.GONE);
        photosImageViews[index].setClickable(true);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_new_item_details_back: {
                getFragmentManager().popBackStack();
                executor.shutdownNow();
                break;
            }
            case R.id.cardview_new_item_delete1: {

                deleteImagesThread = new DeleteImagesThread(String.valueOf(photosImageViews[0].getTag(R.id.image_id)));
                executor.execute(deleteImagesThread);

                refreshPhotoImageViewInstances(0);
                break;
            }
            case R.id.cardview_new_item_delete2: {

                deleteImagesThread = new DeleteImagesThread(String.valueOf(photosImageViews[1].getTag(R.id.image_id)));
                executor.execute(deleteImagesThread);

                refreshPhotoImageViewInstances(1);
                break;
            }
            case R.id.cardview_new_item_delete3: {
                deleteImagesThread = new DeleteImagesThread(String.valueOf(photosImageViews[2].getTag(R.id.image_id)));
                executor.execute(deleteImagesThread);
                refreshPhotoImageViewInstances(2);
                break;
            }
            case R.id.cardview_new_item_delete4: {
                deleteImagesThread = new DeleteImagesThread(String.valueOf(photosImageViews[3].getTag(R.id.image_id)));
                executor.execute(deleteImagesThread);
                refreshPhotoImageViewInstances(3);
                break;
            }
            case R.id.cardview_new_item_delete5: {
                deleteImagesThread = new DeleteImagesThread(String.valueOf(photosImageViews[4].getTag(R.id.image_id)));
                executor.execute(deleteImagesThread);
                refreshPhotoImageViewInstances(4);
                break;
            }
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
                } else if (imagesUploaded == 0) {
                    Toast.makeText(getContext(), "Image uploading in Progress,Please try again later!", Toast.LENGTH_SHORT).show();
                } else if (locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom && addressEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter Address!", Toast.LENGTH_SHORT).show();
                    addressEditText.requestFocus();
                } else if (locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom && cityEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter City!", Toast.LENGTH_SHORT).show();
                    cityEditText.requestFocus();
                } else if (locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom && stateEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter State!", Toast.LENGTH_SHORT).show();
                    stateEditText.requestFocus();
                } else if (locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom && countryEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter Country!", Toast.LENGTH_SHORT).show();
                    countryEditText.requestFocus();
                } else if (locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom && postCodeEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter Postcode!", Toast.LENGTH_SHORT).show();
                    postCodeEditText.requestFocus();

                } else if (locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom_location && customAddrssEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter Location!", Toast.LENGTH_SHORT).show();
                    customAddrssEditText.requestFocus();
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
            case R.id.image_new_item_photo1: {
                Intent newItemIntent = new Intent(getActivity(), AddItemActivity.class);
                newItemIntent.putExtra("IsPrimaryImage", true);
                startActivityForResult(newItemIntent, Integer.parseInt(v.getTag().toString()));
                break;
            }
            case R.id.image_new_item_photo2: {
                Intent newItemIntent = new Intent(getActivity(), AddItemActivity.class);
                startActivityForResult(newItemIntent, Integer.parseInt(v.getTag().toString()));
                break;
            }
            case R.id.image_new_item_photo3: {
                Intent newItemIntent = new Intent(getActivity(), AddItemActivity.class);
                startActivityForResult(newItemIntent, Integer.parseInt(v.getTag().toString()));
                break;
            }
            case R.id.image_new_item_photo4: {
                Intent newItemIntent = new Intent(getActivity(), AddItemActivity.class);
                startActivityForResult(newItemIntent, Integer.parseInt(v.getTag().toString()));
                break;
            }
            case R.id.image_new_item_photo5: {
                Intent newItemIntent = new Intent(getActivity(), AddItemActivity.class);
                startActivityForResult(newItemIntent, Integer.parseInt(v.getTag().toString()));
                break;
            }
        }
    }

    public void refreshInstancesAfterImageUploaded(int index) {
        photosImageViews[index].setClickable(false);
        deletePhotoImageViews[index].setVisibility(View.VISIBLE);
        deletePhotoImageViews[index].setOnClickListener(AddItemFragment.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //   super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            mFilePath = data.getStringExtra("FilePath");
            // imageFile = new File(Environment.getExternalStorageDirectory() + "/ToyApp/" + mFilePath);
            imageFile = new File(getContext().getCacheDir() + "/" + mFilePath);
            if (requestCode == Integer.parseInt(photosImageViews[0].getTag().toString())) {
                imageLoader.DisplayImage(mFilePath, photosImageViews[0], true);
                refreshInstancesAfterImageUploaded(0);
                uploadPhotoThread = new UploadPhotoThread(photosImageViews[0]);
                executor.execute(uploadPhotoThread);


            } else if (requestCode == Integer.parseInt(photosImageViews[1].getTag().toString())) {
                imageLoader.DisplayImage(mFilePath, photosImageViews[1], true);
                refreshInstancesAfterImageUploaded(1);
                uploadPhotoThread = new UploadPhotoThread(photosImageViews[1]);
                executor.execute(uploadPhotoThread);


            } else if (requestCode == Integer.parseInt(photosImageViews[2].getTag().toString())) {
                imageLoader.DisplayImage(mFilePath, photosImageViews[2], true);
                refreshInstancesAfterImageUploaded(2);
                uploadPhotoThread = new UploadPhotoThread(photosImageViews[2]);
                executor.execute(uploadPhotoThread);

            } else if (requestCode == Integer.parseInt(photosImageViews[3].getTag().toString())) {
                imageLoader.DisplayImage(mFilePath, photosImageViews[3], true);
                refreshInstancesAfterImageUploaded(3);
                uploadPhotoThread = new UploadPhotoThread(photosImageViews[3]);
                executor.execute(uploadPhotoThread);

            } else if (requestCode == Integer.parseInt(photosImageViews[4].getTag().toString())) {
                imageLoader.DisplayImage(mFilePath, photosImageViews[4], true);
                refreshInstancesAfterImageUploaded(4);
                uploadPhotoThread = new UploadPhotoThread(photosImageViews[4]);
                executor.execute(uploadPhotoThread);

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

                postItemJson.put("task", "addItem");
                postItemJson.put("category_id", ((Category) categorySpinner.getSelectedItem()).getCategoryId());
                postItemJson.put("item_name", nameEditText.getText().toString());
                postItemJson.put("item_description", descriptionEditText.getText().toString());
                postItemJson.put("item_price", priceEditText.getText().toString());
                postItemJson.put("lattitude", String.valueOf(objGpsTracker.getLatitude()));
                postItemJson.put("longitude", String.valueOf(objGpsTracker.getLongitude()));
                postItemJson.put("is_custom", locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_gps ? "0" : locationRadioGroup.getCheckedRadioButtonId() == R.id.radiobutton_new_item_custom ? "1" : "2");
                postItemJson.put("item_for", selectedTypes);
                postItemJson.put("image_unique_key", itemUniqueKey);
                postItemJson.put("custom_address", customAddrssEditText.getText().toString());

                postItemJson.put("address", addressEditText.getText().toString());
                postItemJson.put("city", cityEditText.getText().toString());
                postItemJson.put("state", stateEditText.getText().toString());
                postItemJson.put("country", countryEditText.getText().toString());
                postItemJson.put("postcode", postCodeEditText.getText().toString());

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
                        && jsonObj.getString("status_message").equalsIgnoreCase("Added Item")) {

                    getActivity().getSupportFragmentManager().popBackStack();
                    ((DashboardActivity) getActivity()).callItemAddedSuccessfullyScreen();

                } else if (jsonObj.getString("status").equalsIgnoreCase("500") || jsonObj.getString("status").equalsIgnoreCase("400")) {
                    Toast.makeText(getContext(), jsonObj.getString("status_message"), Toast.LENGTH_SHORT).show();

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

    public class DeleteImagesThread implements Runnable {
        private String imageId = null;

        public DeleteImagesThread() {
        }

        public DeleteImagesThread(String imageId) {
            this.imageId = imageId;
        }

        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();
            try {

                fetchItemsJson.put("task", "deleteItemimage");
                if (imageId == null) {
                    fetchItemsJson.put("image_unique_key", itemUniqueKey);
                } else {
                    fetchItemsJson.put("image_id", imageId);

                }

                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.POST_ITEM, fetchItemsJson);
                JSONObject jsonObj = new JSONObject(response);
                if (imageId == null) {
                    if (jsonObj.getString("status").equals("200")
                            ) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().getFragmentManager().popBackStack();
                                ((DashboardActivity) getActivity()).backPressedFromAddNewItem();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Utils.log(e.getMessage());
                e.printStackTrace();
            }
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
                            imagesUploaded++;
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

                    }


                    CategoriesSpinnerAdapter adapter = new CategoriesSpinnerAdapter(getContext(),
                            R.layout.custom_spinner_item_view,
                            categoryList);
                    categorySpinner.setAdapter(adapter);
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
}