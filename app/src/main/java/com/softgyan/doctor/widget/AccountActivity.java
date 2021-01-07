package com.softgyan.doctor.widget;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.softgyan.doctor.R;
import com.softgyan.doctor.util.UserInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AccountActivity extends AppCompatActivity {

    private boolean isSignIn = false;
    private TextView tvMessage;
    private TextView tvSignIn;
    private EditText etUserName;
    private EditText etUserId;
    private Button btnAccount;
    private ProgressBar progressBar;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        tvMessage = findViewById(R.id.tv_option);
        tvSignIn = findViewById(R.id.tv_sign_in);
        etUserName = findViewById(R.id.et_user_name);
        etUserId = findViewById(R.id.et_user_id);
        progressBar = findViewById(R.id.progressBar);
        btnAccount = findViewById(R.id.btn_sign_in);
        ImageButton btnClose = findViewById(R.id.close_btn);

        btnClose.setOnClickListener(v -> finish());

        btnAccount.setOnClickListener(v -> {
            if (isSignIn) {
                String userId = etUserId.getText().toString().trim();
                if (!TextUtils.isEmpty(userId)) {
                    signIn(userId);
                } else {
                    Toast.makeText(AccountActivity.this, "please enter the user id", Toast.LENGTH_SHORT).show();
                }
            } else {
                String userName = etUserName.getText().toString().trim();
                if (!TextUtils.isEmpty(userName)) {
                    if(isOnline()) {
                        signUp(userName);
                    }else {
                        Toast.makeText(this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AccountActivity.this, "please enter the user name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvSignIn.setOnClickListener(v -> setSignIn());

        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setSignIn();
    }

    private void signUp(String userName) {
        progressBar.setVisibility(View.VISIBLE);
        final String uid = getUID();
        Map<String, Object> userData = new HashMap<>();
        userData.put("user_name", userName);
        userData.put("user_id", uid);
        db.collection("Users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        etUserId.setText(uid);
                        Toast.makeText(AccountActivity.this, "your account created", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        setSignIn();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AccountActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void signIn(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try {
                            if (task.isSuccessful()) {
                                String uid = Objects.requireNonNull(task.getResult()).getString("user_id");
                                if (userId.equals(uid)) {
                                    UserInfo.getInstance(AccountActivity.this).setFileValue(0);
                                    String uName = task.getResult().getString("user_name");
                                    UserInfo.getInstance(AccountActivity.this).setUserInfo(uid, uName);
                                    Toast.makeText(AccountActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
                                    startMainActivity();
                                } else {
                                    Toast.makeText(AccountActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        } catch (Exception ex) {
                            Toast.makeText(AccountActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AccountActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }


    private void setSignIn() {
        isSignIn = !isSignIn;
        if (isSignIn) {
            etUserId.setVisibility(View.VISIBLE);
            etUserName.setVisibility(View.GONE);
            tvSignIn.setText(R.string.sign_up);
            tvMessage.setText(R.string.sign_in);
            btnAccount.setText(R.string.sign_in);
        } else {
            etUserId.setVisibility(View.GONE);
            etUserName.setVisibility(View.VISIBLE);
            tvSignIn.setText(R.string.sign_in);
            tvMessage.setText(R.string.sign_up);
            btnAccount.setText(R.string.sign_up);
        }
    }


    private String getUID() {
        String uid;
        uid = UUID.randomUUID().toString();
        uid = uid.substring(0, 10);
        return uid;
    }

    private void startMainActivity() {
        Intent startMain = new Intent(this, MainActivity.class);
        startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private boolean isOnline(){
        ConnectivityManager mgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mgr.getActiveNetworkInfo();
        if (netInfo != null) {
            return netInfo.isConnected();
        } else {
            return false;
        }
    }
}