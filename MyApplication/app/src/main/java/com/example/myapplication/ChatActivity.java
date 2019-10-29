package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ChatActivity extends AppCompatActivity {

    private String messagernReceiverId, messagerReceiverName;

    private Toolbar myToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messagernReceiverId = getIntent().getExtras().get("user_id_chat").toString();
        messagerReceiverName = getIntent().getExtras().get("user_id_name").toString();

        myToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToMainActivity();
            }
        });
        getSupportActionBar().setTitle(messagerReceiverName);
    }
    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(ChatActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

}
