package com.example.akashic;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DateToy#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DateToy extends Fragment {
MediaPlayer player;
    private Button plusButton;
    private Button minusButton;
    private Button yearButton;
    private Button monthButton;
    private Button dayButton;
    private Button hourButton;
    private Button minuteButton;
    private Button oneButton;
    private Button fiveButton;
    private Button tenButton;
    private Button twentyButton;
    private Button nowButton;
    private TextView yearText;
    private TextView monthText;
    private TextView dayText;
    private TextView hourText;
    private TextView minuteText;
    private TextView DSTText;
    private TextView timeZoneText;
    private TextView millisText;
    private TextView secondsText;

    private int red;
    private int white;

    private int amount;
    private int time;

    private Calendar c;

    public DateToy() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static DateToy newInstance(String param1, String param2) {
        DateToy fragment = new DateToy();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_date_toy, container, false);
        c = Calendar.getInstance();

        plusButton = v.findViewById(R.id.Plus);
        minusButton = v.findViewById(R.id.Minus);
        yearButton = v.findViewById(R.id.YearButton);
        monthButton = v.findViewById(R.id.monthButton);
        dayButton = v.findViewById(R.id.Daybutton);
        hourButton = v.findViewById(R.id.HourButton);
        minuteButton = v.findViewById(R.id.MinuteButton);

        nowButton = v.findViewById(R.id.get_now);
        oneButton = v.findViewById(R.id.One);
        fiveButton = v.findViewById(R.id.Five);
        tenButton = v.findViewById(R.id.Ten);
        twentyButton = v.findViewById(R.id.twenty);
        yearText = v.findViewById(R.id.Year);
        monthText = v.findViewById(R.id.month);
        dayText = v.findViewById(R.id.Day);
        hourText = v.findViewById(R.id.Hour);
        minuteText = v.findViewById(R.id.Minutes);
        DSTText = v.findViewById(R.id.DST);
        timeZoneText = v.findViewById(R.id.TimeZone);
        millisText = v.findViewById(R.id.millis);
        secondsText = v.findViewById(R.id.Seconds);

        c = MainActivity.rightNow;
        getTime();

        red = 0xFFE91E63;
        white = 0xFFFFFFFF;


        nowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c = MainActivity.rightNow;
                getTime();
            }
        });
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c.add(time, amount);
                getTime();
            }
        });
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c.add(time, -amount);
                getTime();

            }
        });
        yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time = Calendar.YEAR;
                timeClicked(time);
            }
        });
        monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time = Calendar.MONTH;
                timeClicked(time);
            }
        });
        dayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time = Calendar.DATE;
                timeClicked(time);
            }
        });
        hourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time = Calendar.HOUR_OF_DAY;
                timeClicked(time);
            }
        });
        minuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time = Calendar.MINUTE;
                timeClicked(time);
            }
        });
        oneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = 1;
                amountClicked(amount);
            }
        });
        fiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = 5;
                amountClicked(amount);
            }
        });
        tenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = 10;
                amountClicked(amount);
            }
        });
        twentyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = 20;
                amountClicked(amount);

            }
        });



        return v;


    }
    public void timeClicked(int time){
        yearButton.setBackgroundColor(white);
        monthButton.setBackgroundColor(white);
        dayButton.setBackgroundColor(white);
        hourButton.setBackgroundColor(white);
        minuteButton.setBackgroundColor(white);
        switch(time){
            case Calendar.YEAR:
                yearButton.setBackgroundColor(red);
                break;
            case Calendar.MONTH:
                monthButton.setBackgroundColor(red);
                break;
            case Calendar.DATE:
                dayButton.setBackgroundColor(red);
                break;
            case Calendar.HOUR_OF_DAY:
                hourButton.setBackgroundColor(red);
                break;
            case Calendar.MINUTE:
                minuteButton.setBackgroundColor(red);
                break;
        }
    }

    public void amountClicked(int amount){
        oneButton.setBackgroundColor(white);
        fiveButton.setBackgroundColor(white);
        tenButton.setBackgroundColor(white);
        twentyButton.setBackgroundColor(white);

        switch (amount){
            case 1:
                oneButton.setBackgroundColor(red);
                break;
            case 5:
                fiveButton.setBackgroundColor(red);
                break;
            case 10:
                tenButton.setBackgroundColor(red);
                break;
            case 20:
                twentyButton.setBackgroundColor(red);
                break;
        }
    }
    public void getTime(){
        yearText.setText(Integer.toString(c.get(Calendar.YEAR)));
        monthText.setText(Integer.toString(c.get(Calendar.MONTH)));
        dayText.setText(Integer.toString(c.get(Calendar.DATE)));
        hourText.setText(Integer.toString(c.get(Calendar.HOUR)));
        minuteText.setText(Integer.toString(c.get(Calendar.MINUTE)));
        DSTText.setText(Integer.toString(c.get(Calendar.DST_OFFSET)));
        timeZoneText.setText(Integer.toString(c.get(Calendar.ZONE_OFFSET)));
        millisText.setText(Long.toString(c.getTimeInMillis()));
        secondsText.setText(Long.toString(c.get(Calendar.SECOND)));
    }
}
