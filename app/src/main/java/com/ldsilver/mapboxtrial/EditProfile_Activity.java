package com.ldsilver.mapboxtrial;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditProfile_Activity extends AppCompatActivity
{
    private String currentUser;
    private EditText et_firstName, et_lastName, et_emailAddress;
    private TextView tv_resetPassword;

    private Button btnUpdate;

    DatabaseReference userDbRef;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        et_firstName = findViewById(R.id.editText_dbFirstName);
        et_lastName = findViewById(R.id.editText_dbLastName);
        et_emailAddress = findViewById(R.id.editText_dbEmailAddress);
        tv_resetPassword = findViewById(R.id.TextView_updateResetPassword);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userDbRef = database.getReference("Users");

        // Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getEmail();

        // read from the database
        userDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot userDataSnap : snapshot.getChildren())
                {
                    UserAccount tempUser = userDataSnap.getValue(UserAccount.class);
                    if (tempUser.getEmailAddress().equals(currentUser)) {
                        et_firstName.setText(tempUser.getFirstName());
                        et_lastName.setText(tempUser.getLastName());
                        et_emailAddress.setText(tempUser.getEmailAddress());

                        Toast.makeText(EditProfile_Activity.this, "Here are your details!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // failed to read value
            }
        });

        // update button
        btnUpdate = findViewById(R.id.btn_Update);
        btnUpdate.setOnClickListener(v -> {
            UpdateUserInfo();
        });

        // reset password for user profile
        tv_resetPassword.setOnClickListener(view -> {
            Intent intent = new Intent(EditProfile_Activity.this, ForgotPassword_Activity.class);
            startActivity(intent);
        });

        // sign out button
        Button btnLogout = findViewById(R.id.btn_SignOut);
        btnLogout.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("remember", "false");
            editor.apply();

            finish();

            // Start login activity after signing out
            Intent intent = new Intent(EditProfile_Activity.this, Login_Activity.class);
            startActivity(intent);
        });
    }

    // update user method
    private void UpdateUserInfo() {
        final String firstName = et_firstName.getText().toString().trim();
        final String lastName = et_lastName.getText().toString().trim();
        final String email = et_emailAddress.getText().toString().trim();

        // if fields are empty
        if (firstName.isEmpty()){
            et_firstName.setError("First name is required!");
            et_firstName.requestFocus();
            return;
        }
        if (lastName.isEmpty()){
            et_lastName.setError("Last name is required!");
            et_lastName.requestFocus();
            return;
        }
        if (email.isEmpty()){
            et_emailAddress.setError("Email Address is required!");
            et_emailAddress.requestFocus();
            return;
        }

        /*
        // update user info here
        HashMap<String, String> userMap = new HashMap<>();

        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("emailAddress", email);

        userDbRef.setValue(userMap);
        */

        // once info has been updated
        Toast.makeText(EditProfile_Activity.this, "Your details has been updated!",
                Toast.LENGTH_SHORT).show();
    }

}