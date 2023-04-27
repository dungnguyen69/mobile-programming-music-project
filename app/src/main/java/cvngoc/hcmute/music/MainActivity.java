package cvngoc.hcmute.music;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cvngoc.hcmute.music.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    DatabaseReference mDatabaseRef;
    private boolean isPlaying;
    private Song songRoot;
    int duration = 10000;
    int listsize;
    int newID = -1;

    private final ServiceConnection mServiceConnection  = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.LocalBinder binder = (MyService.LocalBinder) iBinder;
            mService = binder.getService();
            mIsBound = true;
            mService.setSeekBar(binding.seekBar);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mIsBound = false;
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            songRoot = (Song) bundle.get("object_song");
            isPlaying = bundle.getBoolean("status_player");
            int actionMusic = bundle.getInt("action_music");

            handleLayoutBottomMusic(actionMusic);
        }
    };

    private MyService mService;
    String selectedKey;
    private boolean mIsBound = false;



    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Seekbar



        Intent intent1 = new Intent(getApplicationContext(), MyService.class);
        bindService(intent1, mServiceConnection, Context.BIND_AUTO_CREATE);

//        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (mIsBound) {
//                    mService.onSeekBarProgressChanged();
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                if (mIsBound) {
//                    Log.e("Test", "keo");
//                    mService.onSeekBarStartTrackingTouch();
//                }
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                if (mIsBound) {
//                    Log.e("Test", "tha");
//                    mService.onSeekBarStopTrackingTouch();
//                }
//            }
//        });

        //Handle load song
        Intent intent = getIntent();
        selectedKey = intent.getStringExtra("selectedKey");
        int listSongSize = intent.getIntExtra("listsize",0);
        listsize = listSongSize;

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("songs").child(selectedKey);
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Lấy đối tượng dữ liệu từ dataSnapshot
                Song song = dataSnapshot.getValue(Song.class);
                songRoot = song;
                // Sử dụng đối tượng dữ liệu ở đây
                binding.tvTitleMain.setText(song.getTitle());
                binding.tvSingleMain.setText(song.getSingle());
                //Picasso.get().load(song.getImage()).fit().centerCrop().into(binding.imgSongAvt);

                // Tham chiếu đến ảnh trong Firebase Storage
                StorageReference imageRef = storage.getReferenceFromUrl(song.getImage());
                // Lấy URL của ảnh
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Load ảnh bằng Glide
                        ImageView imgGlide = binding.imgSongAvt;
                        Glide.with(getApplicationContext())
                                .load(song.getImage())
                                .apply(new RequestOptions().transform(new CenterCrop()).transform(new RoundedCorners(24)))
                                .into(imgGlide);
                    }
                });


                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, new IntentFilter("send_data_to_activity"));

                binding.imgPlayOrPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clickStartSong(song);
                        binding.imgPlayOrPause.setImageResource(R.drawable.outline_pause_circle_white_48);

                    }
                });

                binding.imgNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference().child("songs");
                        Query nextItemQuery = itemsRef.orderByKey().startAt(selectedKey).limitToFirst(2);
                        nextItemQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Lấy danh sách các phần tử kế tiếp
                                List<DataSnapshot> nextItems = new ArrayList<>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (!child.getKey().equals(selectedKey)) {
                                        nextItems.add(child);
                                    }
                                }

                                // Lấy phần tử kế tiếp
                                DataSnapshot nextItem = null;
                                if (nextItems.size() > 0) {
                                    nextItem = nextItems.get(0);
                                }

                                // Xử lý phần tử kế tiếp ở đây
                                if (nextItem != null) {

                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    intent.putExtra("selectedKey", nextItem.getKey());
                                    startActivity(intent);
                                } else {
                                    // Không có phần tử kế tiếp, truy vấn lại để lấy phần tử đầu tiên
                                    Query firstItemQuery = itemsRef.orderByKey().limitToFirst(1);
                                    firstItemQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            // Lấy phần tử đầu tiên
                                            DataSnapshot firstItem = dataSnapshot.getChildren().iterator().next();

                                            // Xử lý phần tử đầu tiên ở đây

                                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                            intent.putExtra("selectedKey", firstItem.getKey());
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e("NextItem", "Lỗi: " + databaseError.getMessage());
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("NextItem", "Lỗi: " + databaseError.getMessage());
                            }
                        });

                    }
                });

                binding.imgPreviour.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference().child("songs");
                        Query prevItemQuery = itemsRef.orderByKey().endAt(selectedKey).limitToLast(2);
                        prevItemQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Lấy danh sách các phần tử phía trước
                                List<DataSnapshot> prevItems = new ArrayList<>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (!child.getKey().equals(selectedKey)) {
                                        prevItems.add(child);
                                    }
                                }

                                // Lấy phần tử phía trước
                                DataSnapshot prevItem = null;
                                if (prevItems.size() > 1) {
                                    prevItem = prevItems.get(1);
                                } else if (prevItems.size() == 1) {
                                    prevItem = prevItems.get(0);
                                }

                                // Xử lý phần tử phía trước ở đây
                                if (prevItem != null) {

                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    intent.putExtra("selectedKey", prevItem.getKey());
                                    startActivity(intent);
                                } else {
                                    // Lấy phần tử cuối cùng
                                    itemsRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                DataSnapshot lastItem = dataSnapshot.getChildren().iterator().next();
                                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                intent.putExtra("selectedKey", lastItem.getKey());
                                                startActivity(intent);
                                            } else {
                                                Log.d("PrevItem", "Không có phần tử nào trong danh sách");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e("PrevItem", "Lỗi: " + databaseError.getMessage());
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("PrevItem", "Lỗi: " + databaseError.getMessage());
                            }
                        });

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });






        ConstraintLayout constraintLayout = findViewById(R.id.main_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();


        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newID = -1;
                openHomeActivity();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đăng ký Broadcast Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("MEDIA_PLAYER_SEEK_TO");
        filter.addAction("MEDIA_PLAYER_DURATION");
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hủy đăng ký Broadcast Receiver
        unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Xử lý các thông báo từ Service
            if (intent.getAction().equals("MEDIA_PLAYER_SEEK_TO")) {
                // Di chuyển SeekBar đến vị trí mới
                int seekToPosition = intent.getIntExtra("seek_to_position", 0);
                binding.seekBar.setProgress(seekToPosition);
                TextView tvtimeform = findViewById(R.id.time_from);
                tvtimeform.setText(formattedTime(seekToPosition));
                if(seekToPosition == -1)
                {
                    if (songRoot.getId() + 1 <= listsize) {
                        newID = songRoot.getId() + 1;
                    } else {
                        newID = 1;
                    }
                    Intent intent3 = new Intent(MainActivity.this, MainActivity.class);
                    intent3.putExtra("songID", newID);
                    intent3.putExtra("listSongsize", listsize);
                    startActivity(intent3);
                }
            } else if (intent.getAction().equals("MEDIA_PLAYER_DURATION")) {
                duration = intent.getIntExtra("duration", 0);
                // Cập nhật chiều dài của SeekBar
                binding.seekBar.setMax(duration);

                TextView tvtimeTo = findViewById(R.id.time_to);
                tvtimeTo.setText(formattedTime(duration));
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsBound) {
            unbindService(mServiceConnection);
            mIsBound = false;
        }
    }

    private String formattedTime(int duration) {
        String durationString = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        return durationString;
    }

    private void openHomeActivity() {
        Intent intent = new Intent(this, home_activity.class);
        startActivity(intent);
    }

    private void clickStartSong(Song song) {
        Intent intent = new Intent(this, MyService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("object_song", song);
        intent.putExtras(bundle);
        startService(intent);
    }


    private void handleLayoutBottomMusic(int action) {
        switch (action) {
            case MyService.ACTION_PAUSE:
                setBtnPlayOrPause();
                break;
            case MyService.ACTION_RESUME:
                binding.imgPlayOrPause.setImageResource(R.drawable.outline_pause_circle_white_48);
                break;
            case MyService.ACTION_PREVIOUS:
                DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference().child("songs");
                Query prevItemQuery = itemsRef.orderByKey().endAt(selectedKey).limitToLast(2);
                prevItemQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Lấy danh sách các phần tử phía trước
                        List<DataSnapshot> prevItems = new ArrayList<>();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (!child.getKey().equals(selectedKey)) {
                                prevItems.add(child);
                            }
                        }

                        // Lấy phần tử phía trước
                        DataSnapshot prevItem = null;
                        if (prevItems.size() > 1) {
                            prevItem = prevItems.get(1);
                        } else if (prevItems.size() == 1) {
                            prevItem = prevItems.get(0);
                        }

                        // Xử lý phần tử phía trước ở đây
                        if (prevItem != null) {

                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.putExtra("selectedKey", prevItem.getKey());
                            startActivity(intent);
                        } else {
                            // Lấy phần tử cuối cùng
                            itemsRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        DataSnapshot lastItem = dataSnapshot.getChildren().iterator().next();
                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                        intent.putExtra("selectedKey", lastItem.getKey());
                                        startActivity(intent);
                                    } else {
                                        Log.d("PrevItem", "Không có phần tử nào trong danh sách");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("PrevItem", "Lỗi: " + databaseError.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("PrevItem", "Lỗi: " + databaseError.getMessage());
                    }
                });
                break;
            case MyService.ACTION_NEXT:
                DatabaseReference itemsRefN = FirebaseDatabase.getInstance().getReference().child("songs");
                Query nextItemQuery = itemsRefN.orderByKey().startAt(selectedKey).limitToFirst(2);
                nextItemQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Lấy danh sách các phần tử kế tiếp
                        List<DataSnapshot> nextItems = new ArrayList<>();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (!child.getKey().equals(selectedKey)) {
                                nextItems.add(child);
                            }
                        }

                        // Lấy phần tử kế tiếp
                        DataSnapshot nextItem = null;
                        if (nextItems.size() > 0) {
                            nextItem = nextItems.get(0);
                        }

                        // Xử lý phần tử kế tiếp ở đây
                        if (nextItem != null) {

                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.putExtra("selectedKey", nextItem.getKey());
                            startActivity(intent);
                        } else {
                            // Không có phần tử kế tiếp, truy vấn lại để lấy phần tử đầu tiên
                            Query firstItemQuery = itemsRefN.orderByKey().limitToFirst(1);
                            firstItemQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Lấy phần tử đầu tiên
                                    DataSnapshot firstItem = dataSnapshot.getChildren().iterator().next();

                                    // Xử lý phần tử đầu tiên ở đây

                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                    intent.putExtra("selectedKey", firstItem.getKey());
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("NextItem", "Lỗi: " + databaseError.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("NextItem", "Lỗi: " + databaseError.getMessage());
                    }
                });
                break;
            case MyService.ACTION_START:
                //layoutBottom.setVisibility(View.VISIBLE);
                showInforSong();
                setBtnPlayOrPause();
                break;
        }
    }

    private void showInforSong() {
        if (songRoot == null) {
            return;
        }
        binding.imgPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    sendActionToService(MyService.ACTION_PAUSE);
                } else {
                    sendActionToService(MyService.ACTION_RESUME);
                }
            }
        });
    }

    private void setBtnPlayOrPause() {
        if (isPlaying) {
            binding.imgPlayOrPause.setImageResource(R.drawable.outline_pause_circle_white_48);
        } else {
            binding.imgPlayOrPause.setImageResource(R.drawable.outline_play_circle_outline_white_48);
        }
    }

    private void sendActionToService(int action) {
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("action_music_receiver", action);
        startService(intent);
    }


}