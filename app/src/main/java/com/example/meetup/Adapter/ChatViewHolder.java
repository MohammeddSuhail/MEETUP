package com.example.meetup.Adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.R;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView firstUserProfile,secondUserProfile;
    public TextView firstUserText,secondUserText;

    public ChatViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);

        firstUserProfile = itemView.findViewById(R.id.firstUserProfile);
        secondUserProfile = itemView.findViewById(R.id.secondUserProfile);
        firstUserText = itemView.findViewById(R.id.firstUserText);
        secondUserText = itemView.findViewById(R.id.secondUserText);
    }
}
