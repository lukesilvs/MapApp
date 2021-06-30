package com.ldsilver.mapboxtrial;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword_Activity extends AppCompatActivity
{
    private EditText et_email;
    private ProgressBar progressBar;

    FirebaseAuth Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        et_email = findViewById(R.id.editText_ResetEmailAddress);

        /*
        TextView backToLogin = findViewById(R.id.btn_BackToLogin);
        backToLogin.setOnClickListener(v -> {
            Intent myIntent = new Intent(ForgotPassword_Activity.this, Login_Activity.class);
            startActivity(myIntent);
        });*/

        Button buttonResetPassword = findViewById(R.id.btn_ResetPassword);
        buttonResetPassword.setOnClickListener(v -> resetPassword());

        progressBar = findViewById(R.id.progressBar_Reset);

        Auth = FirebaseAuth.getInstance();
    }

    private void resetPassword() {
        String email = et_email.getText().toString().trim();

        // if fields are empty
        if(email.isEmpty()){
            et_email.setError("Email Address is required!");
            et_email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_email.setError("Please enter a valid email address!");
            et_email.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // reset password in Firebase database
        Auth.sendPasswordResetEmail(email).addOnCompleteListener(task1 -> {
            if(task1.isSuccessful()) {
                Toast.makeText(ForgotPassword_Activity.this,
                        "A link has been sent to this email address to reset your password."
                        ,Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(ForgotPassword_Activity.this,
                        "Please try again."
                        ,Toast.LENGTH_SHORT).show();
            }
        });
    }

}