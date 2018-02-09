package com.example.rdas6313.litedownloader;


import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;



public class MainActivity extends AppCompatActivity {

    private final String ACTIVE_FRAG = "active_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActiveDownloadFragment activeDownloadFragment = new ActiveDownloadFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container,activeDownloadFragment,ACTIVE_FRAG).commit();
    }

}
