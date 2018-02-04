package com.example.rdas6313.litedownloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Adapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView)findViewById(R.id.listView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new Adapter(this);
        recyclerView.setAdapter(adapter);

    }

    /*private void TestUI(){
        for(int i=0;i<10;i++){
            DownloadInformation information = new DownloadInformation("Pal by Arijit Singh",10+(i*10),10000,9999);
            adapter.add(information);
        }
    }*/
}
