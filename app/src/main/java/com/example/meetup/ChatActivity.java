package com.example.meetup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.meetup.Adapter.ChatViewHolder;
import com.example.meetup.databinding.ActivityChatBinding;
import com.example.meetup.model.Chat;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    String otherUserId;

    DatabaseReference mUserRef,smsRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    String OtherUserName,OtherUserProfileImageUrl;

    String OurImageUrl;

    String interest;

    //for FireBase Recycler
    FirebaseRecyclerOptions<Chat>options;
    FirebaseRecyclerAdapter<Chat, ChatViewHolder>adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        interest = AllActivity.interest;

        otherUserId = getIntent().getStringExtra("OtherUserId");

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        smsRef = FirebaseDatabase.getInstance().getReference().child("Message");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));

        OurInfo();
        LoadOtherUser();

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMS();
            }
        });

        //for displaying the messages
        loadSms();
    }



    //just retrieving our profileImage
    private void OurInfo() {
        mUserRef.child(interest).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    OurImageUrl = snapshot.child("profileImage").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(ChatActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    //loading the profile pic and name of the other user
    private void LoadOtherUser() {

        mUserRef.child(interest).child(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    OtherUserProfileImageUrl = snapshot.child("profileImage").getValue().toString();
                    OtherUserName = snapshot.child("userName").getValue().toString();

                    binding.usernameAppbar.setText(OtherUserName);
                    Picasso.get().load(OtherUserProfileImageUrl).into(binding.userProfileImageAppbar);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(ChatActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }







    //sending the message
    private void sendSMS() {
        String sms = binding.inputSms.getText().toString();
        if(sms.isEmpty()){
            Toast.makeText(ChatActivity.this,"Please Write Something",Toast.LENGTH_SHORT).show();
        }else{
            HashMap hashMap = new HashMap();
            hashMap.put("sms",sms);
            hashMap.put("userId",mUser.getUid());

            //we are adding the sms and userId(who has sent) in both the users inside Message
            smsRef.child(otherUserId).child(mUser.getUid()).push().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull @NotNull Task task) {
                    if(task.isSuccessful()){
                        //done adding  sms and userId(who has sent) for one, now for other one
                        smsRef.child(mUser.getUid()).child(otherUserId).push().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task task) {
                                if(task.isSuccessful()){
                                    binding.inputSms.setText(null);
                                    Toast.makeText(ChatActivity.this,"Message Sent",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }




    //just loading the message
    //we go to Message -> our id-> hid id->sms and id ,based on we'll know who the message him or me
    private void loadSms() {
        options= new FirebaseRecyclerOptions.Builder<Chat>().setQuery(smsRef.child(mUser.getUid()).child(otherUserId),Chat.class).build();
        adapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull ChatViewHolder holder, int position, @NonNull @NotNull Chat model) {

                if(model.getUserId().equals(mUser.getUid())){
                    //if user is the one who sent the message
                    holder.firstUserText.setVisibility(View.GONE);
                    holder.firstUserProfile.setVisibility(View.GONE);
                    holder.secondUserProfile.setVisibility(View.VISIBLE);
                    holder.secondUserText.setVisibility(View.VISIBLE);

                    holder.secondUserText.setText(model.getSms());
                    Picasso.get().load(OurImageUrl).into(holder.secondUserProfile);
                }else{

                    //if other guy sent u the message
                    holder.firstUserText.setVisibility(View.VISIBLE);
                    holder.firstUserText.setVisibility(View.VISIBLE);
                    holder.secondUserProfile.setVisibility(View.GONE);
                    holder.secondUserText.setVisibility(View.GONE);

                    holder.firstUserText.setText(model.getSms());
                    Picasso.get().load(OtherUserProfileImageUrl).into(holder.firstUserProfile);
                }
            }

            @NonNull
            @NotNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_row,parent,false);
                return new ChatViewHolder(view);
            }
        };
        adapter.startListening();
        binding.recyclerViewChat.setAdapter(adapter);
    }
}