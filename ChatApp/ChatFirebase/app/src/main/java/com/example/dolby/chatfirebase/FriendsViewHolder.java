package com.example.dolby.chatfirebase;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


public class FriendsViewHolder extends RecyclerView.ViewHolder {

    public FriendsViewHolder(View itemView){
        super(itemView);

    }

    public void setDate(String date){

        TextView userNameView = (TextView) itemView.findViewById(R.id.user_single_status);
        userNameView.setText(date);

    }

    public void setName(String name){
        TextView userNameView = (TextView) itemView.findViewById(R.id.user_single_name);
        userNameView.setText(name);
    }
}
