package com.environmental.fly.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.environmental.fly.MainActivity;
import com.environmental.fly.databinding.ActivitySigninBinding;
import com.environmental.fly.databinding.ActivitySignupBinding;
import com.environmental.fly.utilities.Constants;
import com.environmental.fly.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SigninActivity extends AppCompatActivity {


    private ActivitySigninBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
            finish();
        }
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.tcna.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignupActivity.class)));
        binding.buttonSignin.setOnClickListener(v -> {
            if(isValidSignInDetails()){
                signIn();
            }
        });
//        binding.buttonSignin.setOnClickListener(v -> addDatatoFirestore());
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignin.setVisibility(View.INVISIBLE);
            binding.SignPro.setVisibility(View.VISIBLE);
        } else{
            binding.buttonSignin.setVisibility(View.VISIBLE);
            binding.SignPro.setVisibility(View.INVISIBLE);
        }
    }
    public void signIn(){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inPass.getText().toString())
                .get()
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Unable to signin");
                    }
                });
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidSignInDetails() {
        if (binding.inEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (binding.inPass.getText().toString().trim().isEmpty()) {
            showToast(" Enter password ");
            return false;
        } else
            return true;
    }
//    public void addDatatoFirestore(){
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        // Create a new user with a first and last name
//        HashMap<String, Object> user = new HashMap<>();
//        user.put("first", "Ada");
//        user.put("last", "Lovelace");
//
//        // Add a new document with a generated ID
//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(documentReference -> {
//                    Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(exception -> {
//                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
}