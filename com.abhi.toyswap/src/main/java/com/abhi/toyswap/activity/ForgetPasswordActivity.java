package com.abhi.toyswap.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.R;
import com.abhi.toyswap.utils.Constants;

import org.json.JSONObject;


public class ForgetPasswordActivity extends ActionBarActivity {
    private EditText emailIdEditText;
    private Button submitButton;
    private ImageView backButton;
    private ProgressDialog objProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_forget_password);
        emailIdEditText = (EditText) this.findViewById(R.id.edit_forget_password_email);
        backButton=(ImageView)this.findViewById(R.id.image_forget_password_back);
        submitButton = (Button) this.findViewById(R.id.button_forget_password);


backButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }
});
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(emailIdEditText.getText().length()<1){
                    Toast.makeText(ForgetPasswordActivity.this,getString(R.string.login_empty_emailid),Toast.LENGTH_SHORT).show();
                    emailIdEditText.requestFocus();
                }else{
                    new ChangePasswordTask().execute();

                }

            }
        });
    }
    class ChangePasswordTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            objProgressDialog = new ProgressDialog(ForgetPasswordActivity.this);
            objProgressDialog.setMessage("Please wait..");
            objProgressDialog.setCanceledOnTouchOutside(false);
            objProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject changePasswordJson = new JSONObject();
            try {

                changePasswordJson.put("email_id", emailIdEditText.getText().toString());
                changePasswordJson.put("task", "forgetPassword");


            } catch (Exception e) {
                e.printStackTrace();
            }

            Connection objConnection = new Connection();
            String response = objConnection.getResponseFromWebservice(Constants.LOGIN,changePasswordJson);

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);

                if (jsonObj.getString("status").equals("200") && jsonObj.getString("status_message").equalsIgnoreCase("Email id has been sent")){
                   setResult(RESULT_OK);
                   finish();
                } else if (jsonObj.getString("status").equals("400")){
                   Toast.makeText(ForgetPasswordActivity.this,jsonObj.getString("status_message"),Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(ForgetPasswordActivity.this, "Network Failure,please try again", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                objProgressDialog.cancel();
            }
            super.onPostExecute(result);
        }

    }

}
