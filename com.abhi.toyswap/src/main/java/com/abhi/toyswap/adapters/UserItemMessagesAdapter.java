package com.abhi.toyswap.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abhi.toyswap.ImageLazyLoading.ImageLoader;
import com.abhi.toyswap.Models.MessageThread;
import com.abhi.toyswap.R;
import com.abhi.toyswap.interfaces.OnItemClickInterface;

import java.util.List;


/**
 * Created by Abhishek28.Gupta on 11-12-2017.
 */

public class UserItemMessagesAdapter extends RecyclerView.Adapter<UserItemMessagesAdapter.Holder> {

    private Context mContext;
    public List<MessageThread> messagesList;
    public ImageLoader imageLoader;
    private static LayoutInflater inflater = null;
    private OnItemClickInterface onItemClickInterface;

    // Constructor
    public UserItemMessagesAdapter(Context context, OnItemClickInterface onItemClickListener, List<MessageThread> messagesList ){
        mContext = context;
        this.messagesList=messagesList;
        this.onItemClickInterface = onItemClickListener;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(context.getApplicationContext());

    }


    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public UserItemMessagesAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_user_item_messages_message, parent, false);
        return new Holder(itemView, viewType);
    }


    @Override
    public void onBindViewHolder(UserItemMessagesAdapter.Holder holder, final int position) {

        holder.userNameTextView.setText(messagesList.get(position).getUserName());
        imageLoader.DisplayImage(messagesList.get(position).getUserImageUr(), holder.userPhotoImageView, false);
        holder.lastChatMessageTextView.setText( messagesList.get(position).getLastMessage());
        holder.bind(messagesList.get(position));

    }
    @Override
    public int getItemViewType(int position) {
        return 0;
    }
    public class Holder extends RecyclerView.ViewHolder {
        ImageView userPhotoImageView;
        TextView userNameTextView;
        TextView lastChatMessageTextView;
        TextView messagesCountTextView;

        public void bind(final MessageThread item) {
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    onItemClickInterface.onItemClick(item);
                }

            });
        }

        public Holder(View rowView, int type) {
            super(rowView);

            userNameTextView = (TextView) rowView.findViewById(R.id.text_item_messages_view_component_username);
            lastChatMessageTextView = (TextView) rowView.findViewById(R.id.text_item_messages_view_component_lastmessage);
            userPhotoImageView = (ImageView) rowView.findViewById(R.id.image_item_messages_view_component_user_image);
            messagesCountTextView=(TextView)rowView.findViewById(R.id.text_view_item_messages_count);

        }
    }


}