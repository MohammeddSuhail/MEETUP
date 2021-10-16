package com.example.meetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    DatabaseReference mRef;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                //if user exists then we check if he has filed setup activity, if yes->ChatActivity if no->setUpActivity
                if(mUser!=null){
                    mRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //checking if data is present inside database for the signed in user
                            if(snapshot.exists()){
                                String interest = snapshot.child("interest").getValue().toString();
                                Intent intent = new Intent(SplashScreen.this,AllActivity.class);
                                intent.putExtra("interest",interest);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                Intent intent = new Intent(SplashScreen.this,SetUpActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else{
                    Intent intent = new Intent(SplashScreen.this,SignInActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);

    }
}