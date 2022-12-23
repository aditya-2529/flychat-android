package com.environmental.fly.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.environmental.fly.databinding.ItemContainerMessageBinding;
import com.environmental.fly.databinding.ItemContainerReceivedBinding;
import com.environmental.fly.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> cM;
    private Bitmap rpi;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setRPI(Bitmap bitmap){
        rpi = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.cM =  chatMessages;
        this.rpi = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
        else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder)holder).setData(cM.get(position));
        } else{
            ((ReceivedMessageViewHolder)holder).setData(cM.get(position),rpi);
        }
    }

    @Override
    public int getItemCount() {
        return cM.size();
    }

    @Override
    public int getItemViewType (int position) {
        if (cM.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerMessageBinding binding;

        SentMessageViewHolder(ItemContainerMessageBinding icrm){
            super(icrm.getRoot());
            binding = icrm;
        }

        void setData(ChatMessage cM){
            binding.textM.setText(cM.message);
            binding.textDT.setText(cM.dateTime);
        }
    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage,Bitmap receiverProfileImage) {
            binding.textM.setText(chatMessage.message);
            binding.textDT.setText(chatMessage.dateTime);
            if(receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
}
