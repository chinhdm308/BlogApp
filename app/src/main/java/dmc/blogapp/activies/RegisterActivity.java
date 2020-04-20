package dmc.blogapp.activies;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;
import dmc.blogapp.R;

public class RegisterActivity extends AppCompatActivity {

    private static final int REQUESCODE = 1;
    private CircleImageView imgProfile;
    private TextInputEditText txtDisplayName, txtEmail, txtPassword, txtConfirmPassword;
    private ProgressBar loadingRegisterProgress;
    private Button btnRegister;
    private TextView txtReadyToLogin;
    private static int PreqCode = 1;

    private Uri pickedImgUri = null;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Register");

        mapping();

        mAuth = FirebaseAuth.getInstance();

        final String name = txtDisplayName.getText().toString();
        final String email = txtEmail.getText().toString();
        final String password = txtPassword.getText().toString();
        final String confirmPass = txtConfirmPassword.getText().toString();

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 24) {
                    checkAndRequestForPermission();
                } else {
                    openGallery();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
                    showMessage("Please verify all fields");
                    loadingRegisterProgress.setVisibility(View.INVISIBLE);
                    btnRegister.setVisibility(View.VISIBLE);
                } else {
                    if (confirmPass.equals(password)) {
                        if (pickedImgUri != null) {
                            loadingRegisterProgress.setVisibility(View.VISIBLE);
                            btnRegister.setVisibility(View.INVISIBLE);

                            createUserAccount(email, name, password);

                        } else {
                            showMessage("Please add image profile");
                            loadingRegisterProgress.setVisibility(View.INVISIBLE);
                            btnRegister.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showMessage("Please verify correct password");
                        loadingRegisterProgress.setVisibility(View.INVISIBLE);
                        btnRegister.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        txtReadyToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
            }
        });
    }

    private void createUserAccount(String email, final String name, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            showMessage("createUserWithEmail:success");
                            updateUserInfo(name, pickedImgUri, mAuth.getCurrentUser());

                        } else {
                            // If sign in fails, display a message to the user.
                            showMessage("createUserWithEmail:failure" + task.getException());
                            showMessage("Authentication failed.");
                            loadingRegisterProgress.setVisibility(View.INVISIBLE);
                            btnRegister.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void updateUserInfo(final String name, final Uri pickedImgUri, final FirebaseUser currentUser) {
        // need to upload user photo to firebase storage and get url
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // image uploaded successfully
                // now we can get our image url
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // uri contain user image url
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(pickedImgUri)
                                .build();
                        currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // user info updated successfully
                                    showMessage("Register complete");
                                    updateUI();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateUI() {
        Intent loginActivity = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginActivity);
        finish();
    }

    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessage("Please accept for required permission");
            } else {
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, PreqCode);
            }
        } else {
            openGallery();
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void mapping() {
        imgProfile = findViewById(R.id.img_profile);
        txtDisplayName = findViewById(R.id.txt_display_name);
        txtEmail = findViewById(R.id.txt_email);
        txtPassword = findViewById(R.id.txt_password);
        txtConfirmPassword = findViewById(R.id.txt_confirm_password);
        loadingRegisterProgress = findViewById(R.id.progressBar_loading_register);
        btnRegister = findViewById(R.id.btn_register);
        txtReadyToLogin = findViewById(R.id.txt_ready_to_login);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {
            pickedImgUri = data.getData();
            imgProfile.setImageURI(pickedImgUri);
        }
    }
}