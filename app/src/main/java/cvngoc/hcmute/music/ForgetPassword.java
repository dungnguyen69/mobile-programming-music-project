package cvngoc.hcmute.music;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ForgetPassword extends AppCompatActivity {
    private Button btnReset;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private EditText txtEmail;
    private String email;
    private TextView SignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        txtEmail = findViewById(R.id.txtEmail);
        btnReset = findViewById(R.id.btnReset);
        progressBar = findViewById(R.id.progressBar);
        SignIn = findViewById(R.id.sign_in);
        auth = FirebaseAuth.getInstance();

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (checkDataEntered()) {
                    email = txtEmail.getText().toString();
                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful())
                                        Toast.makeText(getApplicationContext(), "Email sent",
                                                Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(ForgetPassword.this, "Email is unregistered",
                                                Toast.LENGTH_SHORT).show();
                                }
                            });
                } else progressBar.setVisibility(View.GONE);
            }
        });

        SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignIn.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean checkDataEntered() {
        if (isEmpty(txtEmail)) {
            Toast t = Toast.makeText(this, "You must enter email !", Toast.LENGTH_SHORT);
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