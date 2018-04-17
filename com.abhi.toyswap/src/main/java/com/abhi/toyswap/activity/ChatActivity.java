package com.abhi.toyswap.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.Models.Message;
import com.abhi.toyswap.Models.MessageThread;
import com.abhi.toyswap.R;
import com.abhi.toyswap.Service.MyFirebaseMessagingService;
import com.abhi.toyswap.adapters.ChatMessageItemAdapter;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView messagesRecyclerView;
    private String itemName;
    private String itemId;
    private String itemImageUrl;
    private String secondUserId;
    private String secondUserImageUrl;
    private TextView titleTextView;
    private ImageView backImageView;
    private ImageView sendMessageImageView;
    private EditText sendMessageEditText;
    private boolean isOtherItemsChatScreen;
    private ProgressDialog objProgressDialog;
    private List<Message> messagesList;
    private ArrayList<MessageThread> topBarMessagesList;
    private LinearLayoutManager linearLayoutManager;
    private ChatMessageItemAdapter messagesItemAdapter;
    private LinearLayout topBarListLinearLayout;
    private ExecutorService executor = Executors.newFixedThreadPool(4);
    private ImageLoader imageLoader;
    private View itemViewLayout;
    SimpleDateFormat outputDateFormatter = new SimpleDateFormat("hh:mm a MMM dd");
    SimpleDateFormat inputDateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_chat);
        imageLoader = new ImageLoader(this);

        if (this.getIntent().getBooleanExtra("IsFromNotification", false)) {
            itemName=this.getIntent().getStringExtra("ItemName");
            itemId=this.getIntent().getStringExtra("ItemId");
            secondUserId=this.getIntent().getStringExtra("SendersUserId");
            if(this.getIntent().getStringExtra("ItemUserId").equals(Utils.getDataFromSharedPreferences(this,"UserId"))){
                isOtherItemsChatScreen=false;
            }else{
                isOtherItemsChatScreen=true;
            }
            secondUserImageUrl=this.getIntent().getStringExtra("SendersImageUrl");
        } else {
            itemName = this.getIntent().getStringExtra("ItemName");
            itemId = this.getIntent().getStringExtra("ItemId");
            itemImageUrl = this.getIntent().getStringExtra("ItemImageUrl");
            isOtherItemsChatScreen = this.getIntent().getBooleanExtra("OtherItemsChat", false);
            secondUserId = this.getIntent().getStringExtra("SecondUserId");
            secondUserImageUrl = this.getIntent().getStringExtra("UserImageUrl");
            topBarMessagesList = this.getIntent().getParcelableArrayListExtra("TopBarList");
        }
        titleTextView = (TextView) this.findViewById(R.id.toolbar_title);
        backImageView = (ImageView) this.findViewById(R.id.imageview_chat_back);
        sendMessageImageView = (ImageView) this.findViewById(R.id.image_chat_send_message);
        sendMessageImageView.setOnClickListener(this);
        backImageView.setOnClickListener(this);
        messagesRecyclerView = (RecyclerView) this.findViewById(R.id.recyclerview_chat);
        topBarListLinearLayout = (LinearLayout) this.findViewById(R.id.linear_chat_topbar_list);
        sendMessageEditText = (EditText) this.findViewById(R.id.edit_chat_message);
        linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        inputDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        outputDateFormatter.setTimeZone(TimeZone.getDefault());
        if (isOtherItemsChatScreen) {
            titleTextView.setText(getString(R.string.other_items));

        } else {
            titleTextView.setText(itemName);

        }
        if(topBarMessagesList!=null){
            for (int index = 0; index < topBarMessagesList.size(); index++) {

                if (isOtherItemsChatScreen) {

                    itemViewLayout = LayoutInflater.from(this).inflate(R.layout.view_chat_topbar_list_other_items, null, false);

                    ImageView thumbnailImageView = (ImageView) itemViewLayout.findViewById(R.id.image_chat_topbar_list_thumbnail);
                    TextView itemNameTextView = (TextView) itemViewLayout.findViewById(R.id.text_chat_topbar_list_itemname);
                    TextView usersNameTextView = (TextView) itemViewLayout.findViewById(R.id.text_chat_topbar_list_itemownername);

                    imageLoader.DisplayImage(topBarMessagesList.get(index).getItemImageUrl(), thumbnailImageView, false);
                    itemNameTextView.setText(topBarMessagesList.get(index).getItemName().trim());
                    usersNameTextView.setText("By " + topBarMessagesList.get(index).getUserName().trim());
                    itemViewLayout.setTag(R.id.item_id, topBarMessagesList.get(index).getItemId());
                    itemViewLayout.setTag(R.id.second_user_id, topBarMessagesList.get(index).getUserId());
                    itemViewLayout.setTag(R.id.second_user_image, topBarMessagesList.get(index).getUserImageUr());
                    if (topBarMessagesList.get(index).getUserId().equals(secondUserId)) {
                        thumbnailImageView.setAlpha(1f);
                        usersNameTextView.setTextColor(getResources().getColor(R.color.active_chat));
                        itemNameTextView.setTextColor(getResources().getColor(R.color.active_chat));


                    } else {
                        usersNameTextView.setTextColor(getResources().getColor(R.color.inactive_chat));
                        itemNameTextView.setTextColor(getResources().getColor(R.color.inactive_chat));

                        thumbnailImageView.setAlpha(0.5f);

                    }
                    topBarListLinearLayout.addView(itemViewLayout);


                } else {
                    itemViewLayout = LayoutInflater.from(this).inflate(R.layout.view_chat_topbar_list_other_users, null, false);

                    ImageView thumbnailImageView = (ImageView) itemViewLayout.findViewById(R.id.image_chat_topbar_list_others_thumbnail);
                    TextView usersNameTextView = (TextView) itemViewLayout.findViewById(R.id.text_chat_topbar_list_others_username);
                    imageLoader.DisplayImage(topBarMessagesList.get(index).getUserImageUr(), thumbnailImageView, false);

                    usersNameTextView.setText(topBarMessagesList.get(index).getUserName().trim());
                    itemViewLayout.setTag(R.id.item_id, topBarMessagesList.get(index).getItemId());
                    itemViewLayout.setTag(R.id.second_user_id, topBarMessagesList.get(index).getUserId());
                    itemViewLayout.setTag(R.id.second_user_image, topBarMessagesList.get(index).getUserImageUr());
                    if (topBarMessagesList.get(index).getUserId().equals(secondUserId)) {
                        thumbnailImageView.setAlpha(1f);
                        usersNameTextView.setTextColor(getResources().getColor(R.color.active_chat));

                    } else {

                        usersNameTextView.setTextColor(getResources().getColor(R.color.inactive_chat));

                        thumbnailImageView.setAlpha(0.5f);

                    }
                    topBarListLinearLayout.addView(itemViewLayout);
                }
                itemViewLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        itemId = String.valueOf(v.getTag(R.id.item_id));
                        secondUserId = String.valueOf(v.getTag(R.id.second_user_id));
                        secondUserImageUrl = String.valueOf(v.getTag(R.id.second_user_image));
                        new getMessagesTask().execute();
                        for (int index2 = 0; index2 < topBarListLinearLayout.getChildCount(); index2++) {
                            if (!secondUserId.equals(String.valueOf(topBarListLinearLayout.getChildAt(index2).getTag(R.id.second_user_id)))) {
                                View itemView = topBarListLinearLayout.getChildAt(index2);
                                if (isOtherItemsChatScreen) {
                                    ((TextView) itemView.findViewById(R.id.text_chat_topbar_list_itemownername)).setTextColor(getResources().getColor(R.color.inactive_chat));
                                    ((ImageView) itemView.findViewById(R.id.image_chat_topbar_list_thumbnail)).setAlpha(0.5f);
                                    ((TextView) itemView.findViewById(R.id.text_chat_topbar_list_itemname)).setTextColor(getResources().getColor(R.color.inactive_chat));


                                } else {
                                    ((TextView) itemView.findViewById(R.id.text_chat_topbar_list_others_username)).setTextColor(getResources().getColor(R.color.inactive_chat));
                                    ((ImageView) itemView.findViewById(R.id.image_chat_topbar_list_others_thumbnail)).setAlpha(0.5f);

                                }

                            } else {
                                View itemView = topBarListLinearLayout.getChildAt(index2);
                                if (isOtherItemsChatScreen) {
                                    ((TextView) itemView.findViewById(R.id.text_chat_topbar_list_itemownername)).setTextColor(getResources().getColor(R.color.active_chat));
                                    ((ImageView) itemView.findViewById(R.id.image_chat_topbar_list_thumbnail)).setAlpha(1f);
                                    ((TextView) itemView.findViewById(R.id.text_chat_topbar_list_itemname)).setTextColor(getResources().getColor(R.color.active_chat));

                                } else {
                                    ((TextView) itemView.findViewById(R.id.text_chat_topbar_list_others_username)).setTextColor(getResources().getColor(R.color.active_chat));
                                    ((ImageView) itemView.findViewById(R.id.image_chat_topbar_list_others_thumbnail)).setAlpha(1f);

                                }
                            }
                        }


                    }
                });

            }
        }
        new getMessagesTask().execute();

        sendMessageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && !sendMessageEditText.getText().toString().isEmpty()) {
                    hideKeyboard();

                    SendMessageThread sendMessageThread = new SendMessageThread(sendMessageEditText.getText().toString());
                    executor.execute(sendMessageThread);
                    sendMessageEditText.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        registerReceiver(broadcastReceiver, new IntentFilter(MyFirebaseMessagingService.BROADCAST_ACTION));

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);

        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_chat_back:{
                finish();
                break;
            }case R.id.image_chat_send_message:{
                if(!sendMessageEditText.getText().toString().isEmpty()) {
                    hideKeyboard();
                    SendMessageThread sendMessageThread = new SendMessageThread(sendMessageEditText.getText().toString());
                    executor.execute(sendMessageThread);
                    sendMessageEditText.setText("");
                }
                break;
            }
        }
    }

    public class SendMessageThread implements Runnable {
        private String message;

        public SendMessageThread(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();

            try {
                fetchItemsJson.put("task", "saveMessages");
                fetchItemsJson.put("fromuser_id", Utils.getDataFromSharedPreferences(ChatActivity.this, "UserId"));
                fetchItemsJson.put("touser_id", secondUserId);
                fetchItemsJson.put("usertimeZone", TimeZone.getDefault().getID());
                fetchItemsJson.put("item_id", itemId);
                fetchItemsJson.put("message", message);
                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.SAVE_MESSAGE, fetchItemsJson);

                final JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Added Message")) {
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Calendar currentDate = Calendar.getInstance();
                            String timeMessageAdded = currentDate.get(Calendar.YEAR) + "-" + currentDate.get(Calendar.MONTH) + "-" + currentDate.get(Calendar.DATE) + " " + currentDate.get(Calendar.HOUR_OF_DAY) + ":" + currentDate.get(Calendar.MINUTE) + ":" + currentDate.get(Calendar.SECOND);

                            Toast.makeText(ChatActivity.this, getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
                            Message objMessage = new Message();
                            objMessage.setMessage(message);
                            objMessage.setMessageFromUserId(Utils.getDataFromSharedPreferences(ChatActivity.this, "UserId"));
                            objMessage.setMessageToUserId(secondUserId);
                            if (isOtherItemsChatScreen) {
                                objMessage.setMessageFrom("INTERESTED_USER");
                            } else {
                                objMessage.setMessageFrom("ITEM_USER");
                            }
                            objMessage.setMessageDateTime(outputDateFormatter.format(new Date()));
                            messagesList.add(objMessage);
                            messagesItemAdapter.notifyDataSetChanged();
                            messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
                        }
                    });
                }else  if (jsonObj.getString("status").equals("500")){
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(ChatActivity.this, jsonObj.getString("status_message"), Toast.LENGTH_SHORT).show();
                            }catch (JSONException e){

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

    public class RefreshMessagesListThread implements Runnable {

        public RefreshMessagesListThread() {

        }

        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();

            try {
                fetchItemsJson.put("task", "getChat");
                fetchItemsJson.put("user_id", Utils.getDataFromSharedPreferences(ChatActivity.this, "UserId"));
                fetchItemsJson.put("item_id", itemId);
                fetchItemsJson.put("myUserid", "");
                fetchItemsJson.put("showChat", "true");
                fetchItemsJson.put("seconduser_id", secondUserId);
                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.GET_MESSAGE_THREADS, fetchItemsJson);

                final JSONObject jsonObj = new JSONObject(response);

                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Record Found")) {

                    JSONArray userItemsJsonArray = jsonObj.getJSONObject("data").getJSONArray("userItemchat");
                    messagesList = new ArrayList<Message>();
                    Message objMessage;
                    for (int index = 0; index < userItemsJsonArray.length(); index++) {
                        objMessage = new Message();
                        objMessage.setMessage(userItemsJsonArray.getJSONObject(index).getString("message"));
                        objMessage.setMessageFrom(userItemsJsonArray.getJSONObject(index).getString("message_from"));
                        Date inputDate=inputDateFormatter.parse(userItemsJsonArray.getJSONObject(index).getString("date_time"));

                        objMessage.setMessageDateTime(outputDateFormatter.format(inputDate));
                        objMessage.setMessageFromUserId(userItemsJsonArray.getJSONObject(index).getString("fromuser_id"));
                        objMessage.setMessageToUserId(userItemsJsonArray.getJSONObject(index).getString("touser_id"));
                        messagesList.add(objMessage);
                    }
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (messagesItemAdapter == null) {
                                messagesItemAdapter = new ChatMessageItemAdapter(ChatActivity.this, messagesList, secondUserImageUrl, isOtherItemsChatScreen);
                                messagesRecyclerView.setLayoutManager(linearLayoutManager);
                                messagesRecyclerView.setAdapter(messagesItemAdapter);
                            } else {
                                messagesItemAdapter.changeData(messagesList, secondUserImageUrl);
                                messagesItemAdapter.notifyDataSetChanged();

                            }

                            messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
                        }
                    });


                } else if (jsonObj.getString("status_message").equalsIgnoreCase("No Record Found")) {
                }

            } catch (Exception e) {
                Utils.log(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RefreshMessagesListThread refreshMessagesListThread = new RefreshMessagesListThread();
            executor.execute(refreshMessagesListThread);        }
    };
    class getMessagesTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            objProgressDialog = new ProgressDialog(ChatActivity.this);
            //objProgressDialog.setMessage("Please wait..");
            objProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            objProgressDialog.setCanceledOnTouchOutside(false);

            objProgressDialog.show();
            //add custom progress bar
            objProgressDialog.setContentView(R.layout.my_progress);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject fetchItemsJson = new JSONObject();

            try {
                fetchItemsJson.put("task", "getChat");
                fetchItemsJson.put("user_id", Utils.getDataFromSharedPreferences(ChatActivity.this, "UserId"));
                fetchItemsJson.put("item_id", itemId);
                fetchItemsJson.put("myUserid", "");
                fetchItemsJson.put("showChat", "true");
                fetchItemsJson.put("seconduser_id", secondUserId);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log(e.getMessage());
            }

            Connection objConnection = new Connection();
            String response = objConnection.getResponseFromWebservice(Constants.GET_MESSAGE_THREADS, fetchItemsJson);
            Log.i("Abhi", "Response=" + response);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);

                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Record Found")) {

                    JSONArray userItemsJsonArray = jsonObj.getJSONObject("data").getJSONArray("userItemchat");
                    messagesList = new ArrayList<Message>();
                    Message objMessage;
                    for (int index = 0; index < userItemsJsonArray.length(); index++) {
                        objMessage = new Message();
                        objMessage.setMessage(userItemsJsonArray.getJSONObject(index).getString("message"));
                        objMessage.setMessageFrom(userItemsJsonArray.getJSONObject(index).getString("message_from"));
                        Date inputDate=inputDateFormatter.parse(userItemsJsonArray.getJSONObject(index).getString("date_time"));

                        objMessage.setMessageDateTime(outputDateFormatter.format(inputDate));
                        objMessage.setMessageFromUserId(userItemsJsonArray.getJSONObject(index).getString("fromuser_id"));
                        objMessage.setMessageToUserId(userItemsJsonArray.getJSONObject(index).getString("touser_id"));
                        messagesList.add(objMessage);
                    }
                    if (messagesItemAdapter == null) {
                        messagesItemAdapter = new ChatMessageItemAdapter(ChatActivity.this, messagesList, secondUserImageUrl, isOtherItemsChatScreen);
                        messagesRecyclerView.setLayoutManager(linearLayoutManager);
                        messagesRecyclerView.setAdapter(messagesItemAdapter);
                    } else {
                        messagesItemAdapter.changeData(messagesList, secondUserImageUrl);
                        messagesItemAdapter.notifyDataSetChanged();
                    }

                    messagesRecyclerView.scrollToPosition(messagesList.size() - 1);

                } else if (jsonObj.getString("status_message").equalsIgnoreCase("No Record Found")) {
                }

            } catch (Exception e) {

                Utils.log("Exception e:" + e.getMessage());
                Toast.makeText(ChatActivity.this, "Network Failure,please try again", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                objProgressDialog.cancel();
            }
            super.onPostExecute(result);
        }
    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }
}
