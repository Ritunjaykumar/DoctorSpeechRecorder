package com.softgyan.doctor.widget;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.softgyan.doctor.R;
import com.softgyan.doctor.adapter.AudioAdapter;
import com.softgyan.doctor.util.UserInfo;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Objects;


public class MyRecordingFragment extends Fragment {

    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView rvAudioContainer;
    private File[] audioFiles;
    private AudioAdapter audioAdapter;
    private TextView noFile;
    //for music controller
    private TextView title;
    private TextView tvFileName;
    private ImageView ivControl;
    private ImageView ivForward;
    private ImageView ivBackward;
    private LinearLayout header;
    private SeekBar seekBar;
    private Handler handler;
    private Runnable updateSeekBar;

    private MediaPlayer mediaPlayer = null;
    private File fileToPlay;
    private boolean isPlaying = true;

    private View currentView=null, previousView=null;
    private int position=-1;

    private MyRecordingFragment() {
    }

    public static MyRecordingFragment getInstance() {
        return new MyRecordingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_recording, container, false);
        ConstraintLayout constraintLayout = view.findViewById(R.id.constraint_layout);
        bottomSheetBehavior = BottomSheetBehavior.from(constraintLayout);
        rvAudioContainer = view.findViewById(R.id.rv_audio_container);
        noFile = view.findViewById(R.id.not_file);

        header = view.findViewById(R.id.ll_header);
        title = view.findViewById(R.id.player_header);
        tvFileName = view.findViewById(R.id.music_title);
        ivControl = view.findViewById(R.id.iv_control);
        ivBackward = view.findViewById(R.id.iv_backword);
        ivForward = view.findViewById(R.id.iv_forword);
        seekBar = view.findViewById(R.id.seek_bar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        header.setOnClickListener(v -> {
            setBehavior();
        });
        ivControl.setOnClickListener(v -> {
            if(fileToPlay != null) {
                if(isPlaying){
                    playAudio(fileToPlay);
                }else {
                    if (mediaPlayer.isPlaying()) {
                        pause();
                    } else {
                        resume();
                    }
                }
            }else{
                Toast.makeText(getContext(), "song are not selected", Toast.LENGTH_SHORT).show();
            }
        });
//        ivForward.setOnClickListener(v -> {
//            int nextPos = position+1;
//            if(position != -1 && nextPos < audioFiles.length){
//                forwardBackward(position, nextPos);
//            }else {
//                Toast.makeText(getContext(), "no more file", Toast.LENGTH_SHORT).show();
//            }
//        });
//        ivBackward.setOnClickListener((v -> {
//            int prePos = position-1;
//            if(position != -1 && prePos > -1){
//                forwardBackward(position, prePos);
//            }else {
//                Toast.makeText(getContext(), "no more file", Toast.LENGTH_SHORT).show();
//            }
//        }));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(fileToPlay != null) {
                    pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seek) {
                if(fileToPlay!=null) {
                    mediaPlayer.seekTo(seek.getProgress());
                    resume();
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        String path = Objects.requireNonNull(getContext()).getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        audioFiles = directory.listFiles(pathname -> {
            String[] fileName = pathname.getName().split("&");
            if(fileName[0].equals(UserInfo.getInstance(getContext()).getUserId())){
                return true;
            }else {
                return false;
            }
        });
        if (audioFiles.length != 0) {
            rvAudioContainer.setVisibility(View.VISIBLE);
            noFile.setVisibility(View.GONE);
        } else {
            rvAudioContainer.setVisibility(View.GONE);
            noFile.setVisibility(View.VISIBLE);
        }

        audioAdapter = new AudioAdapter(getContext(),audioFiles, (file, position, currentView, previousView) -> {
            this.currentView = currentView;
            this.previousView = previousView;
            this.position = position;
            fileToPlay = file;
            if (!isPlaying) {
                stopAudio();
            }
            playAudio(fileToPlay);
        });
        rvAudioContainer.setAdapter(audioAdapter);
        audioAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("my_tag","onPause");
    }

    private void stopAudio() {
        isPlaying = true;
        ivControl.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.ic_play_now));
        title.setText(R.string.not_playing);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mediaPlayer.stop();
        tvFileName.setText("no audio selected");
        handler.removeCallbacks(updateSeekBar);
        mediaPlayer = null;
    }

    private void playAudio(File fileToPlay) {
        ivControl.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.ic_pause));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> isPlaying = true);
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        seekBar.setMax(mediaPlayer.getDuration());
        handler = new Handler();
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(updateSeekBar, 0);
        tvFileName.setText(fileToPlay.getName());
        title.setText(R.string.playing);
        isPlaying = false;
        updateRunnable();
    }

    private void updateRunnable(){
        mediaPlayer.setOnCompletionListener(mp -> {
            stopAudio();
            title.setText(R.string.finish);
            ivControl.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.ic_play_now));
        });
    }


    private void pause(){
        title.setText(R.string.pause);
        mediaPlayer.pause();
        ivControl.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.ic_play_now));
    }
    private void resume(){
        mediaPlayer.start();
        title.setText(R.string.playing);
        ivControl.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.ic_pause));
        handler.removeCallbacks(updateSeekBar);
        updateRunnable();
        handler.postDelayed(updateSeekBar, 100);
    }

    private void setBehavior() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            stopAudio();
            Toast.makeText(getContext(), "music stopped", Toast.LENGTH_SHORT).show();
        }
        Log.i(",my_tag","onStop");
    }

//    private void forwardBackward(int position, int pre){
//        File currentFile = audioFiles[pre];
//        if(currentFile != null) {
//            stopAudio();
//            playAudio(currentFile);
//        }
//        if(previousView != null) {
//            previousView.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.accent));
//            currentView = audioAdapter.getCurrentView();
//            if(currentView != null){
//                currentView.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.secondary_text));
//            }
//        }
//        audioAdapter.notifyItemChanged(position);
//        audioAdapter.notifyItemChanged(pre);
//        position = pre;
//
//    }
}