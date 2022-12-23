package com.environmental.fly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.lights.LightsManager;
import android.os.Bundle;
import com.adcolony.sdk.*;

import androidx.appcompat.app.AppCompatActivity;

import com.environmental.fly.activities.BaseActivity;
import com.environmental.fly.activities.ChatActivity;
import com.environmental.fly.activities.SigninActivity;
import com.environmental.fly.activities.UserActivity;
import com.environmental.fly.adapters.RecentConAdapter;
import com.environmental.fly.databinding.ActivityMainBinding;
import com.environmental.fly.databinding.ActivitySigninBinding;
import com.environmental.fly.listerners.ConListener;
import com.environmental.fly.models.ChatMessage;
import com.environmental.fly.models.User;
import com.environmental.fly.utilities.Constants;
import com.environmental.fly.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import android.util.Base64;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConListener {

    private final String APP_ID = "app56001ca34c0246ae81";
    private final String BANNER_ZONE_ID = "vz83e0b802f93942e59e";

    private AdColonyAdOptions adOptions;
    private AdColonyAdView adView;
    private RelativeLayout adContainer;
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> cM;
    private RecentConAdapter cR;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AdColony.configure(this,APP_ID);
        AdColony.requestAdView(BANNER_ZONE_ID,listener,AdColonyAdSize.BANNER,adOptions);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversations();
    }
    AdColonyAdViewListener listener = new AdColonyAdViewListener(){
        @Override
        public void onRequestFilled(AdColonyAdView ad){
            adContainer = binding.adCont;
            adContainer.addView(ad);
            adView = ad;
        }
    };


    private void init(){
        cM = new ArrayList<>();
        cR = new RecentConAdapter(cM,this);
        binding.conRV.setAdapter(cR);
        db = FirebaseFirestore.getInstance();
    }
    private void setListeners(){
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.newChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UserActivity.class)));
    }
    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void listenConversations() {
            db.collection(Constants.KEY_COLLEGTION_CONVERSATIONS)
                    .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                    .addSnapshotListener(eventListener);
            db.collection(Constants.KEY_COLLEGTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener) ;
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conImg = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }
                    else{
                    chatMessage.conImg = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE) ;
                    chatMessage.conName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                    chatMessage.conId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID) ;
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE) ;
                    chatMessage.dateObj = documentChange.getDocument().getDate(Constants.KEY_TIMESTRAP) ;
                    cM.add(chatMessage);
                    }
                }
                else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < cM.size(); i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(cM.get(i).senderId.equals(senderId) && cM.get(i).receiverId.equals(receiverId)) {
                            cM.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            cM.get(i).dateObj = documentChange.getDocument().getDate(Constants.KEY_TIMESTRAP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(cM,(obj1,obj2) -> obj2.dateObj.compareTo(obj1.dateObj));
            cR.notifyDataSetChanged();
            binding.conRV.smoothScrollToPosition(0);
            binding.conRV.setVisibility(View.VISIBLE);
            binding.probar.setVisibility(View.GONE);
        }
    };
    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FLY_TOKEN, token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        dr.update(Constants.KEY_FLY_TOKEN,token);
    }
    private void signOut(){
        showToast("Signing out...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference dr = db.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
            );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FLY_TOKEN,FieldValue.delete());
        dr.update(updates)
                .addOnSuccessListener(unused ->{
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext() , SigninActivity.class));
                    finish() ;
                })
                .addOnFailureListener(e -> showToast("Unable to sign out")) ;
    }

    @Override
    public void onConClicked(User user){
        Intent i = new Intent(getApplicationContext(), ChatActivity.class);
        i.putExtra(Constants.KEY_USER, user);
        startActivity(i);
    }
}