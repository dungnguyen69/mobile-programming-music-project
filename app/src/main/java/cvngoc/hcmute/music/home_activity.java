package cvngoc.hcmute.music;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import cvngoc.hcmute.music.databinding.ActivityHomeBinding;


public class home_activity extends AppCompatActivity implements SongAdapter.OnItemClickListener {

    ActivityHomeBinding binding;
    private SongAdapter mAdapter;
    private List<Song> listSongs;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    ImageFilterView btnaddSong;

    Button btnLogout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        RecyclerView mRecyclerView = binding.List0fSong;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listSongs = new ArrayList<>();
        mAdapter = new SongAdapter(home_activity.this, listSongs);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(home_activity.this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("songs");
        btnLogout = findViewById(R.id.btnLogout);
        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listSongs.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Song song = postSnapshot.getValue(Song.class);
                    assert song != null;
                    song.setKey(postSnapshot.getKey());
                    listSongs.add(song);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(home_activity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnaddSong = findViewById(R.id.imgAddSong);
        btnaddSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home_activity.this, UploadActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), SignIn.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Song selectedItem = listSongs.get(position);
        int listsize = listSongs.size();
        final String selectedKey = selectedItem.getKey();
        Intent intent = new Intent(home_activity.this, MainActivity.class);
        intent.putExtra("selectedKey", selectedKey);
        intent.putExtra("listsize", listsize);
        home_activity.this.startActivity(intent);
    }

    @Override
    public void onWhatEverClick(int position) {
        Toast.makeText(this, "Whatever click at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        Song selectedItem = listSongs.get(position);
        final String selectedKey = selectedItem.getKey();

        StorageReference imageRef = storage.getReferenceFromUrl(selectedItem.getImage());
        imageRef.delete().addOnSuccessListener(aVoid -> {
            mDatabaseRef.child(selectedKey).removeValue();
        });

        StorageReference fileRef = storage.getReferenceFromUrl(selectedItem.getResource());
        fileRef.delete().addOnSuccessListener(aVoid -> {
            mDatabaseRef.child(selectedKey).removeValue();
            Toast.makeText(home_activity.this, "Item deleted", Toast.LENGTH_SHORT).show();
        });
    }
}
