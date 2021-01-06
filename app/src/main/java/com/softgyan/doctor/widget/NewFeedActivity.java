package com.softgyan.doctor.widget;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.softgyan.doctor.R;
import com.softgyan.doctor.util.NetworkManagerCustom;
import com.softgyan.doctor.util.UserInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewFeedActivity extends AppCompatActivity {

    private TextView etFeed;
    private ProgressBar progressBar;
    private String now;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_feed);
        setSupportActionBar(findViewById(R.id.tool_bar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button btnPost = findViewById(R.id.btn_post);
        etFeed = findViewById(R.id.et_new_feed);
        progressBar = findViewById(R.id.progressBar);
        btnPost.setOnClickListener(v -> {
            String feed = etFeed.getText().toString().trim();
            if (NetworkManagerCustom.isOnline(NewFeedActivity.this)) {
                if (!TextUtils.isEmpty(feed)) {
                    connectDb(feed);
                } else {
                    Toast.makeText(NewFeedActivity.this, "edit box can't be empty", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(NewFeedActivity.this, "Internet connection is not available", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.tv_read_txt_file).setOnClickListener(view -> {
            checkPermissions();
        });
    }


    private void uploadPost(String feed, String document_id) {
        Bundle bundle = new Bundle();
        bundle.putString("feed", feed);
        bundle.putString("date", now);
        bundle.putString("user_name", UserInfo.getInstance(this).getUserName());
        bundle.putString("user_id", UserInfo.getInstance(this).getUserId());
        bundle.putString("document_id", document_id);

        Intent intent = getIntent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();

    }


    private String getCurrentDate() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            return dtf.format(now);
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault(Locale.Category.FORMAT));
            Date date = new Date();
            return formatter.format(date);
        }

    }

    private void connectDb(String feed) {
        now = getCurrentDate();
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> feedMap = new HashMap<>();
        feedMap.put("user_id", UserInfo.getInstance(this).getUserId());
        feedMap.put("user_name", UserInfo.getInstance(this).getUserName());
        feedMap.put("feed", feed);
        feedMap.put("date", now);
        FirebaseFirestore.getInstance()
                .collection("All_feeds")
                .add(feedMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String document_id = documentReference.getId();
                        uploadPost(feed, document_id);
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(NewFeedActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            readFromFile();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            readFromFile();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void readFromFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        startActivityForResult(intent, 2);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                String fileContent = readText(uri);
                etFeed.setText(fileContent);
            }
        }
    }

    private String readText(Uri uri) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            String line = "";

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

}