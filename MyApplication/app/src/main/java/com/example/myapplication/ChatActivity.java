package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messagernReceiverId, messagerReceiverName, messagerReceiverImage, messengerSenderId;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private ImageButton SendMessengerBtn, SendFileBtn;
    private EditText InputMessenger;

    private FirebaseAuth myAuth;
    private DatabaseReference RootReference;

    private Toolbar myToolbar;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView userMessagesList;

    private String checker="", myUrl="";
    private StorageTask uploadFile;
    private Uri fileUri;

    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myAuth = FirebaseAuth.getInstance();
        messengerSenderId = myAuth.getCurrentUser().getUid();
        RootReference = FirebaseDatabase.getInstance().getReference();


        messagernReceiverId = getIntent().getExtras().get("user_id_chat").toString();
        messagerReceiverName = getIntent().getExtras().get("user_id_name").toString();
        messagerReceiverImage = getIntent().getExtras().get("user_id_image").toString();

        IntializeControllers();

        DisplayLastSeen();

        userName.setText(messagerReceiverName);
       // userLastSeen.setText(messagernReceiverId);
        Picasso.get().load(messagerReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        RootReference.child("Messages").child(messengerSenderId).child(messagernReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messagesAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        SendMessengerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessenger();
            }
        });

        SendFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "MS Word Files"

                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the file...");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            checker="image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image !"), 439);
                        }
                        if(i == 1){
                            checker="pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File!"), 439);
                        }
                        if(i == 2){
                            checker="docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select Word File!"), 439);
                        }
                    }
                });
                builder.show();
            }
        });

    }

    //get data từ request upload file trả về
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 439 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            loadingBar.setTitle("Sending File....");
            loadingBar.setMessage("Loading...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (!checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messengerSenderRef = "Messages/" + messengerSenderId + "/" + messagernReceiverId;
                final String messengerReceiverRef = "Messages/" + messagernReceiverId + "/" + messengerSenderId;

                DatabaseReference userMessengerKeyRef = RootReference.child("Messages").child(messengerSenderId).child(messagernReceiverId).push();

                final String messagePushId = userMessengerKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + checker);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()){
                                    Uri dowloadUrl = task.getResult();
                                    myUrl = dowloadUrl.toString();

                                    Map messengerTextBody = new HashMap();
                                    messengerTextBody.put("message", myUrl);
                                    messengerTextBody.put("name", fileUri.getLastPathSegment());
                                    messengerTextBody.put("type", checker);
                                    messengerTextBody.put("from", messengerSenderId);
                                    messengerTextBody.put("to", messagernReceiverId);
                                    messengerTextBody.put("messageID", messagePushId);

                                    Map messengerBodyDetails = new HashMap();
                                    messengerBodyDetails.put(messengerSenderRef + "/" + messagePushId, messengerTextBody);
                                    messengerBodyDetails.put(messengerReceiverRef + "/" + messagePushId, messengerTextBody);

                                    RootReference.updateChildren(messengerBodyDetails);
                                    loadingBar.dismiss();
                                }
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0*taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + "% Uploading.....");
                    }
                });
            }
            else if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messengerSenderRef = "Messages/" + messengerSenderId + "/" + messagernReceiverId;
                final String messengerReceiverRef = "Messages/" + messagernReceiverId + "/" + messengerSenderId;

                DatabaseReference userMessengerKeyRef = RootReference.child("Messages").child(messengerSenderId).child(messagernReceiverId).push();

                final String messagePushId = userMessengerKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + "jpg");

                uploadFile = filePath.putFile(fileUri);
                uploadFile.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri dowloadUrl = task.getResult();
                            myUrl = dowloadUrl.toString();

                            Map messengerTextBody = new HashMap();
                            messengerTextBody.put("message", myUrl);
                            messengerTextBody.put("name", fileUri.getLastPathSegment());
                            messengerTextBody.put("type", checker);
                            messengerTextBody.put("from", messengerSenderId);
                            messengerTextBody.put("to", messagernReceiverId);
                            messengerTextBody.put("messageID", messagePushId);

                            Map messengerBodyDetails = new HashMap();
                            messengerBodyDetails.put(messengerSenderRef + "/" + messagePushId, messengerTextBody);
                            messengerBodyDetails.put(messengerReceiverRef + "/" + messagePushId, messengerTextBody);

                            RootReference.updateChildren(messengerBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Your message has been sent!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    InputMessenger.setText("");
                                }
                            });
                        }
                    }
                });
            } else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing Selected!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void IntializeControllers() {

        myToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.chat_bar_layout, null);
        actionBar.setCustomView(actionBarView);

        userImage = (CircleImageView) findViewById(R.id.chat_bar_layout_image);
        userName = (TextView) findViewById(R.id.chat_bar_layout_name);
        userLastSeen = (TextView) findViewById(R.id.chat_bar_layout_last_seen);

        SendFileBtn = (ImageButton) findViewById(R.id.send_files_btn);
        SendMessengerBtn = (ImageButton) findViewById(R.id.send_messenger_btn);
        InputMessenger = (EditText) findViewById(R.id.input_messenger);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messenger_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);

        loadingBar = new ProgressDialog(this);


    }

    @Override
    protected void onStart() {
        super.onStart();


    }


    //gởi tin nhắn kiểu text
    private void SendMessenger(){
        String messengerText = InputMessenger.getText().toString();

        if(TextUtils.isEmpty(messengerText)){
            Toast.makeText(this, "Text your message..!", Toast.LENGTH_SHORT).show();
        }
        else {
            String messengerSenderRef = "Messages/" + messengerSenderId +"/" + messagernReceiverId;
            String messengerReceiverRef = "Messages/" + messagernReceiverId +"/" + messengerSenderId;

            DatabaseReference userMessengerKeyRef = RootReference.child("Messages").child(messengerSenderId).child(messagernReceiverId).push();

            String messagePushId = userMessengerKeyRef.getKey();

            Map messengerTextBody = new HashMap();
            messengerTextBody.put("message", messengerText);
            messengerTextBody.put("type", "text");
            messengerTextBody.put("from", messengerSenderId);
            messengerTextBody.put("to", messagernReceiverId);
            messengerTextBody.put("messageID", messagePushId);
            messengerTextBody.put("name", "text");

            Map messengerBodyDetails = new HashMap();
            messengerBodyDetails.put(messengerSenderRef + "/" + messagePushId, messengerTextBody);
            messengerBodyDetails.put(messengerReceiverRef + "/" + messagePushId, messengerTextBody);

            RootReference.updateChildren(messengerBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Your message has been sent!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    InputMessenger.setText("");
                }
            });

        }
   }


   //hàm set online offline lastseen
   private void DisplayLastSeen(){
        RootReference.child("Users").child(messengerSenderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("userState").hasChild("state")){
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if(state.equals("online"))
                    {
                        userLastSeen.setText("Online");
                    }
                    else if(state.equals("offline"))
                    {
                        userLastSeen.setText("Last seen: "+date+" "+time);
                    }
                }
                else{
                    userLastSeen.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
   }

}
