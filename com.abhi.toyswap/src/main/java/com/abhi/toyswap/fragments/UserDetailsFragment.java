package com.abhi.toyswap.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.R;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.GPSTracker;
import com.abhi.toyswap.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UserDetailsFragment extends Fragment implements View.OnClickListener {

    private ProgressDialog objProgressDialog;
    private RelativeLayout likesLayout;
    private RelativeLayout dislikesLayout;
    private TextView likesCountTextView;
    private TextView dislikesCountTextView;
    private EditText passwordEditText;
    private EditText firstnameEditText;
    private EditText lastnameEdidText;
    private ImageView usersProfileImageView;
    private Button updateButton;
    private EditText emailAddressTextView;
    TextView usersLocationTextView;
    private GPSTracker objGpsTracker;
    ExecutorService executor = Executors.newFixedThreadPool(4);
    private View view;
    private ImageLoader imageLoader;
    private String encodedProfileImage = null;
    Bitmap bitmap;


    public static UserDetailsFragment newInstance() {
        UserDetailsFragment fragment = new UserDetailsFragment();

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

            view = inflater.inflate(R.layout.fragment_user_details, container, false);
            ImageView backImageView=(ImageView)view.findViewById(R.id.imageview_user_details_back);
            likesLayout = (RelativeLayout) view.findViewById(R.id.relative_user_details_likes_layout);
            dislikesLayout = (RelativeLayout) view.findViewById(R.id.relative_user_details_dislikes_layout);
            likesCountTextView = (TextView) view.findViewById(R.id.text_user_details_number_of_likes);
            dislikesCountTextView = (TextView) view.findViewById(R.id.text_user_details_number_of_dislikes);
            firstnameEditText = (EditText) view.findViewById(R.id.text_user_details_firstname);
            lastnameEdidText = (EditText) view.findViewById(R.id.text_user_details_lastname);
            passwordEditText = (EditText) view.findViewById(R.id.text_user_details_password_value);
            usersProfileImageView = (ImageView) view.findViewById(R.id.image_user_details_user_photo);
            updateButton = (Button) view.findViewById(R.id.button_user_details_update);
            usersLocationTextView = (TextView) view.findViewById(R.id.text_user_details_location);
            emailAddressTextView = (EditText) view.findViewById(R.id.text_user_details_email_value);
            emailAddressTextView.setText(Utils.getDataFromSharedPreferences(getActivity(), "EmailAddress"));
            usersLocationTextView.setVisibility(View.GONE);
            imageLoader.DisplayImage(Utils.getDataFromSharedPreferences(getActivity(), "UserImage"), usersProfileImageView, true);

            firstnameEditText.setText(Utils.getDataFromSharedPreferences(getActivity(), "FirstName"));
            lastnameEdidText.setText(Utils.getDataFromSharedPreferences(getActivity(), "LastName"));


            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        if (firstnameEditText.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), getString(R.string.registration_empty_firstname), Toast.LENGTH_SHORT).show();
                            firstnameEditText.requestFocus();
                        } else if (lastnameEdidText.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), getString(R.string.registration_empty_lastname), Toast.LENGTH_SHORT).show();
                            lastnameEdidText.requestFocus();
                        } else if (emailAddressTextView.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), getString(R.string.registration_empty_email), Toast.LENGTH_SHORT).show();
                            emailAddressTextView.requestFocus();
                        } else if (!Utils.validate(emailAddressTextView.getText().toString())) {
                            Toast.makeText(getActivity(), getString(R.string.registration_invalid_email), Toast.LENGTH_SHORT).show();
                            emailAddressTextView.requestFocus();
                        } else {
                            new UpdateUserDetailsTask().execute();

                        }
                }
            });
            backImageView.setOnClickListener(this);

            usersProfileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    (CropImage.activity().setGuidelines(CropImageView.Guidelines.OFF)).setAllowFlipping(false).setAllowRotation(false).setAutoZoomEnabled(false).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(4, 4).setFixAspectRatio(true).setMinCropResultSize(2000, 2000).setMaxCropResultSize(2000, 2000)
                            .start(getContext(), UserDetailsFragment.this);
                }
            });
            objGpsTracker = GPSTracker.getInstance(getContext(), getActivity());

            GetLikesDislikesThread getLikesDislikesThread = new GetLikesDislikesThread();
            executor.execute(getLikesDislikesThread);

        }
        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageview_user_details_back:{
                getFragmentManager().popBackStack();
                executor.shutdownNow();
                break;
            }

        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
       // super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {

            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE: {

                    if (resultCode == getActivity().RESULT_OK) {
                        objProgressDialog = new ProgressDialog(getActivity());
                        objProgressDialog.setMessage("Please wait..");
                        objProgressDialog.setCanceledOnTouchOutside(false);
                        objProgressDialog.show();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final CropImage.ActivityResult result = CropImage.getActivityResult(data);

                                    Uri resultUri = result.getUri();

                                     bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);

                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 75, byteArrayOutputStream);
                                    byte[] byteArray = byteArrayOutputStream.toByteArray();

                                    encodedProfileImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                                } catch (IOException e) {
                                    e.printStackTrace();

                                } finally {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            objProgressDialog.cancel();
                                            usersProfileImageView.setImageBitmap(bitmap);

                                        }
                                    });
                                }
                            }
                        });

                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    }
                    break;
                }
            }
        }
    }

    class UpdateUserDetailsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            objProgressDialog = new ProgressDialog(getActivity());
            objProgressDialog.setMessage("Please wait..");
            objProgressDialog.setCanceledOnTouchOutside(false);
            objProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject registrationJson = new JSONObject();
            String response=null;
            try {

                registrationJson.put("task", "editUser");
                registrationJson.put("first_name", firstnameEditText.getText().toString());
                registrationJson.put("last_name", lastnameEdidText.getText().toString());
                registrationJson.put("email", emailAddressTextView.getText().toString());
                if (!passwordEditText.getText().toString().isEmpty()) {
                    registrationJson.put("password", passwordEditText.getText().toString());
                }
                registrationJson.put("address", "");
                registrationJson.put("city", "");
                registrationJson.put("state", "");
                registrationJson.put("country", "");
                registrationJson.put("birth_date", "");
                registrationJson.put("gender", "");
                registrationJson.put("member_type", "free");
                registrationJson.put("lattitude", objGpsTracker.getLatitude());
                registrationJson.put("longitude", objGpsTracker.getLongitude());
                registrationJson.put("device_type", "android");
                registrationJson.put("user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                registrationJson.put("device_key", Utils.getDeviceId(getActivity(), getActivity()));
                if (encodedProfileImage != null) {
                    registrationJson.put("image_name", firstnameEditText.getText().toString() + lastnameEdidText.getText().toString() + ".jpg");

                    registrationJson.put("user_image", encodedProfileImage);
                }



            Connection objConnection = new Connection();
             response = objConnection.getResponseFromWebservice(Constants.LOGIN, registrationJson);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);

                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("User Updated")) {
                    JSONObject responseJsonObject = jsonObj.getJSONObject("data");
                    Utils.saveDataIntoSharedPreferences(getActivity(), "Name", firstnameEditText.getText().toString() + " " + lastnameEdidText.getText().toString());
                    Utils.saveDataIntoSharedPreferences(getActivity(), "FirstName", firstnameEditText.getText().toString());
                    Utils.saveDataIntoSharedPreferences(getActivity(), "LastName",lastnameEdidText.getText().toString());
                    try {
                        Utils.saveDataIntoSharedPreferences(getActivity(), "UserImage", responseJsonObject.getString("user_image"));
                    }catch (Exception e){

                    }
                    Utils.saveDataIntoSharedPreferences(getActivity(), "EmailAddress", emailAddressTextView.getText().toString());


                    Toast.makeText(getActivity(), "User Details Updated Successfull!", Toast.LENGTH_LONG).show();

                    getActivity().getSupportFragmentManager().popBackStack();
                    executor.shutdownNow();
                } else if (jsonObj.getString("status").equals("400")) {

                    Toast.makeText(getActivity(), jsonObj.getString("status_message"), Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(getActivity(), "Network Failure,please try again", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                objProgressDialog.cancel();
            }
            super.onPostExecute(result);
        }

    }

}