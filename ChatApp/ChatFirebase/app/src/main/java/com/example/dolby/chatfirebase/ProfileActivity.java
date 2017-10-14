package com.example.dolby.chatfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.Freezable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView mProfileName,mProfileStatus;
    private Button mProfileSendReqBtn,mProfileDeclineBtn;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;

    private ProgressDialog mProgressDialog;

    private FirebaseUser mCurrent_user;


    private String current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_sendReq_btn);
        mProfileDeclineBtn = (Button) findViewById(R.id.profile_declineRequest_btn);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        current_state = "not_friends";

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String displayName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(status);

                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                mProfileDeclineBtn.setEnabled(false);

                if (mCurrent_user.getUid().equals(user_id)){
                    current_state = "your_profile";
                    mProfileSendReqBtn.setText("Your Profile");

                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                    mProfileDeclineBtn.setEnabled(false);

                    mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent settingsIntent = new Intent(ProfileActivity.this,SettingsActivity.class);
                            startActivity(settingsIntent);
                        }
                    });
                }

                //Friends Request
                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")){

                                current_state = "req_received";
                                mProfileSendReqBtn.setText("Accept Frıend Request");

                                mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineBtn.setEnabled(true);

                            } else if (req_type.equals("sent")){
                                current_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Frıend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);
                            }

                            mProgressDialog.dismiss();
                        } else {
                            //already friend durumunu kontrol ediyor
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)){
                                        current_state = "friends";
                                        mProfileSendReqBtn.setText("Already Friend");

                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);

                                    }

                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqBtn.setEnabled(false);

                //SEND REQUEST kısmı
                if(current_state.equals("not_friends")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            current_state = "req_sent";
                                            mProfileSendReqBtn.setText("Cancel Friend Request");

                                            mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                            mProfileDeclineBtn.setEnabled(false);

                                        }
                                    }
                                });

                            } else {
                                Toast.makeText(ProfileActivity.this,"Failed Sending Request.",Toast.LENGTH_LONG).show();
                            }

                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });

                }

                //CANCEL REQUEST kısmı
                if (current_state.equals("req_sent")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            mProfileSendReqBtn.setEnabled(true);
                                            current_state = "not_friends";
                                            mProfileSendReqBtn.setText("Send Friend Request");

                                            mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                            mProfileDeclineBtn.setEnabled(false);

                                        }
                                    }
                                });
                            }
                        }
                    });
                }

                if (current_state.equals("req_received")){

                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();


                    final String currentDate = String.valueOf(date.getDate());
                    final String currentMonth= String.valueOf(date.getMonth());
                    final String currentYear= String.valueOf(date.getYear());

                    //set value
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).child("date").setValue(currentDate+"-"+currentMonth+"-"+currentYear).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                //set value
                                mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).child("date").setValue(currentDate+"-"+currentMonth+"-"+currentYear).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            //del value
                                            mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){

                                                        //del value
                                                        mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){

                                                                    mProfileSendReqBtn.setEnabled(true);
                                                                    current_state = "friends";
                                                                    mProfileSendReqBtn.setText("Already friend");

                                                                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                                                    mProfileDeclineBtn.setEnabled(false);
                                                                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                                                    startActivity(intent);


                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });
                            }
                        }
                    });

                }
            }
        });


        mProfileDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        mProfileSendReqBtn.setEnabled(true);
                                        current_state = "not_friends";
                                        mProfileSendReqBtn.setText("Send Friend Request");

                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);

                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

    }
}
