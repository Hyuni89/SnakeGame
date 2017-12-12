package com.example.cho.snake;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

/**
 * Created by cho on 17. 12. 12.
 */

public class ScoreBoardFragment extends Fragment {

    private Button closeButton;
    private ListView scoreBoard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scoreboard, container, false);

        return view;
    }
}
