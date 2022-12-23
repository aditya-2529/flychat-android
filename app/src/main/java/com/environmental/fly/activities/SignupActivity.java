
package com.environmental.fly.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.environmental.fly.MainActivity;
import com.environmental.fly.databinding.ActivitySigninBinding;
import com.environmental.fly.databinding.ActivitySignupBinding;
import com.environmental.fly.utilities.Constants;
import com.environmental.fly.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {

        binding.sinin.setOnClickListener(v -> onBackPressed());
        binding.buttonSignup.setOnClickListener(v -> {
            if(isValidSignupDetails()){
                signUp();
            }
        });
        binding.layImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignup.setVisibility(View.INVISIBLE);
            binding.proSign.setVisibility(View.VISIBLE);
        } else{
            binding.buttonSignup.setVisibility(View.VISIBLE);
            binding.proSign.setVisibility(View.INVISIBLE);
        }
    }
    public void signUp(){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME,binding.Nameo.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.Emailo.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.Passo.getText().toString());
        user.put(Constants.KEY_IMAGE,encodedImage);
            db.collection(Constants.KEY_COLLECTION_USERS).add(user)
                    .addOnSuccessListener(documentReference -> {
                        loading(false);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                        preferenceManager.putString(Constants.KEY_NAME, binding.Nameo.getText().toString());
                        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .addOnFailureListener(exception -> {
                        loading(false);
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        

    }
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap( bitmap, previewWidth, previewHeight , false) ;
        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bAOS) ;
        byte[] bytes = bAOS.toByteArray( );
        return Base64.encodeToString(bytes , Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );



    private boolean isValidSignupDetails(){
        if(encodedImage == null){
            Toast.makeText(getApplicationContext(), "Select Profile Image", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(binding.Nameo.getText().toString().trim().isEmpty()){
            Toast.makeText(getApplicationContext(), "Enter Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(binding.Emailo.getText().toString().trim().isEmpty()){
            Toast.makeText(getApplicationContext(), "Enter Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.Emailo.getText().toString()).matches()){
            Toast.makeText(getApplicationContext(), "Enter Valid Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(binding.conpasso.getText().toString().trim().isEmpty()){
            Toast.makeText(getApplicationContext(), "Enter Password", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!binding.Passo.getText().toString().equals(binding.conpasso.getText().toString())){
            Toast.makeText(getApplicationContext(), "Password Not Matching", Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            return true;
        }

    }

}