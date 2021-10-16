package com.example.meetup.Adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.R;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendViewHolder extends RecyclerView.ViewHolder {
    public CircleImageView profileImage;
    public TextView username,profession;

    public FriendViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        profileImage = itemView.findViewById(R.id.profileImageFri);
        username = itemView.findViewById(R.id.usernameFri);
        profession = itemView.findViewById(R.id.professionFri);
    }
}
