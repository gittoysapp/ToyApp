package com.abhi.toyswap.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.abhi.toyswap.R;

public class ItemAddedSuccessfullyActivity extends AppCompatActivity implements View.OnClickListener {

    private Button postAnotherItemButton;
    private TextView backTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_item_added_successfully);
        postAnotherItemButton=(Button)this.findViewById(R.id.button_item_added_successfully_post_another);
        backTextView=(TextView)this.findViewById(R.id.text_item_added_successfully_back);
        postAnotherItemButton.setOnClickListener(this);
        backTextView.setOnClickListener(this);



    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_item_added_successfully_post_another: {
                setResult(RESULT_OK);
                finish();

                break;
            }
            case R.id.text_item_added_successfully_back: {
                finish();

                break;
            }

        }
    }

}
