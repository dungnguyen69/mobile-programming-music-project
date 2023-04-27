package cvngoc.hcmute.music;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;


public class SignUp extends AppCompatActivity {
    private FrameLayout frameLayout;
    private EditText txtUsername;
    private EditText txtEmail;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private Button btnRegister;
    private String email, password;
    private FirebaseAuth mAuth;
    private final boolean NOT_EMAIL = false;

    private ProgressBar progressBar;
    private TextView txtSignIn;
    private FirebaseUser currentUser;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

//        if (currentUser != null) {
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        txtSignIn = findViewById(R.id.sign_in);
        mAuth = FirebaseAuth.getInstance();
        initListener();
    }

    private void initListener() {
        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignIn.class);
                startActivity(intent);
                finish();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                email = txtEmail.getText().toString();
                password = txtPassword.getText().toString();
                if (checkDataEntered()) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        currentUser = mAuth.getCurrentUser();
                                        currentUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(SignUp.this, "Email verification was sent",
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(), SignIn.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "Email not sent " + e.getMessage());
                                            }
                                        });
                                    } else
                                        Toast.makeText(SignUp.this, "This email was already registered",
                                                Toast.LENGTH_SHORT).show();
                                }
                            });
                } else progressBar.setVisibility(View.GONE);
            }
        });
    }

    private boolean checkDataEntered() {

        if (isEmail(txtEmail) == NOT_EMAIL) {
            Toast t = Toast.makeText(this, "Email is required to register!", Toast.LENGTH_SHORT);
            t.show();
            return false;
        }
        if (isEmpty(txtPassword)) {
            Toast t = Toast.makeText(this, "Password is mandatory!", Toast.LENGTH_SHORT);
            t.show();
            return false;
        }
        if (isEmpty(txtConfirmPassword)) {
            Toast t = Toast.makeText(this, "Confirm password need to be entered to register!", Toast.LENGTH_SHORT);
            t.show();
            return false;
        }
        if (!txtPassword.getText().toString().equals(txtConfirmPassword.getText().toString())) {
            Toast t = Toast.makeText(this, "Confirm password need to be identical to password entered", Toast.LENGTH_SHORT);
            t.show();
            return false;
        }
        return true;
    }

    private boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

    private boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private void setFragment(Fragment signIn) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(frameLayout.getId(), signIn);
        fragmentTransaction.commit();
    }
}