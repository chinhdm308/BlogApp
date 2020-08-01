package dmc.blogapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dmc.blogapp.R;
import dmc.blogapp.adapter.PostAdapter;
import dmc.blogapp.model.Post;

public class HomeFragment extends Fragment {

    private List<Post> postList;
    private PostAdapter postAdapter;
    private RecyclerView recyclerViewPosts;

    @Override
    public void onStart() {
        super.onStart();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList = new ArrayList<>();
                for (DataSnapshot i : dataSnapshot.getChildren()) {
                    Post post = i.getValue(Post.class);
                    postList.add(post);
                }

                postAdapter = new PostAdapter(getContext(), getActivity(), postList);
                recyclerViewPosts.setAdapter(postAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewPosts = view.findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setHasFixedSize(true);
        recyclerViewPosts.setBackgroundColor(Color.TRANSPARENT);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }
}
