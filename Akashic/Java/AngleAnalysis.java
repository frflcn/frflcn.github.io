package com.example.akashic;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AngleAnalysis#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AngleAnalysis extends Fragment {


    public AngleAnalysis() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AngleAnalysis newInstance() {
        AngleAnalysis fragment = new AngleAnalysis();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_angle_analysis, container, false);

        TextView[] angles = new TextView[12];
        angles[0] = v.findViewById(R.id.angle1);
        angles[1] = v.findViewById(R.id.angle2);
        angles[2] = v.findViewById(R.id.angle3);
        angles[3] = v.findViewById(R.id.angle4);
        angles[4] = v.findViewById(R.id.angle5);
        angles[5] = v.findViewById(R.id.angle6);
        angles[6] = v.findViewById(R.id.angle7);
        angles[7] = v.findViewById(R.id.angle8);
        angles[8] = v.findViewById(R.id.angle9);
        angles[9] = v.findViewById(R.id.angle10);
        angles[10] = v.findViewById(R.id.angle11);
        angles[11] = v.findViewById(R.id.angle12);

        for (int i = 0; i < 12; i++) {
            angles[i].setText(Double.toString(MainActivity.anglesOfTheSun[(i * 2 * 2) + (7 * 60 * 2) + (5 * 20)]));
        }


        return v;
    }
}