package dmc.blogapp.activies;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

import de.hdodenhof.circleimageview.CircleImageView;
import dmc.blogapp.R;
import dmc.blogapp.fragments.HomeFragment;
import dmc.blogapp.fragments.PostsFragment;
import dmc.blogapp.fragments.ProfileFragment;
import dmc.blogapp.fragments.SettingFragment;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private  NavigationView navigationView;
    private CircleImageView imgProfile;
    private TextView txtEmail, txtDisplayName;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Dialog popAddPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapping();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        initPopup();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            getSupportActionBar().setTitle("Home");
            navigationView.setCheckedItem(R.id.nav_home);
        }

        loadInfoUser();

        SpaceNavigationView spaceNavigationView = (SpaceNavigationView) findViewById(R.id.space);
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("Home", R.drawable.ic_home_black_24dp));
        spaceNavigationView.addSpaceItem(new SpaceItem("Notifications", R.drawable.ic_notifications_black_24dp));
        spaceNavigationView.showIconOnly();
        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                popAddPost.show();
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                showMessage( itemIndex + " " + itemName);
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
                showMessage( itemIndex + " " + itemName);
            }
        });
    }

    private void initPopup() {
        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.CENTER_HORIZONTAL;

    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void loadInfoUser() {
        Glide.with(this).load(currentUser.getPhotoUrl()).error(R.mipmap.ic_launcher_round).into(imgProfile);
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
    }
}
