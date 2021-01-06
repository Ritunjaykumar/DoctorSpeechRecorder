package com.softgyan.doctor.widget;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.softgyan.doctor.R;
import com.softgyan.doctor.util.RecordingManager;
import com.softgyan.doctor.util.UserInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class FeedDetailsActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    private TextView tvUserName;
    private TextView tvUploadDate;
    private TextView tvContent;
    private FloatingActionButton btnRecord;
    private Chronometer recordTimer;
    private TextView tvRecordOption;

    private TextView tvRecordFileName;
    private RecordingManager recordingManager;
    private String document_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //initialize ui
        tvUserName = findViewById(R.id.tv_user_name);
        tvUploadDate = findViewById(R.id.tv_upload_date);
        tvContent = findViewById(R.id.tv_feed_container);
        btnRecord = findViewById(R.id.floating_action_button);
        tvRecordFileName = findViewById(R.id.textView2);
        recordTimer = findViewById(R.id.record_timer);
        tvRecordOption = findViewById(R.id.tv_record_option);

        recordingManager = new RecordingManager(this);

        btnRecord.setOnClickListener(v -> {
            if (checkPermission()) {
                if (!recordingManager.isRecordingStart()) {
                    recordAudio();
                } else {
                    stopRecording();
                }
            } else {
                ActivityCompat.requestPermissions(FeedDetailsActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 100);
            }
        });

        if (getIntent() != null) {
            Bundle bundle = getIntent().getBundleExtra("feed_details");
            String userName = bundle.getString("user_name");
            String date = bundle.getString("date");
            String feed = bundle.getString("feed");
            document_id = bundle.getString("document_id");
            boolean isFile = bundle.getBoolean("file", false);
            if (isFile) {
                readTextFile();
                document_id = UUID.randomUUID().toString().substring(0, 10)+"&from_file";
            } else {
                document_id = document_id.concat("from_feed");
                setData(userName, date, feed);
            }
        }
    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void recordAudio() {
        tvRecordOption.setText("Recording Started");
        tvRecordOption.setTextColor(ContextCompat.getColor(FeedDetailsActivity.this, R.color.green));
        btnRecord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop));
        String fileName = recordingManager.startRecording(document_id);
        recordTimer.setBase(SystemClock.elapsedRealtime());
        recordTimer.start();
        tvRecordFileName.setText(fileName);
    }

    private void stopRecording() {
        tvRecordOption.setText("Recording not Started");
        tvRecordOption.setTextColor(ContextCompat.getColor(FeedDetailsActivity.this, R.color.red));
        recordTimer.stop();
        btnRecord.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_recording));
        recordingManager.stopRecording();
    }

    private void setData(String userName, String date, String feed) {
        tvUserName.setText(userName);
        tvUploadDate.setText(date);
        tvContent.setText(feed);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (recordingManager.isRecordingStart()) {
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
            stopRecording();
        }
    }

    // start code for reading txt file
    private void readTextFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        startActivityForResult(intent, REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                String fileContent = readText(uri);
                setData(
                        UserInfo.getInstance(FeedDetailsActivity.this).getUserName(),
                        getTodayDate(),
                        fileContent
                );
            }
        }else {
            finish();
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


    private String getTodayDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }
}