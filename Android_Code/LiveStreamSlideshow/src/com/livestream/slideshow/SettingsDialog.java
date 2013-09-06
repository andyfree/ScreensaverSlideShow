package com.livestream.slideshow;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Spinner;

public class SettingsDialog extends DialogFragment implements OnItemSelectedListener, OnValueChangeListener, OnClickListener {

    public interface SettingsUpdatedListener {
    	
        void settingsChanged(Settings settings);
        
    }

    
    private NumberPicker presetPicker;
    private NumberPicker durationPicker;
    private Spinner presetTimeFormatSpinner;
    private Spinner durationTimeFormatSpinner;
    
    private int presetTime = 5;
    private int durationTime = 10;
    private String presetFormat = "seconds";
    private String durationFormat = "seconds";
    
    private Button updateSettings;

    public SettingsDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container);
        updateSettings = (Button) view.findViewById(R.id.updateSettings);
        durationTimeFormatSpinner = (Spinner) view.findViewById(R.id.durationTimeFormat);
        presetTimeFormatSpinner =  (Spinner) view.findViewById(R.id.presetTimeFormat);
        durationPicker = (NumberPicker) view.findViewById(R.id.duration);
        durationPicker.setMaxValue(60);
        durationPicker.setMinValue(2);
        
        presetPicker = (NumberPicker) view.findViewById(R.id.preset);
        presetPicker.setMaxValue(60);
        presetPicker.setMinValue(4);
        
//        mEditText = (EditText) view.findViewById(R.id.txt_your_name);
//        getDialog().setTitle("Hello");
//
//        // Show soft keyboard automatically
//        mEditText.requestFocus();
//        getDialog().getWindow().setSoftInputMode(
//                LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//        mEditText.setOnEditorActionListener(this);
        
        durationTimeFormatSpinner.setOnItemSelectedListener(this);
        presetTimeFormatSpinner.setOnItemSelectedListener(this);
        presetPicker.setOnValueChangedListener(this);
        durationPicker.setOnValueChangedListener(this);
        updateSettings.setOnClickListener(this);
        
        return view;
    }


	@Override
	public void onItemSelected(AdapterView<?> parent, View arg1, int position,
			long id) {
		
		String format = (String) parent.getItemAtPosition(position);
		if(id == R.id.durationTimeFormat){
			durationFormat = format;
		}else{
			presetFormat = format;
		}
		
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldValue, int newValue) {
		
		if(picker.getId() == R.id.preset){
			presetTime = newValue;
		}else{
			durationTime = newValue;
		}
		
	}

	@Override
	public void onClick(View v) {
		
		// Update the settings
		Settings settings = new Settings(presetTime, durationTime, presetFormat, durationFormat);
		SettingsUpdatedListener activity = (SettingsUpdatedListener) getActivity();
		activity.settingsChanged(settings);
		this.dismiss();
		
	}
}
