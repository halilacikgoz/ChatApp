package com.example.dolby.chatfirebase;

//KAYIT OLMA SAYFASI


import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName, mEmail, mPassword;
    private Button mCreateBtn;

    private FirebaseAuth mAuth;

    private ProgressDialog mRegProgress;

    private Toolbar mToolbar;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Firebase auth bağlantısı için
        mAuth = FirebaseAuth.getInstance();

        //Kendi toolbarımızı oluşturduk activity_register.xml in içinde main_page_toolbar var , onuda app_bar_layout.xml den olşturduk.
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        //Toolbarın title ını koyduk
        getSupportActionBar().setTitle("Create Account");
        //Toolbardaki sol üstteki geri butonunu oluşturdu. Manifest in içinde parentActivityName olarak gideceği activity i belirledik.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Progress Dialog oluşturmak(o yükleniyor diye dönen işaret)
        mRegProgress = new ProgressDialog(this);


        mDisplayName = (TextInputLayout) findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mCreateBtn = (Button) findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                //display_name , email ve password empty mi degil mi onu kontrol ediyor
                if(!TextUtils.isEmpty(display_name)||!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){

                    //Progress dialog ayarları yapıyoruz
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account!");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    //Firebase e bilgileri yollamak için bu method u oluşturduk.
                    register_user(display_name,email,password);
                }
            }
        });
    }

    private void register_user(final String display_name, String email, String password){

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {


                //Successful olup olmadıgına firebase otomatik karar veriyor
                //Mail yerine mail girdiginde ve password 6 haneden büyük olursa kabul eder
                if(task.isSuccessful()){

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


                    //Bu hash map ile
                    HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("name", display_name);
                    userMap.put("status","Hi, I am using HorHor");

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                 //Progress dialog u kapatmak için çünkü artık login olduk
                                mRegProgress.dismiss();

                                //Kayıt başarılı olursa main activity ye geçiş yapıyoruz
                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);

                                //Bu kod yeni sayfaya geçmeden önceki tüm process leri kapatmayı sağlıyor geri tuşuna bastıgımızda eski sayfaya gitmiyor
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                startActivity(mainIntent);
                                finish();

                            }
                        }
                    });

                } else{
                    //Burda kapatmıyoruz hide yapıyoruz çünkü main activity e geciş yapamadık
                    mRegProgress.hide();

                    //Toast ile ekrana hata basıyoruz
                    Toast.makeText(RegisterActivity.this,"Connot Sign in. Please check the form and try again.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
