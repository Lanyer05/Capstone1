package com.hcdc.capstone.transactionprocess;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.hcdc.capstone.R;

public class TaskComplete extends Fragment {

    RecyclerView completedTaskrecord;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.completedtask_fragment, container, false);

        // Add your code to initialize and populate the first tab's content here

        return view;
    }
}
