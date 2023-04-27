package cvngoc.hcmute.music;

import static android.content.ContentValues.TAG;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import cvngoc.hcmute.music.databinding.ActivityUploadBinding;


public class UploadActivity extends AppCompatActivity {

    ActivityUploadBinding binding;
    //    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private Uri mFileUri;

    private Uri DownloadImageUri;
    private Uri DownloadFileUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    ActivityResultLauncher<Intent> openImg = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Log.e("TAG", "onCreate: " + result.getData().getData());
            mImageUri = result.getData().getData();
//            Picasso.get().load(result.getData().getData()).into(binding.btnUploadImg);
            binding.btnUploadImg.setText((result.getData().getData()).toString());
        }

    });

    ActivityResultLauncher<Intent> openFile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Log.e("TAG", "onCreate: " + result.getData().getData());
            mFileUri = result.getData().getData();
//            Picasso.get().load(result.getData().getData()).into(binding.btnUploadImg);
            binding.btnUploadFile.setText((result.getData().getData()).toString());
        }

    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        signInAnonymously();

        mStorageRef = FirebaseStorage.getInstance().getReference("songs");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("songs");

        binding.btnUploadImg.setOnClickListener(v -> openImageChooser());
        binding.btnUploadFile.setOnClickListener(v -> openFileChooser());
        binding.btnUpload.setOnClickListener(v -> uploadFile());
        binding.imgBackHome.setOnClickListener(v -> openHomeActivity());
    }


    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(UploadActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        openImg.launch(intent);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        openFile.launch(intent);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri != null && mFileUri != null) {
            StorageReference imgReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mFileUri));

            imgReference.putFile(mImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> binding.progressBar.setProgress(0), 500);
                        Toast.makeText(UploadActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        imgReference.getDownloadUrl().addOnSuccessListener(uri -> {
//                            Song song = new Song(binding.edtNameSongUp.getText().toString().trim(),binding.edtSingleNameUp.getText().toString().trim(),
//                                    uri.toString());
                            DownloadImageUri = uri;
//                            String songID = mDatabaseRef.push().getKey();
//                            mDatabaseRef.child(songID).setValue(song);
//                            Log.e("TAG", "uploadFile: " + uri);
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(UploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());


            //File Music:

            fileReference.putFile(mFileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> binding.progressBar.setProgress(0), 500);
                        Toast.makeText(UploadActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            Song song = new Song(binding.edtNameSongUp.getText().toString().trim(),binding.edtSingleNameUp.getText().toString().trim(),
                                    DownloadImageUri.toString(), uri.toString());
                            String songID = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(songID).setValue(song);
                            Log.e("TAG", "uploadFile: " + uri);
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(UploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        binding.progressBar.setProgress((int) progress);
                    });

        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }

    }
    private void openHomeActivity() {
        Intent intent = new Intent(this, home_activity.class);
        startActivity(intent);
    }

}