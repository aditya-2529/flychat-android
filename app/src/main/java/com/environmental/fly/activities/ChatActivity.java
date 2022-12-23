package com.environmental.fly.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.environmental.fly.adapters.ChatAdapter;
import com.environmental.fly.databinding.ActivityChatBinding;
import com.environmental.fly.models.ChatMessage;
import com.environmental.fly.models.User;
import com.environmental.fly.network.ApiClient;
import com.environmental.fly.network.ApiService;
import com.environmental.fly.utilities.Constants;
import com.environmental.fly.utilities.PreferenceManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User recUser;
    private List<ChatMessage> cM;
    private ChatAdapter cA;
    private PreferenceManager pM;
    private FirebaseFirestore db;
    private String conId = null;
    private Boolean isRecAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();
        loadRecDetails();
        init();
        listenMessages();
    }

//    private void sendNotification(String msgBody){
//        ApiClient.getClient().create(ApiService.class).sendMessage(
//                Constants.getRemoteMsgHeaders(),
//                msgBody
//        ).enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
//                if(response.isSuccessful()){
//                    try {
//                        if(response.body() != null){
//                            JSONObject resJSON = new JSONObject(response.body());
//                            JSONArray res = resJSON.getJSONArray("results");
//                            if(resJSON.getInt("failure") == 1){
//                                JSONObject err = (JSONObject) res.get(0);
//                                Toast.makeText(getApplicationContext(), , Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                        }
//                    }catch(JSONException e){
//                        e.printStackTrace();
//                    }
//                    Toast.makeText(getApplicationContext(), "Notification sent successfully", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Error: "+response.code(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
//                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
    private void init(){
        pM = new PreferenceManager(getApplicationContext());
        cM = new ArrayList<>();
        cA = new ChatAdapter(
                cM,
                getBitmapFromEncodedString(recUser.image),
                pM.getString(Constants.KEY_USER_ID)
        );
        binding.crv.setAdapter(cA);
        db = FirebaseFirestore.getInstance();
    }
    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, pM.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, recUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inMessage.getText().toString());
        message.put(Constants.KEY_TIMESTRAP, new Date());
        db.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conId != null){
            uodateCon(binding.inMessage.getText().toString());
        } else {
            HashMap<String , Object> con = new HashMap<>();
            con.put(Constants.KEY_SENDER_ID, pM.getString(Constants.KEY_USER_ID));
            con.put(Constants.KEY_SENDER_NAME, pM.getString( Constants.KEY_NAME));
            con.put(Constants.KEY_SENDER_IMAGE, pM.getString( Constants.KEY_IMAGE) ) ;
            con.put(Constants.KEY_RECEIVER_ID, recUser.id);
            con.put(Constants.KEY_RECEIVER_NAME, recUser.name );
            con.put(Constants.KEY_RECEIVER_IMAGE, recUser.image);
            con.put(Constants.KEY_LAST_MESSAGE, binding.inMessage.getText().toString());
            con.put(Constants.KEY_TIMESTRAP, new Date());
            addCon(con);
        }
        if(!isRecAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(recUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, pM.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, pM.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FLY_TOKEN, pM.getString(Constants.KEY_FLY_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

//                sendNotification(body.toString());
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            binding.inMessage.setText(null);
        }

    }

    private void listenAvaliabilityOfReceiver(){
        db.collection(Constants.KEY_COLLECTION_USERS).document(recUser.id)
                .addSnapshotListener(ChatActivity.this,(value,error) -> {
                    if(error != null){
                        return;
                    } else{
                        if(value.getLong(Constants.KEY_AVAILABILITY) != null){
                            int ava = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY))
                                    .intValue();
                            isRecAvailable = ava == 1;
                        }
                        recUser.token = value.getString(Constants.KEY_FLY_TOKEN);
                        if(recUser.image == null){
                            recUser.image = value.getString(Constants.KEY_IMAGE);
                            cA.setRPI(getBitmapFromEncodedString(recUser.image));
                            cA.notifyItemRangeInserted(0,cM.size());
                        }
                    }
                    if(isRecAvailable){
                        binding.textAva.setVisibility(View.VISIBLE);
                    } else binding.textAva.setVisibility(View.GONE);
                });
    }

    private void listenMessages(){
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, pM.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, recUser.id)
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, recUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, pM.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null){
                int count = cM.size();
                for(DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                        chatMessage.dateTime = DateFor(documentChange.getDocument().getDate(Constants.KEY_TIMESTRAP)) ;
                        chatMessage.dateObj = documentChange.getDocument().getDate(Constants.KEY_TIMESTRAP);
                        cM.add(chatMessage);
                    }
                }
            Collections.sort(cM,(obj1, obj2) -> obj1.dateObj.compareTo(obj2.dateObj));
                if(count == 0){
                    cA.notifyDataSetChanged();
                } else{
                    cA.notifyItemRangeInserted(cM.size(),cM.size());
                    binding.crv.smoothScrollToPosition(cM.size() - 1);
                }
                binding.crv.setVisibility(View.VISIBLE);
        }
        binding.proBar.setVisibility(View.GONE);
        if(conId == null){
            checkForCon();
        }
    };

    private Bitmap getBitmapFromEncodedString(String encodedImage){
        if(encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }else{
            return null;
        }
    }

    private void loadRecDetails(){
        recUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(recUser.name);
    }
    private void setListener(){
        binding.imageBack.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),UserActivity.class)));
        binding.laySend.setOnClickListener(v -> sendMessage());
    }
    private void addCon(HashMap<String, Object> con){
        db.collection(Constants.KEY_COLLEGTION_CONVERSATIONS)
                .add(con)
                .addOnSuccessListener(documentReference -> conId = documentReference.getId());
    }
    private void uodateCon(String con){
        DocumentReference dR = db.collection(Constants.KEY_COLLEGTION_CONVERSATIONS).document(conId);
        dR.update(
                Constants.KEY_LAST_MESSAGE, con,
                Constants.KEY_TIMESTRAP, new Date()
        );
    }
    private void checkForCon(){
        if(cM.size() != 0){
            checkForConRemotely(
                    pM.getString(Constants.KEY_USER_ID),
                    recUser.id
            );
            checkForConRemotely(
                    recUser.id,
                    pM.getString(Constants.KEY_USER_ID)
            );
        }
    }
    private void checkForConRemotely(String senderId, String recId){
        db.collection(Constants.KEY_COLLEGTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,recId)
                .get()
                .addOnCompleteListener(conComplete);
    }
    private final OnCompleteListener<QuerySnapshot> conComplete = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot dS = task.getResult().getDocuments().get(0);
            conId = dS.getId();
        }
    };

    private String DateFor(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm s", Locale.getDefault()).format(date);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvaliabilityOfReceiver();
    }
}