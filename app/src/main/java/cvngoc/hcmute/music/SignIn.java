package cvngoc.hcmute.music;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignIn extends AppCompatActivity {
    private TextView txtRegister;
    private EditText txtEmail;
    private EditText txtPassword;
    private TextView txtForgetPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

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
        setContentView(R.layout.activity_login);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtRegister = findViewById(R.id.txtRegister);
        txtForgetPassword = findViewById(R.id.txtForgetPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        initListener();
    }

    private void initListener() {
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (checkDataEntered()) {
                    String email, password;
                    email = txtEmail.getText().toString();
                    password = txtPassword.getText().toString();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        user = FirebaseAuth.getInstance().getCurrentUser();
                                        if (user.isEmailVerified()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Toast.makeText(getApplicationContext(), "Sign in successfully.",
                                                    Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), home_activity.class);
                                            startActivity(intent);
                                            finish();
                                        } else
                                            Toast.makeText(SignIn.this, "Email is  unverified",
                                                    Toast.LENGTH_SHORT).show();
                                    } else
                                        // If sign in fails, display the message to the user.
                                        Toast.makeText(SignIn.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                }
                            });
                } else progressBar.setVisibility(View.GONE);
            }
        });

        txtForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickForgotPassword();
            }
        });
    }

    private void onClickForgotPassword() {
        Intent intent = new Intent(getApplicationContext(), ForgetPassword.class);
        startActivity(intent);
        finish();
    }

    private boolean checkDataEntered() {
        if (isEmpty(txtEmail)) {
            Toast t = Toast.makeText(this, "You must enter email to register!", Toast.LENGTH_SHORT);
            t.show();
            return false;
        }
        if (isEmpty(txtPassword)) {
            Toast t = Toast.makeText(this, "Password is mandatory!", Toast.LENGTH_SHORT);
            t.show();
            return false;
        }
        return true;
    }

    private boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }
}
