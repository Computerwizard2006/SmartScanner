package com.example.smartscanner.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartscanner.R;
import com.example.smartscanner.adapters.HistoryAdapter;
import com.example.smartscanner.models.ScanModel;
import com.example.smartscanner.utils.HistoryManager;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty); // Make sure this ID exists in activity_history.xml
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadHistory();
    }

    private void loadHistory() {
        // Run on background thread to prevent UI freezing
        new Thread(() -> {
            ArrayList<ScanModel> historyList = HistoryManager.getHistory(this);
            
            runOnUiThread(() -> {
                if (historyList.isEmpty()) {
                    if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    HistoryAdapter adapter = new HistoryAdapter(historyList);
                    recyclerView.setAdapter(adapter);
                }
            });
        }).start();
    }
}