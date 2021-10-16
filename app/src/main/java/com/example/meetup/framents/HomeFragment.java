package com.example.meetup.framents;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.AllActivity;
import com.example.meetup.R;
import com.example.meetup.model.Posts;
import com.example.meetup.Adapter.myAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private static final int REQUEST_CODE = 101;
    ImageView addImagePost,sendImagePost;
    EditText inputPostDesc;

    ProgressDialog mLoadingBar;

    Uri imageUri;
    public static String profileImageUrlV,usernameV;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef,PostRef;
    StorageReference postImgRef;
    String interest;

    RecyclerView recyclerView;

    myAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment,container,false);

        addImagePost = view.findViewById(R.id.add_image_to_post);
        inputPostDesc = view.findViewById(R.id.text_to_post);
        sendImagePost = view.findViewById(R.id.send_img);

        interest = AllActivity.interest;

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postImgRef = FirebaseStorage.getInstance().getReference().child("PostImage");
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");


        mLoadingBar =  new ProgressDialog(getContext());
        //Below 3 lines for Firebase RecycleView
        recyclerView = view.findViewById(R.id.recylerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(PostRef.child(interest),Posts.class).build();




        sendImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPost();
            }
        });

        addImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_CODE);
            }
        });

        mRef.child(interest).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                profileImageUrlV = snapshot.child("profileImage").getValue().toString();
                usernameV = snapshot.child("userName").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),"Sorry Something Went Wrong",Toast.LENGTH_SHORT).show();
            }
        });

        adapter = new myAdapter(options);
        recyclerView.setAdapter(adapter);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.startListening();
    }


    private void AddPost() {

        String postDesc = inputPostDesc.getText().toString();
        if(postDesc.isEmpty()){
            postDesc=" ";
        }else if(imageUri==null){
            Toast.makeText(getContext(),"Select a image",Toast.LENGTH_SHORT).show();
        }
        else{
            mLoadingBar.setTitle("Posting");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();

            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyy hh:mm:ss");
            String strDate = formatter.format(date);

            String finalPostDesc = postDesc;
            postImgRef.child(mUser.getUid()+strDate).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){
                        postImgRef.child(mUser.getUid()+strDate).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                HashMap hashMap = new HashMap();
                                hashMap.put("datePost",strDate);
                                hashMap.put("postImageUrl",uri.toString());
                                hashMap.put("postDec", finalPostDesc);
                                hashMap.put("userProfileImage",profileImageUrlV);
                                hashMap.put("username",usernameV);

                                PostRef.child(interest).child(mUser.getUid()+strDate).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if(task.isSuccessful()){
                                            mLoadingBar.dismiss();
                                            Toast.makeText(getContext(),"Post added",Toast.LENGTH_SHORT).show();
                                            addImagePost.setImageResource(R.drawable.ic_add_post_image);
                                            inputPostDesc.setText("");
                                        }
                                        else{
                                            mLoadingBar.dismiss();
                                            Toast.makeText(getContext(),""+task.getException().toString(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }else{
                        mLoadingBar.dismiss();
                        Toast.makeText(getContext(),""+task.getException().toString(),Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE && resultCode==RESULT_OK && data!=null){
            imageUri = data.getData();
            addImagePost.setImageURI(imageUri);
        }
    }
}
