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

public class MessagesItemAdapter extends RecyclerView.Adapter<MessagesItemAdapter.Holder> {

    private Context mContext;
    public List<MessageThread> messagesList;
    public ImageLoader imageLoader;
    private static LayoutInflater inflater = null;
    private OnItemClickInterface onItemClickInterface;

    // Constructor
    public MessagesItemAdapter(Context context, OnItemClickInterface onItemClickListener, List<MessageThread> messagesList ){
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
    public MessagesItemAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_messages_message_component, parent, false);
        return new Holder(itemView, viewType);
    }


    @Override
    public void onBindViewHolder(MessagesItemAdapter.Holder holder, final int position) {

        holder.itemNameTextView.setText(messagesList.get(position).getItemName());
        imageLoader.DisplayImage(messagesList.get(position).getItemImageUrl(), holder.userPhotoImageView, false);
        holder.postedByTextView.setText("By:" + messagesList.get(position).getUserName());
        holder.bind(messagesList.get(position));

    }
    @Override
    public int getItemViewType(int position) {
        return 0;
    }
    public class Holder extends RecyclerView.ViewHolder {
        ImageView userPhotoImageView;
        TextView itemNameTextView;
        TextView postedByTextView;

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

            itemNameTextView = (TextView) rowView.findViewById(R.id.text_messages_view_component_itemname);
            postedByTextView = (TextView) rowView.findViewById(R.id.text_messages_view_component_itemowner);
            userPhotoImageView = (ImageView) rowView.findViewById(R.id.image_messages_view_component_user_image);

        }
    }


}