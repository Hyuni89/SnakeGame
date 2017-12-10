package com.example.cho.snake;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by cho on 17. 12. 10.
 */

public class GameOverFragment extends Fragment {

    private Button restart;
    private Button scoreBoard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gameover, container, false);
        restart = (Button)view.findViewById(R.id.restartButton);
        scoreBoard = (Button)view.findViewById(R.id.scoreButton);

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).restartGame();
            }
        });

        return view;
    }
}
