package com.example.cho.snake;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by cho on 17. 12. 12.
 */

public class ScoreBoardFragment extends Fragment {

    private Button closeButton;
    private ListView scoreBoard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scoreboard, container, false);

        scoreBoard = (ListView)view.findViewById(R.id.scoreList);
        closeButton = (Button)view.findViewById(R.id.closeButton);

        ScoreBoardAdapter adapter = new ScoreBoardAdapter(((MainActivity)getActivity()).getAll());
        scoreBoard.setAdapter(adapter);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).restartGame();
            }
        });

        return view;
    }
}

class ScoreBoardAdapter extends BaseAdapter {

    private ArrayList<RecordInfo> items;

    ScoreBoardAdapter(ArrayList<RecordInfo> in) {
        items = in;
        Collections.sort(items);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Context context = viewGroup.getContext();

        if(view == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listviewitem, viewGroup, false);
        }

        TextView index = (TextView)view.findViewById(R.id.itemIndex);
        TextView name = (TextView)view.findViewById(R.id.itemName);
        TextView score = (TextView)view.findViewById(R.id.itemScore);

        index.setText(Integer.toString(i + 1));
        name.setText(items.get(i).name);
        score.setText(Integer.toString(items.get(i).score));

        return view;
    }
}