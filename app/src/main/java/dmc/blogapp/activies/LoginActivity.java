package dmc.blogapp.activies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;
import dmc.blogapp.R;

public class LoginActivity extends AppCompatActivity {

    CircleImageView imgProfile;
    TextInputEditText txtUsername, txtPassword;
    Button btnLogin;
    TextView txtCreateAcount;
    ProgressBar progressBarLoadingLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mapping();

        final String username = txtUsername.getText().toString();
        final String password = txtPassword.getText().toString();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!username.isEmpty() && !password.isEmpty()) {
                    btnLogin.setVisibility(View.INVISIBLE);
                    progressBarLoadingLogin.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(LoginActivity.this, "abc", Toast.LENGTH_SHORT).show();
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

    void mapping() {
        txtUsername = findViewById(R.id.txt_username);
        txtPassword = findViewById(R.id.txt_password);
        txtCreateAcount = findViewById(R.id.txt_create_new_account);
        btnLogin = findViewById(R.id.btn_login);
        imgProfile = findViewById(R.id.img_profile);
        progressBarLoadingLogin = findViewById(R.id.progressBar_loading_login);
    }
}
