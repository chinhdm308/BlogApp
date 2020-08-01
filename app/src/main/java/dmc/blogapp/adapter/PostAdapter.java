package dmc.blogapp.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import dmc.blogapp.R;
import dmc.blogapp.fragments.CommentFragment;
import dmc.blogapp.model.Post;
import dmc.blogapp.model.User;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context mContext;
    private FragmentActivity fragmentActivity;
    private List<Post> postList;

    public PostAdapter(Context mContext, FragmentActivity fragmentActivity, List<Post> postList) {
        this.mContext = mContext;
        this.fragmentActivity = fragmentActivity;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, int position) {
        Post post = postList.get(position);

        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("users");
        usersReference.child(post.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getProfileImg()).into(holder.authorAvatar);
                holder.authorName.setText(user.getDisplayName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Glide.with(mContext).load(post.getPicture()).into(holder.postPhoto);
        holder.postTitle.setText(post.getTitle());
        holder.postDate.setText(timeStampToString((Long) post.getTimeStamp()));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView authorAvatar;
        ImageView postPhoto;
        TextView postTitle, authorName, postDate;
        ImageButton btnComment;
        CheckBox btnLike;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorAvatar = itemView.findViewById(R.id.item_post_author_avatar);
            postTitle = itemView.findViewById(R.id.item_post_title);
            authorName = itemView.findViewById(R.id.item_post_author_name);
            postDate = itemView.findViewById(R.id.item_post_date);
            postPhoto = itemView.findViewById(R.id.item_post_photo);
            btnLike = itemView.findViewById(R.id.item_post_like_btn);
            btnComment = itemView.findViewById(R.id.item_post_comment_btn);

            btnLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        Toast.makeText(mContext, "like", Toast.LENGTH_SHORT).show();
                }
            });

            btnComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommentFragment commentFragment = new CommentFragment();
                    commentFragment.show(fragmentActivity.getSupportFragmentManager(), "tagname");
                }
            });
        }
    }

    private String timeStampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        return DateFormat.format("dd/MM/yyyy hh:mm", calendar).toString();
    }
}
