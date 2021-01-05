package com.softgyan.doctor.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.softgyan.doctor.R;
import com.softgyan.doctor.adapter.FeedAdapter;
import com.softgyan.doctor.models.FeedModel;

import java.util.ArrayList;
import java.util.List;


public class FeedFragment extends Fragment {
    private static final int FEED_REQUEST_CODE = 100;
    private RecyclerView rvFeedContainer;
    public static List<FeedModel> feedModelList = new ArrayList<>();
    private FeedAdapter feedAdapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabRecording;

    private boolean isScrolling = false;
    private int postData;
    private int currentData;
    private int totalData;
    private static DocumentSnapshot previousDocument;

    private FeedFragment() {
    }

    public static FeedFragment getInstance() {
        return new FeedFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        rvFeedContainer = view.findViewById(R.id.rv_feed_container);
        progressBar = view.findViewById(R.id.progressBar);
        fabAdd = view.findViewById(R.id.floatingActionButton);
        fabRecording = view.findViewById(R.id.fab_recording);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        feedAdapter = new FeedAdapter(feedModelList);
        rvFeedContainer.setAdapter(feedAdapter);
        if (feedModelList.size() == 0) {
            getData(true);
        } else {
            feedAdapter.notifyDataSetChanged();
        }

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), NewFeedActivity.class);
            startActivityForResult(intent, FEED_REQUEST_CODE);
        });
        fabRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), RecordingActivity.class));
            }
        });

        rvFeedContainer.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                LinearLayoutManager manager = (LinearLayoutManager) rvFeedContainer.getLayoutManager();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FEED_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                String user_name = bundle.getString("user_name");
                String user_id = bundle.getString("user_id");
                String feed = bundle.getString("feed");
                String date = bundle.getString("date");
                String documentId = bundle.getString("document_id");
                FeedModel feedModel = new FeedModel(
                        user_name,
                        user_id,
                        date,
                        feed,
                        documentId
                );
                feedModelList.add(0, feedModel);
                feedAdapter.notifyItemInserted(0);
                rvFeedContainer.scrollToPosition(0);//remove it in case of it will not work
            }
        }

    }


    private void getData(boolean flag) {
        GetData getData = new GetData(new GetDataListener() {
            @Override
            public void onGetData(List<FeedModel> feedModels) {
                if (feedModels != null) {
                    feedModelList.addAll(feedModels);
                }
            }

            @Override
            public void onFailedListener(Exception ex) {
                Toast.makeText(getContext(), "onFailedListener" + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        if (flag) {
            getData.getFirstData();
        } else {
            getData.getNextFeedData();
        }
        feedAdapter.notifyDataSetChanged();
    }


    private class GetData {
        private final GetDataListener getDataListener;
        private final List<FeedModel> feedModels;
        private final FirebaseFirestore db;

        private GetData(GetDataListener getDataListener) {
            this.getDataListener = getDataListener;
            feedModels = new ArrayList<>();
            db = FirebaseFirestore.getInstance();
        }

        public void getFirstData() {
            progressBar.setVisibility(View.VISIBLE);
            Query first = db.collection("All_feeds")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(7);
            first.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                            previousDocument = documents.get(documents.size() - 1);
                            for (DocumentSnapshot ds : documents) {
                                String documentName = ds.getId();
                                FeedModel feedModel = new FeedModel(
                                        ds.getString("user_name"),
                                        ds.getString("user_id"),
                                        ds.getTimestamp("date").toDate().toString(),
                                        ds.getString("feed"),
                                        documentName
                                );
                                feedModels.add(feedModel);
                            }
                            getDataListener.onGetData(feedModels);
                        } catch (Exception ex) {
                            Toast.makeText(getContext(), "No more feeds", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        getDataListener.onFailedListener(e);
                    });
        }

        public void getNextFeedData() {
            if (previousDocument == null) {
                Toast.makeText(getContext(), "something is error! try again", Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            Query next = db.collection("All_feeds")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .startAfter(previousDocument)
                    .limit(5);
            next.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                            previousDocument = documents.get(documents.size() - 1);
                            for (DocumentSnapshot ds : documents) {
                                String documentId = ds.getId();
                                FeedModel feedModel = new FeedModel(
                                        ds.getString("user_name"),
                                        ds.getString("user_id"),
                                        ds.getTimestamp("date").toDate().toString(),
                                        ds.getString("feed"),
                                        documentId
                                );
                                feedModels.add(feedModel);
                            }
                            getDataListener.onGetData(feedModels);
                        } catch (Exception ex) {
                            Toast.makeText(getContext(), "No more feeds", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        getDataListener.onFailedListener(e);
                    });
        }

    }

    interface GetDataListener {
        void onGetData(List<FeedModel> feedModels);

        void onFailedListener(Exception ex);
    }
}
