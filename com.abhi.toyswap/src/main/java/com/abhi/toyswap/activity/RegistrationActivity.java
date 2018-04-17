package com.abhi.toyswap.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.R;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.GPSTracker;
import com.abhi.toyswap.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


public class RegistrationActivity extends ActionBarActivity {
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText profileImageTextView;
    private TextView signInTextView;
    private Button registerButton;
    private ProgressDialog objProgressDialog;
    private CheckBox agreemeCheckBox;
    private TextView agreemeTextView;
    private String imagePath;
    private File imageFile;
    private Uri imageUri;
    private String encodedProfileImage = null;
    private static final int MY_PERMISSIONS_REQUEST_GET_LOCATION = 1;
    private GPSTracker objGpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_registration);

        firstNameEditText = (EditText) this.findViewById(R.id.edit_registration_firstname);
        lastNameEditText = (EditText) this.findViewById(R.id.edit_registration_lastname);
        emailEditText = (EditText) this.findViewById(R.id.edit_registration_email);
        signInTextView = (TextView) this.findViewById(R.id.text_registration_signin);
        agreemeCheckBox = (CheckBox) this.findViewById(R.id.check_registration_agreeme);
        agreemeTextView = (TextView) this.findViewById(R.id.text_registration_agreeme);

        SpannableString ss = new SpannableString(Html.fromHtml(getString(R.string.agreeme)));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RegistrationActivity.this);
                alertDialogBuilder.setMessage(getString(R.string.terms_conditions));
                alertDialogBuilder.setTitle("Terms & Conditions");
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        ss.setSpan(clickableSpan, 32, 52, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        agreemeTextView.setText(ss);
        agreemeTextView.setMovementMethod(LinkMovementMethod.getInstance());
        agreemeTextView.setHighlightColor(Color.TRANSPARENT);
        passwordEditText = (EditText) this.findViewById(R.id.edit_registration_password);
        profileImageTextView = (EditText) this.findViewById(R.id.text_registration_profileimage);
        registerButton = (Button) this.findViewById(R.id.button_registration_register);
        objGpsTracker = GPSTracker.getInstance(this, RegistrationActivity.this);

        signInTextView.setText(Html.fromHtml(String.format(getString(R.string.registration_signin))));
        signInTextView.setPaintFlags(signInTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        profileImageTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= profileImageTextView.getRight() - profileImageTextView.getTotalPaddingRight()) {

                        (CropImage.activity().setGuidelines(CropImageView.Guidelines.OFF)).setAllowFlipping(false).setAllowRotation(false).setAutoZoomEnabled(false).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(4, 4).setFixAspectRatio(true).setMinCropResultSize(2000, 2000).setMaxCropResultSize(2000, 2000)
                                .start(RegistrationActivity.this);

                    }
                    return true;
                }

                return true;
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstNameEditText.getText().toString().isEmpty()) {
                    Toast.makeText(RegistrationActivity.this, getString(R.string.registration_empty_firstname), Toast.LENGTH_SHORT).show();
                    firstNameEditText.requestFocus();
                } else if (lastNameEditText.getText().toString().isEmpty()) {
                    Toast.makeText(RegistrationActivity.this, getString(R.string.registration_empty_lastname), Toast.LENGTH_SHORT).show();
                    lastNameEditText.requestFocus();
                } else if (emailEditText.getText().toString().isEmpty()) {
                    Toast.makeText(RegistrationActivity.this, getString(R.string.registration_empty_email), Toast.LENGTH_SHORT).show();
                    emailEditText.requestFocus();
                } else if (!Utils.validate(emailEditText.getText().toString())) {
                    Toast.makeText(RegistrationActivity.this, getString(R.string.registration_invalid_email), Toast.LENGTH_SHORT).show();
                    emailEditText.requestFocus();
                } else if (passwordEditText.getText().toString().isEmpty()) {
                    Toast.makeText(RegistrationActivity.this, getString(R.string.registration_empty_password), Toast.LENGTH_SHORT).show();
                    passwordEditText.requestFocus();
                } else if (!agreemeCheckBox.isChecked()) {
                    Toast.makeText(RegistrationActivity.this, getString(R.string.registration_agreement_error), Toast.LENGTH_SHORT).show();
                    agreemeCheckBox.requestFocus();
                } else {
                    new RegistrationTask().execute();
                }
            }
        });
    }


    class RegistrationTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            objProgressDialog = new ProgressDialog(RegistrationActivity.this);
            objProgressDialog.setMessage("Please wait..");
            objProgressDialog.setCanceledOnTouchOutside(false);
            objProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject registrationJson = new JSONObject();
            try {

                registrationJson.put("task", "addUser");
                registrationJson.put("first_name", firstNameEditText.getText().toString());
                registrationJson.put("last_name", lastNameEditText.getText().toString());
                registrationJson.put("email", emailEditText.getText().toString());
                registrationJson.put("password", passwordEditText.getText().toString());
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
                registrationJson.put("device_token", Utils.getDataFromSharedPreferences(RegistrationActivity.this, "DeviceToken"));

                registrationJson.put("device_key", Utils.getDeviceId(RegistrationActivity.this, RegistrationActivity.this));
                if (encodedProfileImage != null) {
                    registrationJson.put("image_name", firstNameEditText.getText().toString() + lastNameEditText.getText().toString() + ".jpg");

                    registrationJson.put("user_image", encodedProfileImage);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Utils.log(e.getMessage());
            }

            Connection objConnection = new Connection();
            String response = objConnection.getResponseFromWebservice(Constants.REGISTER, registrationJson);

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                Utils.log(result);
                JSONObject jsonObj = new JSONObject(result);

                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Added User")) {
                    JSONObject responseJsonObject = jsonObj.getJSONObject("data");

                    Utils.saveBooleanIntoSharedPreferences(RegistrationActivity.this, "IsUserLoggedIn", true);
                    Utils.saveDataIntoSharedPreferences(RegistrationActivity.this, "UserId", "" + responseJsonObject.getInt("user_id"));
                    Utils.saveDataIntoSharedPreferences(RegistrationActivity.this, "EmailAddress", emailEditText.getText().toString());
                    Utils.saveDataIntoSharedPreferences(RegistrationActivity.this, "Password", passwordEditText.getText().toString());

                    Utils.saveDataIntoSharedPreferences(RegistrationActivity.this, "Name", (firstNameEditText.getText().toString() + " " + lastNameEditText.getText().toString()));
                    Utils.saveDataIntoSharedPreferences(RegistrationActivity.this, "FirstName", (firstNameEditText.getText().toString()));
                    Utils.saveDataIntoSharedPreferences(RegistrationActivity.this, "LastName", (lastNameEditText.getText().toString()));
                    try {
                        Utils.saveDataIntoSharedPreferences(RegistrationActivity.this, "UserImage", responseJsonObject.getString("user_image"));
                    } catch (Exception e) {

                    }

                    Toast.makeText(RegistrationActivity.this, "User Added Successfully!", Toast.LENGTH_LONG).show();
                    Intent dashboardIntent = new Intent(RegistrationActivity.this, DashboardActivity.class);
                    dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(dashboardIntent);
                    //setResult(5);
                    finish();
                } else if (jsonObj.getString("status").equals("400")) {
                    passwordEditText.setText("");
                    Toast.makeText(RegistrationActivity.this, jsonObj.getString("status_message"), Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(RegistrationActivity.this, "Network Failure,please try again", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                Utils.writeLogIntoFile("REgistration Error:"+e.getMessage());
                Utils.writeLogIntoFile("REgistration Error Response From API:"+result);
            } finally {
                objProgressDialog.cancel();
            }
            super.onPostExecute(result);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE: {

                    if (resultCode == RESULT_OK) {
                        objProgressDialog = new ProgressDialog(RegistrationActivity.this);
                        objProgressDialog.setMessage("Please wait..");
                        objProgressDialog.setCanceledOnTouchOutside(false);
                        objProgressDialog.show();

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final CropImage.ActivityResult result = CropImage.getActivityResult(data);

                                    Uri resultUri = result.getUri();

                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(RegistrationActivity.this.getContentResolver(), resultUri);

                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                    byte[] byteArray = byteArrayOutputStream.toByteArray();

                                    encodedProfileImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                                    Log.i("Abhi", "Result OK 2");
                                } catch (IOException e) {
                                    e.printStackTrace();

                                } finally {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            objProgressDialog.cancel();

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

}
