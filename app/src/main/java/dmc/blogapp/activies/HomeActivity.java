package dmc.blogapp.activies;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import dmc.blogapp.R;
import dmc.blogapp.fragments.HomeFragment;
import dmc.blogapp.fragments.PostsFragment;
import dmc.blogapp.fragments.ProfileFragment;
import dmc.blogapp.fragments.SettingFragment;
import dmc.blogapp.model.Post;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private CircleImageView imgProfile;
    private TextView txtEmail, txtDisplayName;
    private FloatingActionButton fab;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Dialog popAddPost;
    private EditText popupTitle, popupDescription;
    private PorterShapeImageView popupImage;
    private ImageView popupAdd;
    private ProgressBar popupProgress;
    private Uri pickedImgUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapping();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        loadUserInfo();

        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            getSupportActionBar().setTitle("Home");
            navigationView.setCheckedItem(R.id.nav_home);
        }

        initPopup();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popAddPost.show();
            }
        });

        popupImageClickListener();

        popupAddClickListener();
    }

    private void popupImageClickListener() {
        popupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestForPermission();
            }
        });
    }

    private void popupAddClickListener() {
        popupAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = popupTitle.getText().toString();
                String description = popupDescription.getText().toString();

                if (title.isEmpty() || description.isEmpty() || pickedImgUri == null) {
                    showMessage("Please verify all input fields and choose post image");
                    popupAdd.setVisibility(View.VISIBLE);
                    popupProgress.setVisibility(View.INVISIBLE);
                } else {
                    popupAdd.setVisibility(View.INVISIBLE);
                    popupProgress.setVisibility(View.VISIBLE);

                    StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("blog_images");
                    final StorageReference imageFilePath = mStorage.child(Objects.requireNonNull(pickedImgUri.getLastPathSegment()));
                    imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageDownloadLink = uri.toString();
                                    Post post = new Post(popupTitle.getText().toString(),
                                            popupDescription.getText().toString(),
                                            imageDownloadLink,
                                            currentUser.getUid());
                                    addPostIntoDatabase(post);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showMessage(e.getMessage());
                            popupAdd.setVisibility(View.VISIBLE);
                            popupProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }

    private void addPostIntoDatabase(Post post) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("posts").push();
        String key = mDatabase.getKey();
        post.setPostKey(key);

        mDatabase.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showMessage("Post added successfully");
                popupAdd.setVisibility(View.VISIBLE);
                popupProgress.setVisibility(View.INVISIBLE);
                popAddPost.dismiss();
            }
        });
    }

    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessage("Please accept for required permission");
            } else {
                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                pickedImgUri = result.getUri();
                popupImage.setImageURI(pickedImgUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void initPopup() {
        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        Objects.requireNonNull(popAddPost.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.CENTER_HORIZONTAL;

        popupTitle = popAddPost.findViewById(R.id.popup_title);
        popupDescription = popAddPost.findViewById(R.id.popup_description);
        popupImage = popAddPost.findViewById(R.id.popup_image);
        popupAdd = popAddPost.findViewById(R.id.popup_add);
        popupProgress = popAddPost.findViewById(R.id.popup_progress);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void loadUserInfo() {
        Glide.with(HomeActivity.this).load(currentUser.getPhotoUrl()).into(imgProfile);
        txtDisplayName.setText(currentUser.getDisplayName());
        txtEmail.setText(currentUser.getEmail());
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selected = null;
        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportActionBar().setTitle("Home");
                selected = new HomeFragment();
                break;
            case R.id.nav_posts:
                getSupportActionBar().setTitle("Post");
                selected = new PostsFragment();
                break;
            case R.id.nav_profile:
                getSupportActionBar().setTitle("Profile");
                selected = new ProfileFragment();
                break;
            case R.id.nav_setting:
                getSupportActionBar().setTitle("Setting");
                selected = new SettingFragment();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                Intent loginActivity = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(loginActivity);
                return true;
        }

        assert selected != null;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selected).commit();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void mapping() {
        drawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        imgProfile = headerView.findViewById(R.id.img_profile);
        txtEmail = headerView.findViewById(R.id.txt_email);
        txtDisplayName = headerView.findViewById(R.id.txt_display_name);

        fab = findViewById(R.id.fab);
    }
}
