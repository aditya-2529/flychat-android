package com.environmental.fly.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.environmental.fly.databinding.ItemContainerMessageBinding;
import com.environmental.fly.databinding.ItemContainerRecentConBinding;
import com.environmental.fly.listerners.ConListener;
import com.environmental.fly.models.ChatMessage;
import com.environmental.fly.models.User;

import java.util.List;

public class RecentConAdapter extends RecyclerView.Adapter<RecentConAdapter.CVH> {

    private final List<ChatMessage> cMs;
    private final ConListener cL;

    public RecentConAdapter(List<ChatMessage> cMs,ConListener cL){
        this.cMs = cMs;
        this.cL = cL;
    }

    @NonNull
    @Override
    public CVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CVH(
                ItemContainerRecentConBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecentConAdapter.CVH holder, int position) {
        holder.setData(cMs.get(position));
    }

    @Override
    public int getItemCount() {
        return cMs.size();
    }

    class CVH extends RecyclerView.ViewHolder{
        ItemContainerRecentConBinding binding;

        CVH(ItemContainerRecentConBinding icrb){
            super(icrb.getRoot());
            binding = icrb;
        }

        void setData(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getBitmapFromEncodedString(chatMessage.conImg));
            binding.textName.setText(chatMessage.conName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conId;
                user.name = chatMessage.conName;
                user.image = chatMessage.conImg;
                cL.onConClicked(user);
            });
        }

    }
    private Bitmap getBitmapFromEncodedString(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
