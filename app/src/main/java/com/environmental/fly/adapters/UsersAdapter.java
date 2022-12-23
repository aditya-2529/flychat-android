package com.environmental.fly.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.environmental.fly.databinding.ItemContainerUserBinding;
import com.environmental.fly.listerners.UserListener;
import com.environmental.fly.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.userViewHolder> {

    private final List<User> User;

    private final UserListener uL;

    public UsersAdapter(List<User> user,UserListener uL) {
        this.User = user;
        this.uL = uL;
    }

    @NonNull
    @Override
    public UsersAdapter.userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new userViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.userViewHolder holder, int position) {
        holder.setUserData(User.get(position));
    }

    @Override
    public int getItemCount() {
        return User.size();
    }

    class userViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUserBinding binding;

        userViewHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user){
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> uL.onUserClicked(user));
        }
    }
     private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
     }
}
