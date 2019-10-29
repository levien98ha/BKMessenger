package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth myAuth;
    private ProgressDialog loadingBar;

    private Button LoginButton;
    private EditText UserEmail, UserPassword;
    private TextView HaveNotAcoount, ForgetPasswork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myAuth = FirebaseAuth.getInstance();

        InitializeFields();
        HaveNotAcoount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginAccpet();
            }
        });
    }

    private void LoginAccpet() {
        String email = UserEmail.getText().toString();
        String pass = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter Email !", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Please enter Password !", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("SIGN IN BKMessenger");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            myAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "login successful !", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        LoginButton = (Button) findViewById(R.id.login_button);
        UserEmail = (EditText)findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        HaveNotAcoount =(TextView) findViewById(R.id.have_not_account);
        ForgetPasswork = (TextView) findViewById(R.id.forget_password);
        loadingBar = new ProgressDialog(this);
    }


    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(LoginActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
