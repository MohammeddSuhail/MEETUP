package com.example.meetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.meetup.databinding.ActivitySignInBinding;
import com.example.meetup.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    private FirebaseAuth mAuth;
    ProgressDialog mLoadingBar;
    FirebaseUser mUser;
    String interest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mLoadingBar =  new ProgressDialog(SignInActivity.this);

        //getInterest();

        binding.newAccountId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignInActivity.this,SignUpActivity.class);
                startActivity(i);
                finish();
            }
        });

        binding.signInButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadingBar.setTitle("Signing In");
                mLoadingBar.setCanceledOnTouchOutside(false);
                mLoadingBar.show();
                //checking if the user is present in firebase
                mAuth.signInWithEmailAndPassword(binding.emailId.getText().toString(),binding.passwordId.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull  Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    mLoadingBar.dismiss();
                                    Intent intent = new Intent(SignInActivity.this,Util.class);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    mLoadingBar.dismiss();
                                    Toast.makeText(SignInActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        //if user is already logged in
        if(mAuth.getCurrentUser()!=null){
            Intent intent = new Intent(SignInActivity.this,AllActivity.class);
            intent.putExtra("interest",interest);
            startActivity(intent);
            finish();
        }

    }

}