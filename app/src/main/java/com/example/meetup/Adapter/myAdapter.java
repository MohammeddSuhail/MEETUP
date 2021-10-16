package com.example.meetup.Adapter;

import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.ImageViewActivity;
import com.example.meetup.R;
import com.example.meetup.framents.HomeFragment;
import com.example.meetup.model.Comment;
import com.example.meetup.model.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class myAdapter extends FirebaseRecyclerAdapter<Posts,myAdapter.MyViewHolder> {

    DatabaseReference likeRef;
    DatabaseReference commentsRef;

    FirebaseRecyclerOptions<Comment>CommentOption;
    FirebaseRecyclerAdapter<Comment,CommentViewHolder> CommentAdapter;

    public RecyclerView recyclerViewCom;


    public myAdapter(@NonNull FirebaseRecyclerOptions<Posts> options) {
        super(options);
    }

    @Override

    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NotNull Posts model) {

        //loading the post
        final String postKey = getRef(position).getKey();
        holder.postDesc.setText(model.getPostDec());
        String timeAgo = getTimeAgo(model.getDatePost());
        holder.timeAgo.setText(timeAgo);
        holder.username.setText(model.getUsername());

        Picasso.get().load(model.getPostImageUrl()).into(holder.postImage);
        Picasso.get().load(model.getUserProfileImage()).into(holder.profileImage);

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),ImageViewActivity.class);
                intent.putExtra("url",model.getPostImageUrl());
                v.getContext().startActivity(intent);
            }
        });

        //creating Likes folder in database
        likeRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        commentsRef=FirebaseDatabase.getInstance().getReference().child("Comments");
        //getting id
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //for changing the color of like adding and removing the likes(as well as user) from Likes folder in firebase
        holder.likeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeRef.child(postKey).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            likeRef.child(postKey).child(userId).removeValue();
                            holder.likeImage.setColorFilter(Color.GRAY);
                            notifyDataSetChanged();
                        }else{
                            likeRef.child(postKey).child(userId).setValue("like");
                            holder.likeImage.setColorFilter(Color.GREEN);
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Toast.makeText(v.getContext(), ""+error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //count the likes and changes it's color
        holder.countLikes(postKey,userId,likeRef);

        holder.commentSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = holder.inputComment.getText().toString();
                if(comment.isEmpty()){
                    Toast.makeText(v.getContext(), "Enter the comment",Toast.LENGTH_SHORT).show();
                }else{
                    addComment(holder,postKey,commentsRef,userId,comment);
                }
            }
        });

        //loading comment
        loadComment(postKey);
        //counting likes and setting commentCounter
        holder.countComments(postKey,userId);
    }


    private void addComment(MyViewHolder holder, String postKey, DatabaseReference commentsRef, String userId, String comment) {
        HashMap hashMap = new HashMap();
        hashMap.put("username", HomeFragment.usernameV);
        hashMap.put("profileImage",HomeFragment.profileImageUrlV);
        hashMap.put("comment",comment);

        commentsRef.child(postKey).child(userId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull @NotNull Task task) {
                if (task.isSuccessful()){
                    Toast.makeText(holder.commentImage.getContext(),"Comment Added",Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                    holder.inputComment.setText(null);
                }else{
                    Toast.makeText(holder.commentImage.getContext(),task.getException().toString(),Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private String getTimeAgo(String datePost) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyy hh:mm:ss");
        try {
            long time = sdf.parse(datePost).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return ago+"";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_row,parent,false);
        return new MyViewHolder(view);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        ImageView postImage,likeImage,commentImage,commentSend;
        TextView username,timeAgo,postDesc,likeCounter,commentsCounter;
        EditText inputComment;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImagePost);
            postImage = itemView.findViewById(R.id.postImage);
            username = itemView.findViewById(R.id.profileUsername);
            timeAgo = itemView.findViewById(R.id.timeAgo);
            postDesc = itemView.findViewById(R.id.postDesc);
            likeImage = itemView.findViewById(R.id.likeImage);
            commentImage = itemView.findViewById(R.id.commentsImage);
            likeCounter = itemView.findViewById(R.id.likeCounter);
            commentsCounter = itemView.findViewById(R.id.commentCounter);
            commentSend = itemView.findViewById(R.id.commentSend);
            inputComment = itemView.findViewById(R.id.inputComment);
            recyclerViewCom = itemView.findViewById(R.id.recyclerViewComments);

        }

        public void countLikes(String postKey, String userId, DatabaseReference likeRef) {
            likeRef.child(postKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    //for setting likes
                    if (snapshot.exists()){
                        int totalLikes = (int) snapshot.getChildrenCount();
                        likeCounter.setText(totalLikes+"");
                    }else{
                        likeCounter.setText("0");
                    }

                    //for setting color of likes
                    if (snapshot.child(userId).exists()){
                        likeImage.setColorFilter(Color.GREEN);
                    }else{
                        likeImage.setColorFilter(Color.GRAY);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });


        }

        public void countComments(String postKey, String userId) {
            commentsRef.child(postKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        int totalComs = (int) snapshot.getChildrenCount();
                        commentsCounter.setText(""+totalComs);
                    }else{
                        commentsCounter.setText("0");
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    }

    private void loadComment(String postKey) {
        recyclerViewCom.setLayoutManager(new LinearLayoutManager(recyclerViewCom.getContext()));
        CommentOption = new FirebaseRecyclerOptions.Builder<Comment>().setQuery(commentsRef.child(postKey), Comment.class).build();
        CommentAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(CommentOption) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull CommentViewHolder holder, int position, @NonNull @NotNull Comment model) {
                Picasso.get().load(model.getProfileImage()).into(holder.profileImage);
                holder.username.setText(model.getUsername());
                holder.comment.setText(model.getComment());
            }

            @NonNull
            @NotNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_comment, parent, false);
                return new CommentViewHolder(view);
            }
        };
        CommentAdapter.startListening();
        recyclerViewCom.setAdapter(CommentAdapter);
    }
}
