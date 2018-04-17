package com.abhi.toyswap.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.Models.MessageThread;
import com.abhi.toyswap.R;
import com.abhi.toyswap.activity.ChatActivity;
import com.abhi.toyswap.adapters.UserItemMessagesAdapter;
import com.abhi.toyswap.interfaces.OnItemClickInterface;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.GPSTracker;
import com.abhi.toyswap.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class ItemMessagesFragment extends Fragment implements View.OnClickListener, OnItemClickInterface {

    private RecyclerView messagesRecyclerView;
    private TextView titleTextView;
    private ProgressDialog objProgressDialog;
    private UserItemMessagesAdapter messagesItemAdapter;
    private GPSTracker objGpsTracker;
    private ArrayList<MessageThread> userItemMessages;
    private LinearLayoutManager linearLayoutManager ;
    private String itemId="";
    private String itemName;
    private String itemImageUrl;
    private ArrayList<MessageThread> otherUsersMessagesForItem;

    private View view;



    public static ItemMessagesFragment newInstance( ) {
        ItemMessagesFragment fragment = new ItemMessagesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                itemName=getArguments().getString("ItemName");
                itemId=getArguments().getString("ItemId");
                itemImageUrl=getArguments().getString("ItemImageUrl");
                otherUsersMessagesForItem=getArguments().getParcelableArrayList("UsersList");
            }catch (ClassCastException e){
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {

            view = inflater.inflate(R.layout.fragment_item_messages, container, false);
            ImageView backImageView=(ImageView)view.findViewById(R.id.imageview_item_messages_back);
            titleTextView=(TextView)view.findViewById(R.id.toolbar_title);
            messagesRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_item_messages);
            objGpsTracker = GPSTracker.getInstance(getContext(), getActivity());
            linearLayoutManager = new LinearLayoutManager(getContext());
            backImageView.setOnClickListener(this);
            titleTextView.setText(itemName);
            new getItemsTask().execute();
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
            messagesRecyclerView.addItemDecoration(dividerItemDecoration);

        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageview_item_messages_back:{
                getFragmentManager().popBackStack();
                break;
            }
        }
    }

    @Override
    public void onItemClick(Object data) {
        MessageThread messageThread = (MessageThread) data;
        Intent chatViewIntent = new Intent(getActivity(), ChatActivity.class);
        chatViewIntent.putExtra("UserImageUrl",((MessageThread) data).getUserImageUr());
        chatViewIntent.putExtra("ItemName",((MessageThread) data).getItemName());
        chatViewIntent.putExtra("ItemId",((MessageThread) data).getItemId());
        chatViewIntent.putExtra("ItemImageUrl",((MessageThread) data).getItemImageUrl());
        chatViewIntent.putParcelableArrayListExtra("TopBarList",userItemMessages);

        chatViewIntent.putExtra("SecondUserId",((MessageThread) data).getUserId());

        startActivityForResult(chatViewIntent, Constants.POST_NEW_ITEM);
    }
    class getItemsTask extends AsyncTask<Void, Void, String> {
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
            JSONObject fetchItemsJson = new JSONObject();

            try {
                fetchItemsJson.put("task", "getChat");
                fetchItemsJson.put("user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                fetchItemsJson.put("item_id", itemId);
                fetchItemsJson.put("myUserid", "");
                fetchItemsJson.put("showChat", "");
                fetchItemsJson.put("seconduser_id", "");
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

                    JSONArray userItemsJsonArray=jsonObj.getJSONObject("data").getJSONArray("userItem");
                    userItemMessages=new ArrayList<MessageThread>();
                    MessageThread messageThread;
                    for(int index=0;index<userItemsJsonArray.length();index++){
                        messageThread=new MessageThread();
                        messageThread.setItemId(itemId);
                        messageThread.setItemImageUrl(itemImageUrl);
                        messageThread.setItemName(itemName);
                        messageThread.setUserId(userItemsJsonArray.getJSONObject(index).getString("seconduser_id"));
                        messageThread.setUserImageUr(userItemsJsonArray.getJSONObject(index).getString("user_image"));
                        messageThread.setUserName(userItemsJsonArray.getJSONObject(index).getString("user_name"));
                        messageThread.setLastMessage(userItemsJsonArray.getJSONObject(index).getString("lastMessage"));
                        userItemMessages.add(messageThread);
                    }
                    messagesItemAdapter=new UserItemMessagesAdapter(getContext(),ItemMessagesFragment.this,userItemMessages);
                    messagesRecyclerView.setLayoutManager(linearLayoutManager);
                    messagesRecyclerView.setAdapter(messagesItemAdapter);

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
}