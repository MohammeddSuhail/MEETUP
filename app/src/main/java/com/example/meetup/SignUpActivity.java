package com.example.meetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.meetup.databinding.ActivitySignUpBinding;
import com.example.meetup.model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;

    private FirebaseAuth auth;

    ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();

        mLoadingBar =  new ProgressDialog(SignUpActivity.this);

        binding.alreadyAccountId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignUpActivity.this,SignInActivity.class);
                startActivity(i);
                finish();
            }
        });


        binding.signUpButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadingBar.setTitle("Signing Up");
                mLoadingBar.setCanceledOnTouchOutside(false);
                mLoadingBar.show();
                //adding user to (firebase authentication)
                auth.createUserWithEmailAndPassword(binding.emailId.getText().toString(),binding.passwordId.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    //done adding email and password in auth
                                    mLoadingBar.dismiss();
                                    Intent i = new Intent(SignUpActivity.this,SetUpActivity.class);
                                    startActivity(i);
                                    finish();

                                    Toast.makeText(SignUpActivity.this,"Account Sucessfully Created",Toast.LENGTH_SHORT).show();
                                }else{
                                    mLoadingBar.dismiss();
                                    Toast.makeText(SignUpActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}