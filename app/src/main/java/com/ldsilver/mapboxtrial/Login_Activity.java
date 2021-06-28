package com.ldsilver.mapboxtrial;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// import com.google.android.gms.tasks.OnCompleteListener;
// import com.google.android.gms.tasks.Task;
// import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login_Activity extends AppCompatActivity {
    EditText et_email, et_password;
    CheckBox cb_rememberMe;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView registerHere = findViewById(R.id.RegisterHere);
        registerHere.setOnClickListener(v -> {
            Intent myIntent = new Intent(Login_Activity.this, Register_Activity.class);
            startActivity(myIntent);
        });

        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String checkbox = preferences.getString("remember", "");

        if (checkbox.equals("true")) {
            Intent intent = new Intent(Login_Activity.this, MainActivity.class);
            startActivity(intent);
        } else if (checkbox.equals("false")) {
            Toast.makeText(this, "Please Login!", Toast.LENGTH_SHORT).show();
        }

        // login button
        Button login = findViewById(R.id.btn_Login);
        login.setOnClickListener(v -> LoginUser());

        // email and password credentials
        et_email = findViewById(R.id.editText_EmailAddress);
        et_password = findViewById(R.id.editText_Password);

        // remember me feature
        cb_rememberMe = findViewById(R.id.checkBox_RememberMe);
        cb_rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    Toast.makeText(Login_Activity.this, "Checked!", Toast.LENGTH_SHORT).show();
                } else if (!buttonView.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    Toast.makeText(Login_Activity.this, "Unchecked!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        // forgot password
        TextView forgotPassword = findViewById(R.id.TextView_ForgotPassword);
        forgotPassword.setOnClickListener(v -> {
            Intent myIntent = new Intent(Login_Activity.this, ForgotPassword_Activity.class);
            startActivity(myIntent);
        });
    }

    /*

    // switch statement is not the best option here
    // because of the error
    // Resource IDs will be non-final in Android Gradle Plugin version 7.0,
    // avoid using them in switch case statements

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.RegisterHere:
                startActivity(new Intent(this, Register_Activity.class));
                break;
            case R.id.btn_Login:
                LoginUser();
                break;
            case R.id.TextView_ForgotPassword:
                startActivity(new Intent(this, ForgotPassword_Activity.class));
                break;
        }
    }

    */

    // login method
    private void LoginUser() {
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();

        // if Fields are empty
        if (email.isEmpty()) {
            et_email.setError("Email Address is required!");
            et_email.requestFocus();
            return;
        }
        // right email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError("Please enter a valid email address!");
            et_email.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            et_password.setError("Password is required!");
            et_password.requestFocus();
            return;
        }
        if (password.length() < 6) {
            et_password.setError("Password must not be less then 6 characters!");
            et_password.requestFocus();
            return;
        }

        // progress bar for loading time
        progressBar.setVisibility(View.VISIBLE);

        // signing in with email and password
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // go to maps activity (home page)
                startActivity(new Intent(Login_Activity.this, MainActivity.class));
            } else {
                // reenter the correct credentials
                Toast.makeText(Login_Activity.this,
                        "Email address or password is incorrect! Please enter the correct credentials."
                        , Toast.LENGTH_SHORT).show();
            }
        });

        // end of loading
        progressBar.setVisibility(View.GONE);
    }
}

