package com.environmental.fly.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.environmental.fly.adapters.UsersAdapter;
import com.environmental.fly.databinding.ActivityUserBinding;
import com.environmental.fly.listerners.UserListener;
import com.environmental.fly.models.User;
import com.environmental.fly.utilities.Constants;
import com.environmental.fly.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener {

    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();

    }
    private void setListeners(){
        binding.ImageBack.setOnClickListener(v -> onBackPressed());
    }
    private void getUsers(){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot qds : task.getResult()){
                            if(currentUserId.equals(qds.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = qds.getString(Constants.KEY_NAME);
                            user.email = qds.getString(Constants.KEY_EMAIL);
                            user.image = qds.getString(Constants.KEY_IMAGE);
                            user.token = qds.getString(Constants.KEY_FLY_TOKEN);
                            user.id = qds.getId();
                            users.add(user);
                        }
                        if(users.size() > 0){
                            UsersAdapter uA = new UsersAdapter(users,this);
                            binding.rcv.setAdapter(uA);
                            binding.rcv.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    }
                    else{
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){
        binding.errorText.setText(String.format("%s","No User Available"));
        binding.errorText.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.probar.setVisibility(View.VISIBLE);
        } else{
            binding.probar.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onUserClicked(User user){
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}