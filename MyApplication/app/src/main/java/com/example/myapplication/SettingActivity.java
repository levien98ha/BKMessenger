package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Button btnUpdate;
    private EditText userName, userStatus;
    private CircleImageView userImage;

    private String UserCurrentId;

    //firebase
    private FirebaseAuth myAuth;
    private DatabaseReference RootReference;

    //image profile
    private static final int GalleryPick = 1;
    private StorageReference UserImageReference;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        myAuth =  FirebaseAuth.getInstance();
        UserCurrentId = myAuth.getCurrentUser().getUid();
        RootReference = FirebaseDatabase.getInstance().getReference();
        UserImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSetting();
            }
        });
        RetrieveUserInfo();

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }



    private void RetrieveUserInfo() {
        RootReference.child("Users").child(UserCurrentId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveUserImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);

                            Picasso.get().load(retrieveUserImage).into(userImage);

                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);

                        }
                        else{
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingActivity.this, "Please fill your profile information!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void InitializeFields() {
        btnUpdate = (Button) findViewById(R.id.update_profile);
        userName = (EditText) findViewById(R.id.user_name);
        userStatus = (EditText) findViewById(R.id.user_status);
        userImage = (CircleImageView) findViewById(R.id.profile_image);
        loadingBar = new ProgressDialog(this);
    }

    //pick image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GalleryPick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();

            //khoi tao picker crop
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        //Override onActivityResult
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Image for Your Profile");
                loadingBar.setMessage("Loading...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resultUri = result.getUri();
                final StorageReference filePath = UserImageReference.child(UserCurrentId + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = uri.toString();
                                RootReference.child("Users").child(UserCurrentId).child("image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingActivity.this, "Profile image stored to firebase database successfully.", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                    }
                });
            }
                    });
        }
    });}}}


    //click btn Update
    private void UpdateSetting() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your name !", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this, "Please write your status !", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", UserCurrentId);
            profileMap.put("name", setUserName);
            profileMap.put("status", setUserStatus);
            RootReference.child("Users").child(UserCurrentId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(SettingActivity.this, "Your profile update is complete !", Toast.LENGTH_SHORT).show();

                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SendUserToMainActivity();
    }

    //back to main
    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(SettingActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

}
