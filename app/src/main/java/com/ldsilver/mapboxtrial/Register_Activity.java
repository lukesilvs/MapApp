package com.ldsilver.mapboxtrial;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class Register_Activity extends AppCompatActivity
{
    private EditText et_firstName, et_lastName, et_userEmail, et_userPassword;
    // private SwitchCompat s_measurement;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        TextView loginHere = findViewById(R.id.LoginHere);
        loginHere.setOnClickListener(v -> {
            Intent myIntent = new Intent(Register_Activity.this, Login_Activity.class);
            startActivity(myIntent);
        });

        TextView register = findViewById(R.id.btn_Register);
        register.setOnClickListener(v -> RegisterUser());

        // user details
        et_firstName = findViewById(R.id.editText_RegFirstName);
        et_lastName = findViewById(R.id.editText_RegLastName);
        et_userEmail = findViewById(R.id.editText_RegEmailAddress);
        et_userPassword = findViewById(R.id.editText_RegPassword);
       // s_measurement = (SwitchCompat) findViewById(R.id.switch_measurementSystem);

       /* if (s_measurement != null) {
            s_measurement.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) this);
        }*/

        progressBar = findViewById(R.id.progressBar_Register);

    }

    private void RegisterUser() {

        final String firstName = et_firstName.getText().toString().trim();
        final String lastName = et_lastName.getText().toString().trim();
        final String email = et_userEmail.getText().toString().trim();
        final String password = et_userPassword.getText().toString().trim();


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
            et_userEmail.setError("Email Address is required!");
            et_userEmail.requestFocus();
            return;
        }

        // wrong email format
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_userEmail.setError("Please enter a valid email address.");
            et_userEmail.requestFocus();
            return;
        }
        if (password.isEmpty()){
            et_userPassword.setError("Password is required!");
            et_userPassword.requestFocus();
            return;
        }
        // minimum password length
        if(password.length() < 6){
            et_userPassword.setError("Password must not be less then 6 characters!");
            et_userPassword.requestFocus();
            return;
        }
        // imperial or metric


        // progress bar visibility to true
        progressBar.setVisibility(View.VISIBLE);

        // create user in firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // successful task
                    if (task.isSuccessful()) {
                        UserAccount user = new UserAccount(firstName, lastName, email);

                        // Firebase reference
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(task1 -> {
                            // once user is successfully registered
                            if(task1.isSuccessful()){
                                Toast.makeText(Register_Activity.this,
                                        "User has been registered successfully! " +
                                                "\nYou can login now!"
                                        ,Toast.LENGTH_LONG).show();

                                // show progress bar
                                progressBar.setVisibility(View.GONE);

                                // redirect to login activity
                                startActivity(new Intent(Register_Activity.this, Login_Activity.class));
                            }else{
                                Toast.makeText(Register_Activity.this, "Something went wrong. Please try again!",
                                        Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }else{
                        Toast.makeText(Register_Activity.this, "Unsuccessful registration.",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}