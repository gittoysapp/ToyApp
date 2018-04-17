package com.abhi.toyswap.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.Models.Message;
import com.abhi.toyswap.R;

import java.util.List;


/**
 * Created by Abhishek28.Gupta on 11-12-2017.
 */

public class ChatMessageItemAdapter extends RecyclerView.Adapter<ChatMessageItemAdapter.Holder> {

    private Context mContext;
    public List<Message> messagesList;
    public ImageLoader imageLoader;
    private static LayoutInflater inflater = null;
    private String userImageUrl;
    private boolean isOtherItemsChatScreen;

    // Constructor
    public ChatMessageItemAdapter(Context context, List<Message> messagesList,String userImageUrl,boolean isOtherItemsChatScreen ){
        mContext = context;
        this.messagesList=messagesList;
        this.isOtherItemsChatScreen=isOtherItemsChatScreen;
        this.userImageUrl=userImageUrl;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(context.getApplicationContext());

    }


    public void changeData(List<Message> messagesList,String secondUserImageUrl){
        this.messagesList=messagesList;
        this.userImageUrl=secondUserImageUrl;
    }
    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public ChatMessageItemAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chat_message, parent, false);
        return new Holder(itemView, viewType);
    }


    @Override
    public void onBindViewHolder(ChatMessageItemAdapter.Holder holder, final int position) {

        holder.otherUserMessage.setText(messagesList.get(position).getMessage());
        holder.ownMessageDateTime.setText(messagesList.get(position).getMessageDateTime());
        holder.otherUserMessageDateTime.setText(messagesList.get(position).getMessageDateTime());
        imageLoader.DisplayImage(userImageUrl, holder.userPhotoImageView, false);
        if(isOtherItemsChatScreen){
            holder.otherUserMessage.setVisibility(messagesList.get(position).getMessageFrom().equals("INTERESTED_USER")?View.GONE:View.VISIBLE);
            holder.otherUserMessageDateTime.setVisibility(messagesList.get(position).getMessageFrom().equals("INTERESTED_USER")?View.GONE:View.VISIBLE);
            holder.userPhotoImageView.setVisibility(messagesList.get(position).getMessageFrom().equals("INTERESTED_USER")?View.GONE:View.VISIBLE);
            holder.ownMessage.setVisibility(messagesList.get(position).getMessageFrom().equals("INTERESTED_USER")?View.VISIBLE:View.GONE);
            holder.ownMessageDateTime.setVisibility(messagesList.get(position).getMessageFrom().equals("INTERESTED_USER")?View.VISIBLE:View.GONE);

        }else{
            holder.otherUserMessage.setVisibility(messagesList.get(position).getMessageFrom().equals("ITEM_USER")?View.GONE:View.VISIBLE);
            holder.otherUserMessageDateTime.setVisibility(messagesList.get(position).getMessageFrom().equals("ITEM_USER")?View.GONE:View.VISIBLE);

            holder.userPhotoImageView.setVisibility(messagesList.get(position).getMessageFrom().equals("ITEM_USER")?View.GONE:View.VISIBLE);
            holder.ownMessage.setVisibility(messagesList.get(position).getMessageFrom().equals("ITEM_USER")?View.VISIBLE:View.GONE);
            holder.ownMessageDateTime.setVisibility(messagesList.get(position).getMessageFrom().equals("ITEM_USER")?View.VISIBLE:View.GONE);

        }


        holder.ownMessage.setText( messagesList.get(position).getMessage());
        holder.bind(messagesList.get(position));

    }
    @Override
    public int getItemViewType(int position) {
        return 0;
    }
    public class Holder extends RecyclerView.ViewHolder {
        ImageView userPhotoImageView;
        TextView otherUserMessage;
        TextView ownMessage;
        TextView otherUserMessageDateTime;
        TextView ownMessageDateTime;

        public void bind(final Message item) {

        }

        public Holder(View rowView, int type) {
            super(rowView);

            otherUserMessage = (TextView) rowView.findViewById(R.id.text_chat_message_other_user_message);
            ownMessage = (TextView) rowView.findViewById(R.id.text_chat_message_own_message);
            userPhotoImageView = (ImageView) rowView.findViewById(R.id.image_chat_message_icon);
            otherUserMessageDateTime=(TextView)rowView.findViewById(R.id.text_chat_message_other_user_message_datetime);
            ownMessageDateTime=(TextView)rowView.findViewById(R.id.text_chat_message_own_message_datetime);

        }
    }


}