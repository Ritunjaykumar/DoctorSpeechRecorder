package com.softgyan.doctor.widget;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.softgyan.doctor.R;
import com.softgyan.doctor.adapter.DownloadAudioAdapter;
import com.softgyan.doctor.models.AudioModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadAudioActivity extends AppCompatActivity {

    private RecyclerView rvAudioContainer;
    private static final List<AudioModel> audioModelList = new ArrayList<>();
    private ProgressBar progressBar;
    private DocumentSnapshot previousDocument;
    private DownloadAudioAdapter audioAdapter;
    private boolean isScrolling = false;
    private int postData;
    private int currentData;
    private int totalData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_audio);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rvAudioContainer = findViewById(R.id.rv_audio_container);
        progressBar = findViewById(R.id.progress_bar);
        setRecyclerView();
    }

    private void setRecyclerView() {
        if (audioModelList.size() == 0) {
            getData(true);
        }
        audioAdapter = new DownloadAudioAdapter(audioModelList, this, new DownloadAudioAdapter.OnDownloadListener() {
            @Override
            public void onDownload(String downloadUrl, String fileName) {
                if(getPermission()){
                    downloadAudioFile(downloadUrl, fileName);
                }else {
                    Toast.makeText(DownloadAudioActivity.this, "Don't have permission to write file!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        rvAudioContainer.setAdapter(audioAdapter);

        rvAudioContainer.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                currentData = manager.getChildCount();
                totalData = manager.getItemCount();
                postData = manager.findFirstVisibleItemPosition();
                if (isScrolling && ((currentData + postData) == totalData)) {
                    isScrolling = false;
                    getData(false);
                }
            }
        });

    }

    private void downloadAudioFile( String downloadUrl,  String fileName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(downloadUrl);

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle(fileName);
        pd.setMessage("Downloading Please Wait!");
        pd.setCancelable(false);
        pd.setProgress(0);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();

        try {
            File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "dsr_"+fileName);

            Log.d("my_tag", "" + localFile.getAbsolutePath());
            storageRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                            if (localFile.canRead()) {
                                pd.dismiss();
//                            }
                            Toast.makeText(DownloadAudioActivity.this, "Download Completed", Toast.LENGTH_SHORT).show();
                            Toast.makeText(DownloadAudioActivity.this, "File saved in download directory", Toast.LENGTH_LONG).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            exception.printStackTrace();
                            pd.dismiss();
                            Toast.makeText(DownloadAudioActivity.this, "Download InCompleted", Toast.LENGTH_LONG).show();
                            Log.d("my_tag", "Download InCompleted : " + exception.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                            int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            pd.setProgress(currentProgress);
                        }
                    });

        } catch (Exception ex) {
            Toast.makeText(this, "file not created", Toast.LENGTH_SHORT).show();
        }
    }

    private void getData(boolean flag) {
        GetAudioData getAudioData = new GetAudioData(new GetAudioDataListener() {
            @Override
            public void onGetAudioData(List<AudioModel> audioModels) {
                audioModelList.addAll(audioModels);
                audioAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailedListener(Exception ex) {
                Toast.makeText(DownloadAudioActivity.this, "loading failed " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        if (flag) {
            getAudioData.getFirstData();
        } else {
            getAudioData.getNextFeedData();
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean getPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            return false;
        }
    }



    //for getting data from server
    private class GetAudioData {
        private final GetAudioDataListener getAudioDataListener;
        private final List<AudioModel> audioModels;
        private final FirebaseFirestore db;

        private GetAudioData(GetAudioDataListener getAudioDataListener) {
            this.getAudioDataListener = getAudioDataListener;
            audioModels = new ArrayList<>();
            db = FirebaseFirestore.getInstance();
        }

        public void getFirstData() {
            progressBar.setVisibility(View.VISIBLE);
            Query first = db.collection("Media")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(7);
            first.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                            previousDocument = documents.get(documents.size() - 1);
                            for (DocumentSnapshot ds : documents) {
                                AudioModel audioModel = new AudioModel(
                                        ds.getString("user_id"),
                                        ds.getString("user_name"),
                                        ds.getString("date"),
                                        ds.getString("download_url"),
                                        ds.getString("file_name")

                                );
                                audioModels.add(audioModel);
                            }
                            getAudioDataListener.onGetAudioData(audioModels);
                        } catch (Exception ex) {
                            Toast.makeText(DownloadAudioActivity.this, "No more data!", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        getAudioDataListener.onFailedListener(e);
                    });
        }

        public void getNextFeedData() {
            if (previousDocument == null) {
                Toast.makeText(DownloadAudioActivity.this, "something is error try again!", Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            Query next = db.collection("Media")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .startAfter(previousDocument)
                    .limit(5);
            next.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                            previousDocument = documents.get(documents.size() - 1);
                            for (DocumentSnapshot ds : documents) {
                                AudioModel audioModel = new AudioModel(
                                        ds.getString("user_id"),
                                        ds.getString("user_name"),
                                        ds.getString("date"),
                                        ds.getString("download_url"),
                                        ds.getString("file_name")

                                );
                                audioModels.add(audioModel);
                            }
                            getAudioDataListener.onGetAudioData(audioModels);
                        } catch (Exception ex) {
                            Toast.makeText(DownloadAudioActivity.this, "No more audio files", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        getAudioDataListener.onFailedListener(e);
                    });
        }

    }

    interface GetAudioDataListener {
        void onGetAudioData(List<AudioModel> audioModels);

        void onFailedListener(Exception ex);
    }


}