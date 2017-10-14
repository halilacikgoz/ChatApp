package com.example.dolby.chatfirebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private String mChatUser,mUserName,mCurrentUserId;

    private Toolbar mChatToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;

    private ImageButton mChatAddBtn,mChatSendBtn;
    private EditText mChatMessage;

    private RecyclerView mMessagesList;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private MessagesImageAdapter messagesAdapter;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl( "gs://chatfirebase-69ff4.appspot.com" );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatUser = getIntent().getStringExtra("user_id");
        mUserName = getIntent().getStringExtra("user_name");

        mChatAddBtn = (ImageButton) findViewById(R.id.chat_addImageBtn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_sendImageBtn);
        mChatMessage = (EditText) findViewById(R.id.chat_editText);

        mAdapter = new MessageAdapter(messagesList);

        mChatToolbar = (Toolbar) findViewById(R.id.chat_appBar);
        setSupportActionBar(mChatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mUserName);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);
        mLinearLayout.setStackFromEnd(true);




        mCurrentUserId = mAuth.getCurrentUser().getUid();

        loadMessages();


        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendPic();

            }
        });
    }

    private void sendPic() {

        Intent takePictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        if ( takePictureIntent.resolveActivity( getPackageManager() ) != null )
        {
            startActivityForResult( takePictureIntent, REQUEST_IMAGE_CAPTURE );
        }

    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        if ( requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK )
        {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get( "data" );
            UploadPostTask uploadPostTask = new UploadPostTask();
            uploadPostTask.execute( imageBitmap );
        }
    }

    @SuppressWarnings("VisibleForTests")
    private class UploadPostTask
            extends AsyncTask<Bitmap, Void, Void>
    {

        @Override
        protected Void doInBackground( Bitmap... params )
        {
            Bitmap bitmap = params[0];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress( Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream );
            storageRef.child( UUID.randomUUID().toString() + "jpg" ).putBytes(
                    byteArrayOutputStream.toByteArray() ).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess( UploadTask.TaskSnapshot taskSnapshot )
                        {
                            if ( taskSnapshot.getDownloadUrl() != null )
                            {
                                String imageUrl = taskSnapshot.getDownloadUrl().toString();
                                final MessageImage message = new MessageImage( imageUrl );
                                runOnUiThread( new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        messagesAdapter.addMessage( message );
                                    }
                                } );
                            }
                        }
                    } );

            return null;
        }
    }

    private void loadMessages() {

        mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String message = mChatMessage.getText().toString();

        if (!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message" , message);
            messageMap.put("type" , "text");
            messageMap.put("from" , mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id , messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id , messageMap);

            mChatMessage.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null){
                        Log.d("CHAT_LOG" , databaseError.getMessage().toString());
                    }
                }
            });
        }
    }
}
