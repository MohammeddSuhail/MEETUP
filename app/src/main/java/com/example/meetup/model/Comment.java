package com.example.meetup.model;

import com.example.meetup.framents.HomeFragment;

import java.util.HashMap;

public class Comment {
        String username,profileImage,comment;

        public Comment() {
        }

        public Comment(String username, String profileImage, String comment) {
            this.username = username;
            this.profileImage = profileImage;
            this.comment = comment;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getProfileImage() {
            return profileImage;
        }

        public void setProfileImage(String profileImage) {
            this.profileImage = profileImage;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
}
