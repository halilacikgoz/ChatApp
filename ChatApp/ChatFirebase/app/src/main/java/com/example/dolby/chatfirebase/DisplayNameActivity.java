package com.example.dolby.chatfirebase;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DisplayNameActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mDisplayName;
    private Button mSaveBtn;

    private DatabaseReference mDisplayNameDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_name);

        //Firebase den bağlı oldugumuz hesabın unique id sini çekiyoruz
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        //Bu çektigimiz id yi kullanarak bu id nin verilerini çekiyoruz
        mDisplayNameDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //Toolbar ı oluşturduk
        mToolbar = (Toolbar) findViewById(R.id.displayName_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Name");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String displayName_value = getIntent().getStringExtra("displayName_value");

        mDisplayName = (TextInputLayout) findViewById(R.id.displayName_input_layout);
        mDisplayName.getEditText().setText(displayName_value);

        mSaveBtn = (Button) findViewById(R.id.displayName_save_btn);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Progress dialog u oluşturduk
                mProgress = new ProgressDialog(DisplayNameActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save the changes");
                mProgress.show();

                String displayName = mDisplayName.getEditText().getText().toString();

                mDisplayNameDatabase.child("name").setValue(displayName).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            mProgress.dismiss();
                            Toast.makeText(getApplicationContext(), "Your name is changed.",Toast.LENGTH_LONG).show();
                        } else {

                            Toast.makeText(getApplicationContext(), "There was some error in saving changes.",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
    }
}
