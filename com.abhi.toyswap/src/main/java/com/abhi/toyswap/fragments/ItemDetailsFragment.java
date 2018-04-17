package com.abhi.toyswap.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.toyswap.Connection.Connection;
import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.Models.Category;
import com.abhi.toyswap.Models.ProductDetails.ProductItem;
import com.abhi.toyswap.R;
import com.abhi.toyswap.activity.ChatActivity;
import com.abhi.toyswap.adapters.ItemDetailsPhotoAdapter;
import com.abhi.toyswap.utils.Constants;
import com.abhi.toyswap.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ItemDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemDetailsFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "ItemDetails";
    private static final String ARG_PARAM2 = "param2";
    private EditText messageEditText;
    private ImageView favouriteImageView;
    private NestedScrollView nestedScrollView;
    private ViewPager itemPhotosViewPager;
    private ImageView[] dotImageViews;
    private LinearLayout dotsContainerLayout;
    private LinearLayout autoWriteMessagesTemplatesLayout;
    private TextView likesTextView;
    private TextView dislikesTextView;
    private CardView likesCardView;
    private CardView dislikesCardView;
    private ProgressDialog objProgressDialog;
    private TextView followTextView;
    private Button sendMessageButton;
    private Button reportButton;
    ExecutorService executor = Executors.newFixedThreadPool(4);
    private ProductItem mProductItem;
    private ImageLoader imageLoader;
    private ImageView shareImageView;
    private boolean isImageFitToScreen = false;
    private ItemDetailsPhotoAdapter photosViewPagerAdapter;
    private LinearLayout reportOptionsRadioGroup;
    private Button submitReportOptionButton;
    private CheckBox[] reportOptionsCheckboxArray;
    private AlertDialog reportDialog;

    public ItemDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ItemDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ItemDetailsFragment newInstance(ProductItem item) {
        ItemDetailsFragment fragment = new ItemDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLoader = new ImageLoader(getContext());

        if (getArguments() != null) {
            try {
                mProductItem = getArguments().getParcelable(ARG_PARAM1);
            } catch (ClassCastException e) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_details, container, false);
        TextView toolBarTitle = (TextView) view.findViewById(R.id.toolbar_title);
        favouriteImageView = (ImageView) view.findViewById(R.id.imageview_details_favourite);
        ImageView backImageView = (ImageView) view.findViewById(R.id.imageview_item_details_back);
        nestedScrollView = (NestedScrollView) view.findViewById(R.id.scroll);
        autoWriteMessagesTemplatesLayout = (LinearLayout) view.findViewById(R.id.linear_details_autowrite_messages_section);
         reportButton = (Button) view.findViewById(R.id.button_details_report);
        followTextView = (TextView) view.findViewById(R.id.text_details_follow);
        ImageView locationImageView = (ImageView) view.findViewById(R.id.image_details_location);
        shareImageView = (ImageView) view.findViewById(R.id.image_item_details_share);
        itemPhotosViewPager = (ViewPager) view.findViewById(R.id.viewpager_details_item_photos);
        dotsContainerLayout = (LinearLayout) view.findViewById(R.id.linear_details_dots_container);
        TextView priceTextView = (TextView) view.findViewById(R.id.text_details_price);
        ImageView buyImageView = (ImageView) view.findViewById(R.id.imageview_details_buy);
        ImageView swapImageView = (ImageView) view.findViewById(R.id.imageview_details_swap);
        ImageView bidImageView = (ImageView) view.findViewById(R.id.imageview_details_bid);
        TextView timeTextView = (TextView) view.findViewById(R.id.text_details_time);
        ImageView userPhotoImageView = (ImageView) view.findViewById(R.id.image_details_user_photo);
        TextView postedByTextView = (TextView) view.findViewById(R.id.text_details_postedby);
        TextView locationTextView = (TextView) view.findViewById(R.id.text_details_location);
        ImageView locationIconImageView = (ImageView) view.findViewById(R.id.image_details_location_icon);
        final RelativeLayout visibilityAdjustableRelativeLayout = (RelativeLayout) view.findViewById(R.id.relative_details_visibility_adjustable_panel);
        TextView descriptionTextView = (TextView) view.findViewById(R.id.text_details_description);
        messageEditText = (EditText) view.findViewById(R.id.edit_details_message);
        sendMessageButton = (Button) view.findViewById(R.id.button_details_send_message);
        final Button showHideButton = (Button) view.findViewById(R.id.button_details_show_or_hide_more);
        likesTextView = (TextView) view.findViewById(R.id.text_details_number_of_likes);
        dislikesTextView = (TextView) view.findViewById(R.id.text_details_number_of_dislikes);

        likesCardView = (CardView) view.findViewById(R.id.cardview_details_likes);
        dislikesCardView = (CardView) view.findViewById(R.id.cardview_details_dislikes);

        new getTemplateMessagesTask().execute();
        imageLoader.DisplayImage(mProductItem.getUserImage(), userPhotoImageView, false);

        photosViewPagerAdapter = new ItemDetailsPhotoAdapter(getContext(), mProductItem.getProductImageUrl());

        itemPhotosViewPager.setAdapter(photosViewPagerAdapter);

        dotImageViews = new ImageView[mProductItem.getProductImageUrl().size()];

        if (mProductItem.getProductImageUrl().size() > 1) {
            for (int index = 0; index < mProductItem.getProductImageUrl().size(); index++) {
                dotImageViews[index] = new ImageView(getContext());
                dotImageViews[index].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.inactive_dot));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(8, 0, 8, 0);
                dotsContainerLayout.addView(dotImageViews[index], params);
            }

            dotImageViews[0].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot));
        }

        itemPhotosViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                for (int index = 0; index < mProductItem.getProductImageUrl().size(); index++) {
                    dotImageViews[index].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.inactive_dot));
                }
                dotImageViews[position].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        favouriteImageView.setImageResource(mProductItem.isFavourite() ? R.drawable.icon_favourite : R.drawable.icon_unfavourite);
        favouriteImageView.setTag(mProductItem.isFavourite());
        if (mProductItem.isFollowed()) {
            followTextView.setText(getResources().getText(R.string.unfollow));
        } else {
            followTextView.setText(getResources().getText(R.string.follow));

        }
        if(mProductItem.getIsAlreadyReported()){
            reportButton.setText(getString(R.string.reported));
            reportButton.setEnabled(false);
        }
        followTextView.setTag(mProductItem.isFollowed());
        buyImageView.setImageResource(mProductItem.isAvailableForBuy() ? R.drawable.icon_buy_enabled : R.drawable.icon_buy_item_details_disabled);
        swapImageView.setImageResource(mProductItem.isAvailableForSwap() ? R.drawable.icon_swap_enabled : R.drawable.icon_swap_item_details_disabled);
        bidImageView.setImageResource(mProductItem.isAvailableForBid() ? R.drawable.icon_bid_enabled : R.drawable.icon_bid_item_details_disabled);

        backImageView.setOnClickListener(this);

        favouriteImageView.setOnClickListener(this);
        locationImageView.setOnClickListener(this);
        shareImageView.setOnClickListener(this);
        followTextView.setOnClickListener(this);
        sendMessageButton.setOnClickListener(this);
        reportButton.setOnClickListener(this);


        if (Utils.getDataFromSharedPreferences(getActivity(), "UserLiked") != null && Utils.getDataFromSharedPreferences(getActivity(), "UserLiked").contains("USER_" + mProductItem.getUserId())) {
            likesTextView.setTextColor(getResources().getColor(R.color.black));
            likesCardView.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.app_base_color));
            likesCardView.setTag(1);
            dislikesCardView.setEnabled(false);

        }
        likesCardView.setOnClickListener(this);
        if (Utils.getDataFromSharedPreferences(getActivity(), "UserDisliked") != null && Utils.getDataFromSharedPreferences(getActivity(), "UserDisliked").contains("USER_" + mProductItem.getUserId())) {
            dislikesTextView.setTextColor(getResources().getColor(R.color.black));
            dislikesCardView.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.app_base_color));
            dislikesCardView.setTag(1);
            likesCardView.setEnabled(false);
        }
        dislikesCardView.setOnClickListener(this);
        if (Utils.getDataFromSharedPreferences(getActivity(), "UserId").equals(mProductItem.getUserId())) {
            sendMessageButton.setVisibility(View.GONE);
            messageEditText.setVisibility(View.GONE);
            reportButton.setVisibility(View.GONE);
            followTextView.setVisibility(View.GONE);
            (view.findViewById(R.id.linear_details_autowrite_messages_section)).setVisibility(View.GONE);
        }


        toolBarTitle.setText(mProductItem.getProductName());
        priceTextView.setText("$ " + mProductItem.getPrice());
        timeTextView.setText(mProductItem.getPostedTime());
        postedByTextView.setText(mProductItem.getUserName());
        if (mProductItem.getItemAddress().trim().isEmpty()) {
            locationTextView.setVisibility(View.GONE);
            locationIconImageView.setVisibility(View.GONE);
        } else {
            locationTextView.setText(mProductItem.getItemAddress());
        }
        if (!mProductItem.getDescription().trim().isEmpty()) {
            descriptionTextView.setText(mProductItem.getDescription());
        }

        likesTextView.setText(String.valueOf(mProductItem.getNumberOfLikes()));
        dislikesTextView.setText(String.valueOf(mProductItem.getNumberOfDislikes()));
        showHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibilityAdjustableRelativeLayout.getVisibility() == View.VISIBLE) {
                    visibilityAdjustableRelativeLayout.setVisibility(View.GONE);
                    showHideButton.setText(getString(R.string.more_details));
                } else {
                    visibilityAdjustableRelativeLayout.setVisibility(View.VISIBLE);
                    showHideButton.setText(getString(R.string.less_details));

                }
            }
        });

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
                        getTargetFragment().onActivityResult(
                                getTargetRequestCode(),
                                Constants.BACK_DETAILS_FRAGMENT,
                                new Intent().putExtra("ProductItem", mProductItem)
                        );
                    }
                }
                return false;
            }
        });
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_item_details_back: {
                getFragmentManager().popBackStack();
                executor.shutdownNow();
                break;
            }
            case R.id.button_details_report: {
                new getReportOptionsTask().execute();
                break;
            }
            case R.id.button_report_dialog_submit: {
                String selectedReportOptions = "";

                for (CheckBox reportOptionCheckbox : reportOptionsCheckboxArray) {
                    if (reportOptionCheckbox.isChecked()) {
                        if(selectedReportOptions.isEmpty()){
                            selectedReportOptions =  reportOptionCheckbox.getText().toString();
                        }else{
                            selectedReportOptions = selectedReportOptions +","+ reportOptionCheckbox.getText().toString();
                        }

                    }
                }
                if(selectedReportOptions.isEmpty()){
                    Toast.makeText(getContext(),"Please select one of the reasons!",Toast.LENGTH_SHORT).show();
                }else{
                    new submitReportItemResponse(selectedReportOptions).execute();
                }
                break;
            }
            case R.id.cardview_report_dialog_close: {
                reportDialog.cancel();
                break;
            }
            case R.id.image_item_details_share: {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                String sAux = "\nLet me recommend you this application\n\n";
                sAux = sAux + "http://www.com.abhi.toyapp/launch \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "choose one"));

                break;
            }
            case R.id.text_view_template_message_1: {
                nestedScrollView.fullScroll(View.FOCUS_DOWN);
                messageEditText.setText(v.getTag().toString());
                break;
            }
            case R.id.text_view_template_message_2: {
                nestedScrollView.fullScroll(View.FOCUS_DOWN);
                messageEditText.setText(v.getTag().toString());
                break;
            }
            case R.id.imageview_details_favourite: {
                FavouriteUnfavouriteThread favouriteUnfavouriteThread = new FavouriteUnfavouriteThread();
                executor.execute(favouriteUnfavouriteThread);
                break;
            }
            case R.id.text_details_follow: {
                FollowUnfollowThread followUnfollowThread = new FollowUnfollowThread();
                executor.execute(followUnfollowThread);
                break;
            }
            case R.id.button_details_send_message: {
                if (!messageEditText.getText().toString().trim().isEmpty()) {
                    SendMessageThread sendMessageThread = new SendMessageThread(messageEditText.getText().toString());
                    executor.execute(sendMessageThread);
                    messageEditText.setText("");
                }
                break;
            }
            case R.id.image_details_location: {
                if (!mProductItem.getItemAddress().trim().isEmpty()) {
                    String map = "http://maps.google.co.in/maps?q=" + mProductItem.getItemAddress().replaceAll("#", "");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
                    mapIntent.setPackage("com.google.android.apps.maps");
                    mapIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(getActivity(), "No Location Details available for this Product!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.cardview_details_likes: {
                if (v.getTag() != null && Integer.parseInt(v.getTag().toString()) == 1) {
                    LikeDislikeThread likeDislikeThread = new LikeDislikeThread(0, -1);
                    executor.execute(likeDislikeThread);
                } else {
                    LikeDislikeThread likeDislikeThread = new LikeDislikeThread(1, -1);
                    executor.execute(likeDislikeThread);
                }

                break;
            }
            case R.id.cardview_details_dislikes: {
                if (v.getTag() != null && Integer.parseInt(v.getTag().toString()) == 1) {
                    LikeDislikeThread likeDislikeThread = new LikeDislikeThread(-1, 0);
                    executor.execute(likeDislikeThread);
                } else {
                    LikeDislikeThread likeDislikeThread = new LikeDislikeThread(-1, 1);
                    executor.execute(likeDislikeThread);
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
                fetchItemsJson.put("fromuser_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                fetchItemsJson.put("touser_id", mProductItem.getUserId());
                fetchItemsJson.put("usertimeZone", TimeZone.getDefault().getID());
                fetchItemsJson.put("item_id", mProductItem.getProductId());
                fetchItemsJson.put("message", message);
                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.SAVE_MESSAGE, fetchItemsJson);

                final JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Added Message")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (jsonObj.getString("status").equals("500")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(getContext(), jsonObj.getString("status_message"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {

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

    public class FollowUnfollowThread implements Runnable {
        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();

            try {
                fetchItemsJson.put("task", "setItemfollow");
                fetchItemsJson.put("user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                fetchItemsJson.put("seconduser_id", mProductItem.getUserId());
                fetchItemsJson.put("is_follow", Boolean.valueOf(followTextView.getTag().toString()) ? "0" : "1");
                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.GET_ITEMS, fetchItemsJson);
                JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Item Followed")) {
                    mProductItem.setFollowed(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            followTextView.setTag(true);
                            followTextView.setText(getResources().getText(R.string.unfollow));
                        }
                    });
                } else if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Item Unfollowed")) {
                    mProductItem.setFollowed(false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            followTextView.setTag(false);
                            followTextView.setText(getResources().getText(R.string.follow));

                        }
                    });
                }
            } catch (Exception e) {
                Utils.log(e.getMessage());

                e.printStackTrace();
            }
        }
    }

    public class FavouriteUnfavouriteThread implements Runnable {
        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();

            try {
                fetchItemsJson.put("task", "setItemlike");
                fetchItemsJson.put("user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                fetchItemsJson.put("item_id", mProductItem.getProductId());
                fetchItemsJson.put("is_like", Boolean.valueOf(favouriteImageView.getTag().toString()) ? "0" : "1");

                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.GET_ITEMS, fetchItemsJson);
                JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Item Liked")) {
                    mProductItem.setFavourite(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            favouriteImageView.setImageResource(R.drawable.icon_favourite);
                            favouriteImageView.setTag(true);
                        }
                    });
                } else if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Item Unliked")) {
                    mProductItem.setFavourite(false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            favouriteImageView.setImageResource(R.drawable.icon_unfavourite);
                            favouriteImageView.setTag(false);
                        }
                    });
                }
            } catch (Exception e) {
                Utils.log(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public class LikeDislikeThread implements Runnable {

        private int isLike;
        private int isDislike;


        public LikeDislikeThread(int isLike, int isDislike) {
            this.isLike = isLike;
            this.isDislike = isDislike;
        }

        @Override
        public void run() {
            JSONObject fetchItemsJson = new JSONObject();

            try {
                fetchItemsJson.put("task", "setUserlike");
                fetchItemsJson.put("user_liked_by", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                fetchItemsJson.put("user_id", mProductItem.getUserId());
                if (isLike != -1) {
                    fetchItemsJson.put("is_like", String.valueOf(isLike));

                } else if (isDislike != -1) {
                    fetchItemsJson.put("is_dislike", String.valueOf(isDislike));

                }

                Connection objConnection = new Connection();
                String response = objConnection.getResponseFromWebservice(Constants.REGISTER, fetchItemsJson);
                JSONObject jsonObj = new JSONObject(response);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("User Liked")) {
                    mProductItem.setFavourite(true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dislikesCardView.setEnabled(false);
                            likesCardView.setTag(1);

                            mProductItem.setNumberOfLikes(mProductItem.getNumberOfLikes() + 1);
                            likesTextView.setText(String.valueOf(mProductItem.getNumberOfLikes()));
                            likesTextView.setTextColor(getResources().getColor(R.color.black));
                            likesCardView.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.app_base_color));

                            String userLiked = Utils.getDataFromSharedPreferences(getActivity(), "UserLiked");
                            if (userLiked == null) {
                                Utils.saveDataIntoSharedPreferences(getActivity(), "UserLiked", "USER_" + mProductItem.getUserId());
                            } else {
                                userLiked = userLiked + ",USER_" + mProductItem.getUserId();
                                Utils.saveDataIntoSharedPreferences(getActivity(), "UserLiked", userLiked);

                            }
                        }
                    });
                } else if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("User Unliked")) {
                    mProductItem.setFavourite(false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dislikesCardView.setEnabled(true);
                            likesCardView.setTag(0);

                            mProductItem.setNumberOfLikes(mProductItem.getNumberOfLikes() - 1);
                            likesTextView.setText(String.valueOf(mProductItem.getNumberOfLikes()));
                            likesTextView.setTextColor(getResources().getColor(R.color.appgrey));
                            likesCardView.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.appgrey));

                            String userLiked = Utils.getDataFromSharedPreferences(getActivity(), "UserLiked");

                            userLiked = userLiked.replace("USER_" + mProductItem.getUserId(), "");
                            Utils.saveDataIntoSharedPreferences(getActivity(), "UserLiked", userLiked);


                        }
                    });
                } else if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("User Disliked")) {
                    mProductItem.setFavourite(false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            likesCardView.setEnabled(false);
                            dislikesCardView.setTag(1);

                            mProductItem.setNumberOfDislikes(mProductItem.getNumberOfDislikes() + 1);
                            dislikesTextView.setText(String.valueOf(mProductItem.getNumberOfDislikes()));
                            dislikesTextView.setTextColor(getResources().getColor(R.color.black));
                            dislikesCardView.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.app_base_color));

                            String userDisliked = Utils.getDataFromSharedPreferences(getActivity(), "UserDisliked");
                            if (userDisliked == null) {
                                Utils.saveDataIntoSharedPreferences(getActivity(), "UserDisliked", "USER_" + mProductItem.getUserId());
                            } else {
                                userDisliked = userDisliked + ",USER_" + mProductItem.getUserId();
                                Utils.saveDataIntoSharedPreferences(getActivity(), "UserDisliked", userDisliked);

                            }

                        }
                    });
                } else if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("User Undisliked")) {
                    mProductItem.setFavourite(false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            likesCardView.setEnabled(true);
                            dislikesCardView.setTag(0);

                            mProductItem.setNumberOfDislikes(mProductItem.getNumberOfDislikes() - 1);
                            dislikesTextView.setText(String.valueOf(mProductItem.getNumberOfDislikes()));
                            dislikesTextView.setTextColor(getResources().getColor(R.color.appgrey));
                            dislikesCardView.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.appgrey));

                            String userDisliked = Utils.getDataFromSharedPreferences(getActivity(), "UserDisliked");

                            userDisliked = userDisliked.replace("USER_" + mProductItem.getUserId(), "");
                            Utils.saveDataIntoSharedPreferences(getActivity(), "UserDisliked", userDisliked);

                        }
                    });
                }
            } catch (Exception e) {
                Utils.log(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    class submitReportItemResponse extends AsyncTask<Void, Void, String> {
        ProgressDialog objProgressDialog;
        String selectedReportMessage;

        public submitReportItemResponse( String selectedReportMessage) {
            this.selectedReportMessage = selectedReportMessage;
        }


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
            JSONObject getReportOptionsJson = new JSONObject();
            try {
                getReportOptionsJson.put("task", "reportItem");
                getReportOptionsJson.put("reportedBy_user_id", Utils.getDataFromSharedPreferences(getActivity(), "UserId"));
                getReportOptionsJson.put("reported_text", selectedReportMessage);
                getReportOptionsJson.put("reported_id", mProductItem.getProductId());
                getReportOptionsJson.put("userTimeZone", TimeZone.getDefault().getID());

            } catch (Exception e) {
                e.printStackTrace();
            }

            Connection objConnection = new Connection();
            String response = objConnection.getResponseFromWebservice(Constants.REPORT_MESSAGES, getReportOptionsJson);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);

                if (jsonObj.getString("status").equals("200")) {
                    reportButton.setText(getString(R.string.reported));
                    reportButton.setEnabled(false);
                    mProductItem.setIsAlreadyReported(true);
                    reportDialog.cancel();
                }else if(jsonObj.getString("status").equals("400") || jsonObj.getString("status").equals("500")){
                    Toast.makeText(getContext(), jsonObj.getString("status_message"), Toast.LENGTH_SHORT).show();
                    reportDialog.cancel();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Network Failure,please try again", Toast.LENGTH_SHORT).show();
                reportDialog.cancel();

                e.printStackTrace();
            } finally {
                objProgressDialog.cancel();
            }
            super.onPostExecute(result);
        }
    }

    class getReportOptionsTask extends AsyncTask<Void, Void, String> {
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
            JSONObject getReportOptionsJson = new JSONObject();
            try {
                getReportOptionsJson.put("task", "getreportedmessages");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Connection objConnection = new Connection();
            String response = objConnection.getResponseFromWebservice(Constants.REPORT_MESSAGES, getReportOptionsJson);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONArray reportMessagesJsonArray;

                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Record Found")) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getContext());
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.report_dialog, null);
                    reportOptionsRadioGroup = (LinearLayout) view.findViewById(R.id.radiogroup_report_dialog_options);

                    submitReportOptionButton = (Button) view.findViewById(R.id.button_report_dialog_submit);
                    CardView closeButton = (CardView) view.findViewById(R.id.cardview_report_dialog_close);
                    closeButton.setOnClickListener(ItemDetailsFragment.this);
                    submitReportOptionButton.setOnClickListener(ItemDetailsFragment.this);

                    reportMessagesJsonArray = jsonObj.getJSONArray("data");
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(20, 10, 0, 10);
                    reportOptionsCheckboxArray = new CheckBox[reportMessagesJsonArray.length()];
                    for (int index = 0; index < reportMessagesJsonArray.length(); index++) {
                        reportOptionsCheckboxArray[index] = new CheckBox(getContext());
                        reportOptionsCheckboxArray[index].setTag(reportMessagesJsonArray.getJSONObject(index).getString("reportedMessage_id"));
                        reportOptionsCheckboxArray[index].setId(Integer.parseInt(reportMessagesJsonArray.getJSONObject(index).getString("reportedMessage_id")));
                        reportOptionsCheckboxArray[index].setText(reportMessagesJsonArray.getJSONObject(index).getString("reportedMessage"));
                        reportOptionsCheckboxArray[index].setTextColor(getResources().getColor(R.color.appgrey));
                        reportOptionsCheckboxArray[index].setButtonTintList(ContextCompat.getColorStateList(getActivity(), R.color.app_base_color));
                        reportOptionsCheckboxArray[index].setLayoutParams(lp);

                        reportOptionsRadioGroup.addView(reportOptionsCheckboxArray[index]);
                    }
                    alertDialogBuilder.setView(view);

                    reportDialog = alertDialogBuilder.create();
                    reportDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    reportDialog.setCanceledOnTouchOutside(false);
                    reportDialog.show();
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

    class getTemplateMessagesTask extends AsyncTask<Void, Void, String> {
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
            JSONObject templateMessagesJson = new JSONObject();
            try {
                templateMessagesJson.put("task", "getAllTemplatemessage");
                templateMessagesJson.put("itemType_id", "");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Connection objConnection = new Connection();
            String response = objConnection.getResponseFromWebservice(Constants.TEMPLATE_MESSAGES, templateMessagesJson);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONArray templatesJsonArray;
                JSONObject categoryJsonObject;
                Category[] categoryList;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                if (jsonObj.getString("status").equals("200")
                        && jsonObj.getString("status_message").equalsIgnoreCase("Record Found")) {
                    templatesJsonArray = jsonObj.getJSONArray("data");
                    View itemViewLayout = null;
                    TextView template1TextView;
                    TextView template2TextView = null;
                    for (int index = 0; index < templatesJsonArray.length(); index++) {

                        if (index % 2 == 0) {
                            itemViewLayout = LayoutInflater.from(getActivity()).inflate(R.layout.view_template_message, null, false);
                            template1TextView = (TextView) itemViewLayout.findViewById(R.id.text_view_template_message_1);
                            template2TextView = (TextView) itemViewLayout.findViewById(R.id.text_view_template_message_2);
                            template1TextView.setText(templatesJsonArray.getJSONObject(index).getString("message_content"));
                            template1TextView.setTag(templatesJsonArray.getJSONObject(index).getString("message_content"));
                            template1TextView.setOnClickListener(ItemDetailsFragment.this);
                            template1TextView.setVisibility(View.VISIBLE);
                            if (index == templatesJsonArray.length() - 1) {
                                autoWriteMessagesTemplatesLayout.addView(itemViewLayout);

                            }
                        } else {
                            template2TextView.setText(templatesJsonArray.getJSONObject(index).getString("message_content"));
                            template2TextView.setTag(templatesJsonArray.getJSONObject(index).getString("message_content"));
                            template2TextView.setOnClickListener(ItemDetailsFragment.this);
                            template2TextView.setVisibility(View.VISIBLE);

                            autoWriteMessagesTemplatesLayout.addView(itemViewLayout);

                        }
                    }

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