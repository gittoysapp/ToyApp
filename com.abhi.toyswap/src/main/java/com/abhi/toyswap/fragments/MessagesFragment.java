package com.abhi.toyswap.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.Models.MessageThread;
import com.abhi.toyswap.R;
import com.abhi.toyswap.activity.ChatActivity;
import com.abhi.toyswap.adapters.MessagesItemAdapter;
import com.abhi.toyswap.interfaces.OnItemClickInterface;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.GPSTracker;
import com.abhi.toyswap.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MessagesFragment extends Fragment implements View.OnClickListener, OnItemClickInterface {

    private RecyclerView messagesRecyclerView;
    private LinearLayout noChatAvailableLayout;
    private ProgressDialog objProgressDialog;
    private MessagesItemAdapter messagesItemAdapter;
    private GPSTracker objGpsTracker;
    private TabLayout tabLayout;
    private List<MessageThread> selectedMessages;
    private ArrayList<MessageThread> userItemMessages;
    private ArrayList<MessageThread> otherItemMessages;
    private LinearLayoutManager linearLayoutManager;

    ExecutorService executor = Executors.newFixedThreadPool(4);
    private View view;


    public static MessagesFragment newInstance() {
        MessagesFragment fragment = new MessagesFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {

            view = inflater.inflate(R.layout.fragment_messages, container, false);
            tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
            messagesRecyclerView = (RecyclerView) view.findViewById(R.id.gridview_messages_screen_messages);
            noChatAvailableLayout=(LinearLayout)view.findViewById(R.id.linear_messages_no_chat_available);
            objGpsTracker = GPSTracker.getInstance(getContext(), getActivity());
            linearLayoutManager = new LinearLayoutManager(getContext());
            selectedMessages = new ArrayList<>();
            new getItemsTask().execute();
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
            messagesRecyclerView.addItemDecoration(dividerItemDecoration);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                   // if(tabLayout.getTabCount()>1) {
                        if (tabLayout.getSelectedTabPosition() == 0) {
                            selectedMessages.clear();
                            selectedMessages.addAll(userItemMessages);
                            messagesItemAdapter.notifyDataSetChanged();
                            if(userItemMessages.size()==0){
                                noChatAvailableLayout.setVisibility(View.VISIBLE);
                            }else{
                                noChatAvailableLayout.setVisibility(View.GONE);
                            }
                        } else {
                            selectedMessages.clear();
                            selectedMessages.addAll(otherItemMessages);
                            messagesItemAdapter.notifyDataSetChanged();
                            if(otherItemMessages.size()==0){
                                noChatAvailableLayout.setVisibility(View.VISIBLE);
                            }else{
                                noChatAvailableLayout.setVisibility(View.GONE);
                            }
                        }
                   // }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    @Override
    public void onItemClick(Object data) {
        if (tabLayout.getSelectedTabPosition() == 0) {

            MessageThread messageThread = (MessageThread) data;
            ItemMessagesFragment fragment2 = ItemMessagesFragment.newInstance();
            Bundle itemDetailsBundle = new Bundle();
            itemDetailsBundle.putString("ItemName", messageThread.getItemName());
            itemDetailsBundle.putString("ItemId", messageThread.getItemId());
            itemDetailsBundle.putString("ItemImageUrl", messageThread.getItemImageUrl());
            itemDetailsBundle.putParcelableArrayList("UsersList", userItemMessages);
            fragment2.setArguments(itemDetailsBundle);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            // fragment2.setTargetFragment(MessagesFragment.this, Constants.BACK_DETAILS_FRAGMENT);
            fragmentTransaction.replace(R.id.frame_layout, fragment2);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

        } else {
            Intent chatViewIntent = new Intent(getActivity(), ChatActivity.class);
            chatViewIntent.putExtra("OtherItemsChat", true);
            chatViewIntent.putExtra("UserImageUrl", ((MessageThread) data).getUserImageUr());
            chatViewIntent.putExtra("ItemName", ((MessageThread) data).getItemName());
            chatViewIntent.putExtra("ItemId", ((MessageThread) data).getItemId());
            chatViewIntent.putExtra("ItemImageUrl", ((MessageThread) data).getItemImageUrl());
            chatViewIntent.putParcelableArrayListExtra("TopBarList", otherItemMessages);
            chatViewIntent.putExtra("SecondUserId", ((MessageThread) data).getUserId());

            startActivityForResult(chatViewIntent, Constants.POST_NEW_ITEM);
        }
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
                fetchItemsJson.put("item_id", "");
                fetchItemsJson.put("myUserid", "");
                fetchItemsJson.put("showChat", "");
                fetchItemsJson.put("usertimeZone", TimeZone.getDefault().getID());

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

                    userItemMessages = new ArrayList<MessageThread>();
                    otherItemMessages = new ArrayList<MessageThread>();
                    MessageThread messageThread;
                    JSONArray userItemsJsonArray = null;
                    JSONArray othersItemsJsonArray = null;
                    try {
                        userItemsJsonArray = jsonObj.getJSONObject("data").getJSONArray("userItem");
                        for (int index = 0; index < userItemsJsonArray.length(); index++) {
                            messageThread = new MessageThread();
                            messageThread.setItemId(userItemsJsonArray.getJSONObject(index).getString("item_id"));
                            messageThread.setItemImageUrl(userItemsJsonArray.getJSONObject(index).getString("item_image"));
                            messageThread.setItemName(userItemsJsonArray.getJSONObject(index).getString("item_name"));
                            messageThread.setUserId(userItemsJsonArray.getJSONObject(index).getString("user_id"));
                            messageThread.setUserImageUr(userItemsJsonArray.getJSONObject(index).getString("user_image"));
                            messageThread.setUserName(userItemsJsonArray.getJSONObject(index).getString("user_name"));
                            userItemMessages.add(messageThread);
                        }

                    } catch (Exception e) {
                        userItemsJsonArray = null;
                    }
                    try {


                        othersItemsJsonArray = jsonObj.getJSONObject("data").getJSONArray("otherItem");


                        for (int index = 0; index < othersItemsJsonArray.length(); index++) {
                            messageThread = new MessageThread();
                            messageThread.setItemId(othersItemsJsonArray.getJSONObject(index).getString("item_id"));
                            messageThread.setItemImageUrl(othersItemsJsonArray.getJSONObject(index).getString("item_image"));
                            messageThread.setItemName(othersItemsJsonArray.getJSONObject(index).getString("item_name"));
                            messageThread.setUserId(othersItemsJsonArray.getJSONObject(index).getString("seconduser_id"));
                            messageThread.setUserImageUr(othersItemsJsonArray.getJSONObject(index).getString("user_image"));
                            messageThread.setUserName(othersItemsJsonArray.getJSONObject(index).getString("user_name"));
                            otherItemMessages.add(messageThread);
                        }
                    } catch (Exception e) {
                        othersItemsJsonArray = null;
                    }

                    if (userItemsJsonArray != null) {
                        selectedMessages.addAll(userItemMessages);

                    } else {
                        selectedMessages.addAll(otherItemMessages);

                    }

                    messagesItemAdapter = new MessagesItemAdapter(getContext(), MessagesFragment.this, selectedMessages);
                    messagesRecyclerView.setLayoutManager(linearLayoutManager);

                    messagesRecyclerView.setAdapter(messagesItemAdapter);
                    if (userItemsJsonArray != null && userItemsJsonArray.length() != 0) {
                        tabLayout.addTab(tabLayout.newTab().setText("My Items(" + userItemsJsonArray.length() + ")"));
                        noChatAvailableLayout.setVisibility(View.GONE);
                    }else{
                        tabLayout.addTab(tabLayout.newTab().setText("My Items"));
                        noChatAvailableLayout.setVisibility(View.VISIBLE);
                    }
                    if (othersItemsJsonArray != null && othersItemsJsonArray.length() != 0) {
                        tabLayout.addTab(tabLayout.newTab().setText("Other(" + othersItemsJsonArray.length() + ")"));
                    }else{
                        tabLayout.addTab(tabLayout.newTab().setText("Other"));
                    }


                } else if (jsonObj.getString("status_message").equalsIgnoreCase("No Record Found")) {
                    noChatAvailableLayout.setVisibility(View.VISIBLE);

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