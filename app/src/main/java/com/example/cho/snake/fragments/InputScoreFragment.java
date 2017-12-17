package com.example.cho.snake.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.cho.snake.MainActivity;
import com.example.cho.snake.R;

/**
 * Created by cho on 17. 12. 12.
 */

public class InputScoreFragment extends Fragment {

    private Button confirm;
    private EditText nameText;
    private TextView scoreText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.inputscore, container, false);
        confirm = (Button)view.findViewById(R.id.inputButton);
        nameText = (EditText)view.findViewById(R.id.inputName);
        scoreText = (TextView)view.findViewById(R.id.newScore);

        final int score = getArguments().getInt("score");
        scoreText.setText(Integer.toString(score));

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputName = nameText.getText().toString();
                if(inputName.getBytes().length <= 0) {
                    inputName = "AAA";
                }
                ((MainActivity)getActivity()).updateDB(inputName, score);
            }
        });

        return view;
    }

}
