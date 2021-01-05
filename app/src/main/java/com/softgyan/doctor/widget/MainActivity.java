package com.softgyan.doctor.widget;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.softgyan.doctor.R;
import com.softgyan.doctor.models.FeedModel;
import com.softgyan.doctor.util.UserInfo;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = findViewById(R.id.vp_fragment);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        setWithViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.info) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("User Info");
            builder.setMessage(
                    "User Name :" + UserInfo.getInstance(this).getUserName() + "\n" +
                            "User Id : " + UserInfo.getInstance(this).getUserId()
            );
            builder.setPositiveButton("Okay", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        } else if (id == R.id.logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("LogOut");
            builder.setMessage("Do you want to logOut?");
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            builder.setPositiveButton("Yes", (dialog, which) -> {
                FeedFragment.feedModelList.clear();
                UserInfo.getInstance(MainActivity.this).logOut();
                finish();
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            builder.create().show();

        } else if (id == R.id.read_text_file) {
            checkPermission();
        }else if(id == R.id.download_file){
            startActivity(new Intent(MainActivity.this, DownloadAudioActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void setWithViewPager(ViewPager viewPager) {
        MainActivity.SelectionPagerAdapter adapter = new SelectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(FeedFragment.getInstance(), getString(R.string.my_feed));
        adapter.addFragment(MyRecordingFragment.getInstance(), getString(R.string.my_recording));
        viewPager.setAdapter(adapter);
    }

    private void startFeedActivity() {
        Intent feedIntent = new Intent(this, FeedDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("feed", "");
        bundle.putString("date", "");
        bundle.putString("user_name", "");
        bundle.putBoolean("file", true);
        feedIntent.putExtra("feed_details",bundle);
        startActivity(feedIntent);

    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            startFeedActivity();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startFeedActivity();
        } else {
            Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    public static class SelectionPagerAdapter extends FragmentPagerAdapter {
        private final ArrayList<Fragment> mFragmentList = new ArrayList<>();
        private final ArrayList<String> mFragmentTitleList = new ArrayList<>();

        public SelectionPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}