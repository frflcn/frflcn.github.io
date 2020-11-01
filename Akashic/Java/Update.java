package com.example.akashic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import static com.example.akashic.AlarmCheckerReceiver.UPDATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Update#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Update extends Fragment {

    private Update.UpdateListener updateListener;
    private EditText editLongitude;
    private EditText editLatitude;
    private Switch atmosphereSwitch;
    private Button iveBowedButton;

    public interface UpdateListener{
        void onSubmitLatitudeLongitude(double latitude, double longitude);

        void onGetLocation();
        void onAtmosphereSwitch();
    }

    public Update() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static Update newInstance() {
        Update fragment = new Update();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if (context instanceof Update.UpdateListener) {
            updateListener = (Update.UpdateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentAListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        updateListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_update, container, false);
        editLatitude = (EditText) v.findViewById(R.id.editLatitude);
        editLongitude = (EditText) v.findViewById(R.id.editLongitude);
        Button latitudelongitudebutton = (Button) v.findViewById(R.id.submitLatitudeLongitude);
        Button getLocationButton = (Button) v.findViewById(R.id.getLocationButton);
        SharedPreferences sharedPref = getActivity().getSharedPreferences("ALARMS", Context.MODE_PRIVATE);
        final SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        atmosphereSwitch = (Switch) v.findViewById(R.id.atmosphereSwitch);
        iveBowedButton = (Button) v.findViewById(R.id.bowedButton);
        atmosphereSwitch.setChecked(sharedPref.getBoolean("ATMOSPHERE_EFFECTS", false));




        getLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateListener.onGetLocation();
            }
        });


        latitudelongitudebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double latitudeinput = MainActivity.latitude;
                double longitudeinput = MainActivity.longitude;
                if (!editLatitude.getText().toString().isEmpty()) {
                    latitudeinput = Double.parseDouble(editLatitude.getText().toString());
                }

                if (!editLongitude.getText().toString().isEmpty()){
                    longitudeinput = Double.parseDouble(editLongitude.getText().toString());
                }

                updateListener.onSubmitLatitudeLongitude(latitudeinput, longitudeinput);
            }
        });


        atmosphereSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPrefEdit.putBoolean("ATMOSPHERE_EFFECTS", b).commit();
                updateListener.onAtmosphereSwitch();

                Intent intent = new Intent(getActivity(), AlarmCheckerReceiver.class);
                intent.putExtra("TIME_OF_DAY", AlarmCheckerReceiver.ALL);
                intent.putExtra("MODE", UPDATE);
                getActivity().sendBroadcast(intent);

        }});

        iveBowedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IveBowedDialog iveBowedDialog = new IveBowedDialog();
                iveBowedDialog.show(getFragmentManager(), "Ive Bowed Dialog");
            }
        });
        return v;


    }
}