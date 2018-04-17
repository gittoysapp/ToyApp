package com.abhi.toyswap.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.R;
import com.abhi.toyswap.fragments.ItemDetailsFragment;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.Utils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LoginActivity extends ActionBarActivity implements View.OnClickListener {
    private EditText emailIdEditText;
    private EditText passwordEditText;
    private TextView forgotPasswordTextView;
    private TextView registerTextView;
    private Button loginButton;
    private ProgressDialog objProgressDialog;
    private ImageView googleSignImageView;
    private ImageView facebookSignInImageView;
    private CheckBox rememberMeCheckbox;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 25;
    private boolean isSigninFromGoogle;
    private boolean isSigninFromFacebook;
    private GoogleSignInAccount googleSignInAccount;
    private CallbackManager callbackManager;
    private String facebookEmailId;
    private String facebookId;
    private String facebookName;
    private LoginManager facebookLoginManager;
    private Profile facebookProfile;
    ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
      //    FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        if (Utils.getBooleanDataFromSharedPreferences(LoginActivity.this, "IsUserLoggedIn")) {
            Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
            dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(dashboardIntent);
            finish();
        }
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        callbackManager = CallbackManager.Factory.create();
        emailIdEditText = (EditText) this.findViewById(R.id.edit_login_username);
        passwordEditText = (EditText) this.findViewById(R.id.edit_login_password);
        googleSignImageView = (ImageView) this.findViewById(R.id.image_login_google);
        rememberMeCheckbox = (CheckBox) this.findViewById(R.id.check_login_remember_me);
        forgotPasswordTextView = (TextView) this.findViewById(R.id.text_login_forgot_password);
        registerTextView = (TextView) this.findViewById(R.id.text_login_register);
        facebookSignInImageView = (ImageView) this.findViewById(R.id.image_login_facebook);
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        loginButton = (Button) this.findViewById(R.id.button_login_signin);
        if (Utils.getBooleanDataFromSharedPreferences(LoginActivity.this, "IsRememberMe")) {
            emailIdEditText.setText(Utils.getDataFromSharedPreferences(LoginActivity.this, "EmailAddress"));
            passwordEditText.setText(Utils.getDataFromSharedPreferences(LoginActivity.this, "Password"));
        }
        forgotPasswordTextView.setOnClickListener(this);
        registerTextView.setOnClickListener(this);
        googleSignImageView.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        facebookSignInImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login_signin: {
                if (emailIdEditText.getText().length() < 1) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_empty_emailid), Toast.LENGTH_SHORT).show();
                    emailIdEditText.requestFocus();
                } else if (!Utils.validate(emailIdEditText.getText().toString())) {
                    Toast.makeText(LoginActivity.this, getString(R.string.registration_invalid_email), Toast.LENGTH_SHORT).show();
                    emailIdEditText.requestFocus();
                } else if (passwordEditText.getText().length() < 1) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_empty_password), Toast.LENGTH_SHORT).show();
                    passwordEditText.requestFocus();
                } else {
                    new LoginTask().execute();

                }
                break;
            }
            case R.id.image_login_google: {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            }
            case R.id.text_login_forgot_password: {
                Intent changePasswordIntent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivityForResult(changePasswordIntent, Constants.CHANGE_PASSWORD);
                break;
            }
            case R.id.text_login_register: {
                Intent registerIntent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(registerIntent);
                break;
            }
            case R.id.image_login_facebook: {
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
                //   LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                // App code
                                isSigninFromFacebook = true;
                                GraphRequest request = GraphRequest.newMeRequest(
                                        loginResult.getAccessToken(),
                                        new GraphRequest.GraphJSONObjectCallback() {
                                            @Override
                                            public void onCompleted(JSONObject object, GraphResponse response) {
                                                Utils.writeLogIntoFile("Facebook Login Response:" + response.toString());
                                                facebookProfile = Profile.getCurrentProfile();

                                                facebookLoginManager = LoginManager.getInstance();


                                                try {
                                                    facebookEmailId = object.getString("email");
                                                    facebookId = object.getString("id");
                                                    facebookName = object.getString("name");
                                                } catch (JSONException e) {

                                                }
                                                new LoginTask().execute();

                                            }
                                        });
                                Bundle parameters = new Bundle();
                                parameters.putString("fields", "id,name,email");
                                request.setParameters(parameters);
                                request.executeAsync();
                            }

                            @Override
                            public void onCancel() {
                                // App code
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                // App code
                            }
                        });
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            googleSignInAccount = completedTask.getResult(ApiException.class);
            isSigninFromGoogle = true;
            new LoginTask().execute();

        } catch (ApiException e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (resultCode == RESULT_OK) {
            if (requestCode == Constants.CHANGE_PASSWORD) {
                Toast.makeText(LoginActivity.this, getString(R.string.forget_password_message), Toast.LENGTH_SHORT).show();

            }
        }
    }

    class LoginTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            objProgressDialog = new ProgressDialog(LoginActivity.this);
            objProgressDialog.setMessage("Please wait..");
            objProgressDialog.setCanceledOnTouchOutside(false);
            objProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String testMessage="";
            JSONObject registrationJson = new JSONObject();
            try {
                registrationJson.put("task", "getUserDetails");
                registrationJson.put("device_token", Utils.getDataFromSharedPreferences(LoginActivity.this, "DeviceToken"));
                registrationJson.put("device_type", "android");
                registrationJson.put("device_key", Utils.getDeviceId(LoginActivity.this, LoginActivity.this));
                registrationJson.put("usertimeZone", TimeZone.getDefault().getID());
                testMessage="Login Attempt";
                if (isSigninFromGoogle) {
                    registrationJson.put("email", googleSignInAccount.getEmail());
                    registrationJson.put("password", "");
                    registrationJson.put("provider_type", "google");
                    registrationJson.put("provider_key", googleSignInAccount.getId());
                    try {
                        if (googleSignInAccount.getDisplayName().split(" ")[0].contains(".com")) {
                            registrationJson.put("first_name", googleSignInAccount.getGivenName().split(" ")[0]);
                        } else {
                            registrationJson.put("first_name", googleSignInAccount.getDisplayName().split(" ")[0]);
                        }
                    }catch (Exception e){
                    }
                    try{
                        registrationJson.put("last_name", googleSignInAccount.getDisplayName().split(" ")[1]);
                    }catch(Exception e){
                    }
                    registrationJson.put("image_name", googleSignInAccount.getPhotoUrl());

                    try {
                        testMessage = testMessage + "\n" + "Display NAme:" + googleSignInAccount.getDisplayName();
                        testMessage = testMessage + "\n" + "Given NAme:" + googleSignInAccount.getGivenName();

                    }catch (Exception e){

                    }


                } else if (isSigninFromFacebook) {
                    registrationJson.put("email", facebookEmailId);
                    registrationJson.put("password", "");
                    registrationJson.put("provider_key", facebookId);
                    registrationJson.put("provider_type", "facebook");
                    registrationJson.put("first_name", facebookName.split(" ")[0]);
                    registrationJson.put("last_name", facebookName.split(" ")[1]);
                    if (facebookProfile != null) {
                        registrationJson.put("image_name", String.valueOf(facebookProfile.getProfilePictureUri(600, 600)));
                    } else {
                        registrationJson.put("image_name", "http://graph.facebook.com/" + facebookId + "/picture?type=large");
                    }
                } else {
                    registrationJson.put("email", emailIdEditText.getText().toString());
                    registrationJson.put("password", passwordEditText.getText().toString());
                    registrationJson.put("provider_key", "");
                    registrationJson.put("provider_type", "email");
                }


                Utils.writeLogIntoFile("Login Request:" + registrationJson.toString());

            } catch (Exception e) {
                Utils.writeLogIntoFile("Exception in Building Login REquest:"+e.getMessage());
                Utils.writeLogIntoFile("Login Request while Exception:"+registrationJson.toString());

                e.printStackTrace();
            }
            SendLogThread sendLogThread = new SendLogThread(testMessage+"\n"+registrationJson.toString());
            executor.execute(sendLogThread);

            Connection objConnection = new Connection();
            String response = objConnection.getResponseFromWebservice(Constants.LOGIN, registrationJson);
            Log.i("Abhi", "Response=" + response);
            Utils.writeLogIntoFile("Login Response:" + response);

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);

                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Record Found")) {
                    JSONObject responseJsonObject = jsonObj.getJSONObject("data");
                    if (rememberMeCheckbox.isChecked()) {
                        Utils.saveBooleanIntoSharedPreferences(LoginActivity.this, "IsRememberMe", true);
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "Password", passwordEditText.getText().toString());
                    } else {
                        Utils.saveBooleanIntoSharedPreferences(LoginActivity.this, "IsRememberMe", false);
                    }
                    Utils.saveBooleanIntoSharedPreferences(LoginActivity.this, "IsUserLoggedIn", true);
                    Utils.saveDataIntoSharedPreferences(LoginActivity.this, "UserId", responseJsonObject.getString("user_id"));
                    Utils.saveDataIntoSharedPreferences(LoginActivity.this, "MemberType", responseJsonObject.getString("member_type"));

                    Utils.saveDataIntoSharedPreferences(LoginActivity.this, "Name", (responseJsonObject.getString("first_name") + " " + responseJsonObject.getString("last_name")));
                    Utils.saveDataIntoSharedPreferences(LoginActivity.this, "UserImage", responseJsonObject.getString("user_image"));
                    if(isSigninFromFacebook){
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "EmailAddress", facebookEmailId);
                    }else if(isSigninFromGoogle){
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "EmailAddress", googleSignInAccount.getEmail());
                    }else{
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "EmailAddress", emailIdEditText.getText().toString());
                    }
                    Utils.saveDataIntoSharedPreferences(LoginActivity.this, "FirstName", (responseJsonObject.getString("first_name")));
                    Utils.saveDataIntoSharedPreferences(LoginActivity.this, "LastName", (responseJsonObject.getString("last_name")));

                    Toast.makeText(LoginActivity.this, "Login Successfull!", Toast.LENGTH_LONG).show();
                    Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                    dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(dashboardIntent);
                    finish();
                } else if (jsonObj.getString("status_message").equalsIgnoreCase("Added User")) {
                    JSONObject responseJsonObject = jsonObj.getJSONObject("data");

                    Utils.saveBooleanIntoSharedPreferences(LoginActivity.this, "IsUserLoggedIn", true);
                    Utils.saveDataIntoSharedPreferences(LoginActivity.this, "UserId", responseJsonObject.getString("user_id"));
                    if (isSigninFromGoogle) {
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "UserImage", googleSignInAccount.getPhotoUrl() != null ? googleSignInAccount.getPhotoUrl().toString() : "");
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "EmailAddress", googleSignInAccount.getEmail());
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "Name", googleSignInAccount.getDisplayName());
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "FirstName", googleSignInAccount.getDisplayName() != null ? googleSignInAccount.getDisplayName().split(" ")[0] : "");
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "LastName", googleSignInAccount.getDisplayName() != null ? googleSignInAccount.getDisplayName().split(" ")[1] : "");

                    } else if (isSigninFromFacebook) {
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "UserImage", String.valueOf(facebookProfile.getProfilePictureUri(600, 600)));
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "EmailAddress", facebookEmailId);
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "Name", facebookProfile.getFirstName() + " " + facebookProfile.getLastName());
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "FirstName", facebookProfile.getFirstName());
                        Utils.saveDataIntoSharedPreferences(LoginActivity.this, "LastName", facebookProfile.getLastName());

                    }
                    Toast.makeText(LoginActivity.this, "Login Successfull!", Toast.LENGTH_LONG).show();
                    Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                    dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(dashboardIntent);
                    finish();
                } else if (jsonObj.getString("status").equals("400")) {
                    emailIdEditText.setText("");
                    passwordEditText.setText("");
                    if (isSigninFromGoogle) {

                        mGoogleSignInClient.signOut();

                    }
                    Toast.makeText(LoginActivity.this, jsonObj.getString("status_message"), Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Utils.writeLogIntoFile("Stack : " + e.getStackTrace()[0].getClassName() + ", Method:" + e.getStackTrace()[0].getMethodName() + ",Line :" + e.getStackTrace()[0].getLineNumber());

                Toast.makeText(LoginActivity.this, "Network Failure,please try again", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                isSigninFromGoogle = false;
                objProgressDialog.cancel();
                if (facebookLoginManager != null) {
                    facebookLoginManager.logOut();
                }if(mGoogleSignInClient!=null){
                    Log.i("Abhi","Setting it null");
                    mGoogleSignInClient.signOut();
                }

            }
            super.onPostExecute(result);
        }

    }
    public class SendLogThread implements Runnable {
        String message;
        public SendLogThread(String message){
            this.message=message;
        }
        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();

            try {
                fetchItemsJson.put("task", "socialmedialog");
                fetchItemsJson.put("logstring", message);
                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.LOGIN, fetchItemsJson);
                JSONObject jsonObj = new JSONObject(response);
              Log.i("Abhi","Response From Log Api:"+jsonObj.toString());
            } catch (Exception e) {
                Utils.log(e.getMessage());

                e.printStackTrace();
            }
        }
    }
}
