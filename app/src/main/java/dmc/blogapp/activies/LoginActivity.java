package dmc.blogapp.activies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;
import dmc.blogapp.R;

public class LoginActivity extends AppCompatActivity {

    CircleImageView imgProfile;
    TextInputEditText txtEmail, txtPassword;
    Button btnLogin;
    TextView txtCreateAcount;
    ProgressBar progressBarLoadingLogin;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Login");

        mapping();

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = txtEmail.getText().toString();
                final String password = txtPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    btnLogin.setVisibility(View.VISIBLE);
                    progressBarLoadingLogin.setVisibility(View.INVISIBLE);
                    showMessage("Please verify all feilds");
                } else {
                    signin(email, password);
                }
            }
        });

        txtCreateAcount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activityRegister = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(activityRegister);
            }
        });
    }

    private void signin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    updateUI();
                } else {
                    showMessage("Authentication Failed");
                }
            }
        });
    }


    private void updateUI() {
        Intent homeActivity = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(homeActivity);
        finish();
    }

    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI();
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    void mapping() {
        txtEmail = findViewById(R.id.txt_email);
        txtPassword = findViewById(R.id.txt_password);
        txtCreateAcount = findViewById(R.id.txt_create_new_account);
        btnLogin = findViewById(R.id.btn_login);
        imgProfile = findViewById(R.id.img_profile);
        progressBarLoadingLogin = findViewById(R.id.progressBar_loading_login);
    }
}
