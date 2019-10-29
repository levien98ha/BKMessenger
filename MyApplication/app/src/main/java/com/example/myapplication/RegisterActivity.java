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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button RegisterButton;
    private EditText UserEmail, UserPassword;
    private TextView HaveAnAccount;

    private FirebaseAuth myAuth;
    private DatabaseReference RootReference;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        myAuth = FirebaseAuth.getInstance();
        RootReference =  FirebaseDatabase.getInstance().getReference();

        InitializeFields();
        HaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String pass = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter Email !", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Please enter Password !", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Register for BKMessenger");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            myAuth.createUserWithEmailAndPassword(email,pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String currentUserId = myAuth.getCurrentUser().getUid();
                                RootReference.child("Users").child(currentUserId).setValue("");
                                SendUserToMainActivity();

                                Toast.makeText(RegisterActivity.this, "Register account is complete", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        RegisterButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText)findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        HaveAnAccount =(TextView) findViewById(R.id.have_account);

        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToLoginActivity() {
        Intent LoginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(LoginIntent);
    }
    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
