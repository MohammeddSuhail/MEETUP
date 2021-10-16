package com.example.meetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetup.databinding.ActivitySetUpBinding;
import com.example.meetup.databinding.ActivityViewFriendBinding;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewFriendActivity extends AppCompatActivity {

    ActivityViewFriendBinding binding;

    DatabaseReference mUserRef,requestRef,friendRef,cUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String profileImageUrl,username,city,country,profession;

    String CurrentState = "nothing_happened";

    String currentProfileImageUrl,cUserProfession,currentUsername;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        userId = getIntent().getStringExtra("userKey");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        //other guy's ref
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        //our ref
        cUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid());
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        findUserInfo();

        loadUser();

        CheckUserExistance(userId);

        binding.btnPerform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PerformAction(userId);
            }
        });

        binding.btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Unfriend(userId);
            }
        });



    }

    private void Unfriend(String userId) {
        //if he is friend, the if we press btnDecline, then we unfriend him
        //that is removing him from under our id in friends and removing myself from under his id
        if(CurrentState == "friend"){
            friendRef.child(mUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //done removing him from under our id, now removing myself from under his id
                        friendRef.child(userId).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(ViewFriendActivity.this,"You are Unfriended",Toast.LENGTH_SHORT).show();
                                    CurrentState = "nothing_happened";
                                    binding.btnPerform.setText("Send Friend Request");
                                    binding.btnDecline.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }
            });
        }

        //we can decline friend request ,if someone sends
        if(CurrentState == "he_sent_pending"){
            HashMap hashMap = new HashMap();
            hashMap.put("status","decline");
            requestRef.child(userId).child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull @NotNull Task task) {
                    Toast.makeText(ViewFriendActivity.this,"You have declined the friend request",Toast.LENGTH_SHORT).show();
                    CurrentState = "he_sent_decline";
                    binding.btnPerform.setVisibility(View.GONE);
                    binding.btnDecline.setVisibility(View.GONE);
                }
            });
        }

    }

    private void findUserInfo() {
        cUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    currentProfileImageUrl = snapshot.child("profileImage").getValue().toString();
                    currentUsername = snapshot.child("userName").getValue().toString();
                    cUserProfession = snapshot.child("profession").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    //for loading the info of that user in the views
    private void loadUser() {
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    username = snapshot.child("userName").getValue().toString();
                    city = snapshot.child("city").getValue().toString();
                    country = snapshot.child("country").getValue().toString();
                    profession = snapshot.child("profession").getValue().toString();

                    Picasso.get().load(profileImageUrl).into(binding.profileImageV);
                    binding.usernameV.setText(username);
                    binding.address.setText(city+", "+country);

                }else{
                    Toast.makeText(ViewFriendActivity.this,"Data not found",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(ViewFriendActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }



    //what to do when perform btn is clicked
    private void PerformAction(String userId) {
        //if user is sending request for first time for that guy
        if (CurrentState.equals("nothing_happened")){
            HashMap hashMap = new HashMap();
            hashMap.put("status","pending");
            //put Requests->ourId->thatGuyId->CurrentState="I_sent_pending"
            requestRef.child(mUser.getUid()).child(userId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull @NotNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ViewFriendActivity.this,"Friend Request Sent",Toast.LENGTH_SHORT).show();
                        binding.btnDecline.setVisibility(View.GONE);
                        CurrentState = "I_sent_pending";
                        binding.btnPerform.setText("Cancel Friend Request");
                    }else{
                        Toast.makeText(ViewFriendActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        //if request already sent and its pending or request declined ,then remove that guy id from yourId->that_guy_id
        if(CurrentState.equals("I_sent_pending") || CurrentState.equals("I_sent_decline")){
            requestRef.child(mUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ViewFriendActivity.this,"You Have Cancelled Friend Request",Toast.LENGTH_SHORT).show();
                        CurrentState = "nothing_happened";
                        binding.btnPerform.setText("Send Friend Request");
                        binding.btnDecline.setVisibility(View.GONE);
                    }else{
                        Toast.makeText(ViewFriendActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        //if he sent request ,if u press btnPerform again ,then he will be your friend, so add his id to Friends->ourId->hisId
        if(CurrentState.equals("he_sent_pending")){
            requestRef.child(userId).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()){

                        //adding my friend info into friends->my id->his id->info
                        HashMap hashMap = new HashMap();
                        hashMap.put("status","friend");
                        hashMap.put("username",username);
                        hashMap.put("profileImageUrl",profileImageUrl);
                        hashMap.put("profession",profession);
                        friendRef.child(mUser.getUid()).child(userId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task task) {
                                if(task.isSuccessful()){
                                    //adding my info into friends->his id->my id->info
                                    HashMap hashMap2 = new HashMap();
                                    hashMap2.put("status","friend");
                                    hashMap2.put("username",currentUsername);
                                    hashMap2.put("profileImageUrl",currentProfileImageUrl);
                                    hashMap2.put("profession",cUserProfession);
                                    friendRef.child(userId).child(mUser.getUid()).updateChildren(hashMap2).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task task) {
                                            CurrentState = "friend";
                                            binding.btnPerform.setText("Send SMS");
                                            binding.btnDecline.setText("Unfriend");
                                            binding.btnDecline.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }

        //if the the guy is already a friend
        if(CurrentState.equals("friend")){
            Intent intent = new Intent(ViewFriendActivity.this, ChatActivity.class);
            intent.putExtra("OtherUserId",userId);
            startActivity(intent);
        }


    }





    private void CheckUserExistance(String userId) {

        //if already a friend i.e, if he is my friend list, then we are friend
        friendRef.child(mUser.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    CurrentState = "friend";
                    binding.btnPerform.setText("Chat");
                    binding.btnDecline.setText("Unfriend");
                    binding.btnDecline.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //if we are his friend list, then we are friend
        friendRef.child(userId).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    CurrentState = "friend";
                    binding.btnPerform.setText("Chat");
                    binding.btnDecline.setText("Unfriend");
                    binding.btnDecline.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //checking from our end
        requestRef.child(mUser.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //we have sent friend request and it's in pending
                    if(snapshot.child("status").getValue().toString().equals("pending")){
                        CurrentState = "I_sent_pending";
                        binding.btnPerform.setText("Cancel Friend Request");
                        binding.btnDecline.setVisibility(View.GONE);
                    }

                    //we have sent decline
                    if(snapshot.child("status").getValue().toString().equals("decline")){
                        CurrentState = "I_sent_decline";
                        binding.btnPerform.setText("Cancel Friend Request");
                        binding.btnDecline.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        //checking if he's sent
        requestRef.child(userId).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //we have received friend request and it's in pending
                    if(snapshot.child("status").getValue().toString().equals("pending")){
                        CurrentState = "he_sent_pending";
                        binding.btnPerform.setText("Accept Friend Request");
                        binding.btnDecline.setText("Decline Friend");
                        binding.btnDecline.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        if(CurrentState.equals("nothing_happened")){
            CurrentState = "nothing_happened";
            binding.btnPerform.setText("Send Friend Request");
            binding.btnDecline.setVisibility(View.GONE);
        }
    }
}