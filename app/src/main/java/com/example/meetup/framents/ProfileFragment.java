package com.example.meetup.framents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.meetup.AllActivity;
import com.example.meetup.R;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class ProfileFragment extends Fragment {

    CircleImageView profileImageView;
    EditText inputUsername,inputCity,inputCountry,inputProfession;
    Button btnUpdate;

    DatabaseReference mUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String interest;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment,container,false);

        profileImageView = view.findViewById(R.id.circleImageView);
        inputUsername = view.findViewById(R.id.inputUsername);
        inputCity = view.findViewById(R.id.inputCity);
        inputCountry = view.findViewById(R.id.inputCountry);
        inputProfession = view.findViewById(R.id.inputProfession);
        btnUpdate = view.findViewById(R.id.btnUpdate);

        interest = AllActivity.interest;

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //getting the user details and putting in the views of profile
        mUserRef.child(interest).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    String city = snapshot.child("city").getValue().toString();
                    String country = snapshot.child("country").getValue().toString();
                    String profession = snapshot.child("profession").getValue().toString();
                    String username = snapshot.child("userName").getValue().toString();

                    Picasso.get().load(profileImageUrl).into(profileImageView);
                    inputCity.setText(city);
                    inputUsername.setText(username);
                    inputCountry.setText(country);
                    inputProfession.setText(profession);
                }else{
                    Toast.makeText(getContext(),"Data doesn't exist",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getContext(),error.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = inputCity.getText().toString();
                String country = inputCountry.getText().toString();
                String profession = inputProfession.getText().toString();
                String username = inputUsername.getText().toString();

                //userName,city,country,profession,status, profileImage,interest;
                HashMap hashMap = new HashMap();
                hashMap.put("city",city);
                hashMap.put("country",country);
                hashMap.put("profession",profession);
                hashMap.put("userName",username);

                mUserRef.child(interest).child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(getContext(),"Updated successfully",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        return view;
    }
}
