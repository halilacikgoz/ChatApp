package com.example.dolby.chatfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mLoginEmail,mLoginPassword;
    private Button mLoginBtn;

    //Firebase auth oluşturduk
    private FirebaseAuth mAuth;

    private ProgressDialog mLoginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Firebase auth u bağladık bu activity e
        mAuth = FirebaseAuth.getInstance();

        //Kendi toolbarımızı oluşturduk activity_register.xml in içinde main_page_toolbar var , onuda app_bar_layout.xml den olşturduk.
        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        //Toolbarın title ını koyduk
        getSupportActionBar().setTitle("Login");
        //Toolbardaki sol üstteki geri butonunu oluşturdu. Manifest in içinde parentActivityName olarak gideceği activity i belirledik.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Progress dialog oluşturduk
        mLoginProgress = new ProgressDialog(this);


        mLoginEmail = (TextInputLayout) findViewById(R.id.login_email);
        mLoginPassword = (TextInputLayout) findViewById(R.id.login_password);

        mLoginBtn= (Button) findViewById(R.id.login_btn);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //email ve password a girilen değerleri çekiyoruz
                String email = mLoginEmail.getEditText().getText().toString();
                String password = mLoginPassword.getEditText().getText().toString();

                //Girilen değerlerin boş olup olmadıgını kontrol ediyoruz
                if(!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){

                    //Progress Dialog ayarları yapıyoruz login olurken çalışacak
                    mLoginProgress.setTitle("Logging In");
                    mLoginProgress.setMessage("Please wait while we check your credentials.");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();

                    //Login olma metodunu çagırdık
                    loginUser(email,password);
                    
                }
            }
        });


    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    //Progress dialog u kapatıyoruz
                    mLoginProgress.dismiss();

                    //MainActiviy e geçiş yapıyoruz login oldugumuz için
                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);

                    //Bu kod yeni sayfaya geçmeden önceki tüm process leri kapatmayı sağlıyor geri tuşuna bastıgımızda eski sayfaya gitmiyor
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(mainIntent);
                    finish();
                } else {

                    //Progress dialog u gizliyoruz tekrar giriş yapmasını istiyoruz
                    mLoginProgress.hide();
                    Toast.makeText(LoginActivity.this,"Cannot Login. Please check the form and try again.", Toast.LENGTH_LONG);
                }
            }
        });
    }
}
