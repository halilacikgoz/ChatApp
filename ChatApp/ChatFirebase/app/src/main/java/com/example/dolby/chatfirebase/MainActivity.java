package com.example.dolby.chatfirebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.app.Notification.FLAG_AUTO_CANCEL;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    String user_id1;
    FirebaseUser currentUser;
    //Bu Viewpager yana kaydırınca sayfalar arası(fragmentler arası) geçişi sağlayan yapı
    private ViewPager mViewPager;

    //Üstteki REQUEST-CHATS-FRIENDS yapısı
    private TabLayout mTabLayout;


    //Friends part
    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private FirebaseAuth mAuthFriends;
    private DatabaseReference mUsersDatabase,databaseReference;

    private String mCurrent_user_id;

    private View mMainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase bağlantısı yaptık
        mAuth = FirebaseAuth.getInstance();

        //Kendi toolbarımızı oluşturduk activity_main.xml in içinde main_page_toolbar var , onuda app_bar_layout.xml den olşturduk.
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        //Toolbarın title ını koyduk
        getSupportActionBar().setTitle("HORHOR CHAT");

        //Friends part
        mFriendsList = (RecyclerView) findViewById(R.id.friends_list);
        mAuthFriends = FirebaseAuth.getInstance();


        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(this));

        currentUser=mAuth.getCurrentUser();
        if(currentUser!=null) {  //if başlngıç
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Friend_Request").child(currentUser.getUid());


            ValueEventListener dinle = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        Log.v("halil", postSnapshot.child("request_type").getValue().toString());
                        // String xx= postSnapshot.child("Friend_request").child(currentUser.getUid()).child("request_type").getValue().toString();
                        //  Log.v("halil344",xx);
                        if (postSnapshot.child("request_type").getValue().toString().equals("received")) {
                            //  Log.v("halil1",databaseReference.child("request_type").toString());

                            user_id1=  postSnapshot.getKey().toString();
                            Log.v("ssss",user_id1);
                            notification();

                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            databaseReference.addValueEventListener(dinle);

        }

    }

    public void notification() {

        Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
        intent.putExtra("user_id",user_id1);


        Log.v("dd",user_id1);
        PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 10, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification.Builder(MainActivity.this)
                .setTicker("Request coming")
                .setContentTitle("Request")
                .setContentText("Someone add you as friend")
                .setSmallIcon(R.drawable.defualt_icon)
                .setContentIntent(pIntent).getNotification();
        notification.flags = FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);


    }

    @Override
    public void onStart() {
        super.onStart();

        //User daha önce girilmiş mi onu kontrol ediyor
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Girilmediyse StartActivity e yolluyoruz
        if(currentUser == null){

            sendToStart();

        } else {

            mCurrent_user_id = mAuthFriends.getCurrentUser().getUid();
            mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);

            //Friends part
            FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends,FriendsViewHolder>(
                    Friends.class,
                    R.layout.users_single_layout,
                    FriendsViewHolder.class,
                    mFriendsDatabase

            ) {


                @Override
                protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, final int i) {

                    friendsViewHolder.setDate(friends.getDate());

                    final String list_user_id = getRef(i).getKey();

                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String userName = dataSnapshot.child("name").getValue().toString();
                            friendsViewHolder.setName(userName);

                            friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Select Options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {

                                            //her item a click yapılınca napıcagını söylüyoz
                                            if (i == 0){

                                                Intent profileIntent = new Intent(MainActivity.this,ProfileActivity.class);
                                                profileIntent.putExtra("user_id",list_user_id);
                                                startActivity(profileIntent);

                                            }

                                            if (i == 1){

                                                Intent chatIntent = new Intent(MainActivity.this,ChatActivity.class);
                                                chatIntent.putExtra("user_id",list_user_id);
                                                chatIntent.putExtra("user_name",userName);
                                                startActivity(chatIntent);

                                            }

                                        }
                                    });

                                    builder.show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            };

            mFriendsList.setAdapter(friendsRecyclerViewAdapter);

        }
    }


    private void sendToStart() {

        //StartActivity açma metodu
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //Optionmenu(sağ üstte çıkan menü) ayarlama yeri
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        //Menüdeki log out butonu seçilirse
        if(item.getItemId() == R.id.main_logout_btn){

            //FirebaseAuth dan hesabı sign out yapıyor ve StartActivity e gidiyor
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if (item.getItemId() == R.id.main_settings_btn){

            Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settingsIntent);

        }

        if (item.getItemId() == R.id.main_all_btn){

            Intent allIntent = new Intent(MainActivity.this,UsersActivity.class);
            startActivity(allIntent);
        }

        return true;
    }
}
