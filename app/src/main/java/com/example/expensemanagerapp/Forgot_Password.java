package com.example.expensemanagerapp;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.expensemanagerapp.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class Forgot_Password extends AppCompatActivity {

    //View Binding
    private ActivityForgotPasswordBinding binding;

    //Firebase
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressBar mBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //intialising firebase auth
        firebaseAuth=FirebaseAuth.getInstance();

        mBar=findViewById(R.id.progressBar3);
        mBar.setVisibility(View.GONE);
        
        
        //handle click, begin recovery
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String email="";
    private void validateData() {
        //get data i.e, email
        email=binding.emailET.getText().toString().trim();

        //validate data
        if(email.isEmpty())
            Toast.makeText(this, "Enter the email..", Toast.LENGTH_LONG).show();
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format..", Toast.LENGTH_LONG).show();
        }
        else {
            mBar=findViewById(R.id.progressBar3);
            mBar.setVisibility(View.GONE);
            recoverPassword();
        }
    }

    private void recoverPassword() {

        mBar.setVisibility(View.VISIBLE);

        //begin sending recovery
        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //sent
                mBar.setVisibility(View.GONE);
                Toast.makeText(Forgot_Password.this, "Instructions to reset password sent to "+email, Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to send
                mBar.setVisibility(View.GONE);
                Toast.makeText(Forgot_Password.this, "Failed to send email due "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}