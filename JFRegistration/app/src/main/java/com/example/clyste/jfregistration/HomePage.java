package com.example.clyste.jfregistration;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;


public class HomePage extends Fragment {

    private static final String USERKEY = "userkey";
    private View homeView;
    private AssetManager assetManager;
    private TableLayout userTable;
    private ImageButton searchButton;
    private ImageButton myRegistration;
    private JSONObject userEntry;


    public HomePage() {
        // Required empty public constructor
    }

    public static HomePage newInstance(JSONObject userJson) {
        HomePage fragment = new HomePage();
        Bundle args = new Bundle();
        Log.i("jsf", "newInstance: " + userJson.toString());
        args.putString(USERKEY, userJson.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("jsf", "onCreate: Home");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.i("jsf", "onCreate: has args");
            String user = getArguments().getString(USERKEY);
            try {
                userEntry = new JSONObject(user);
            }
            catch(JSONException e){
                Log.i("jsf", "onCreate: JSON exception " + e);
            }
        }
        else{
            //userEntry = readUserData();
            Log.i("jsf", "onCreate: no args!!!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("jsf", "onCreateView: Home");
        homeView = inflater.inflate(R.layout.fragment_home_page, container, false);
        userTable = homeView.findViewById(R.id.homeUserTable);
        searchButton = homeView.findViewById(R.id.searchCoursesButton);
        myRegistration = homeView.findViewById(R.id.viewCoursesButton);
        searchButton.setOnClickListener(event->beginSearch());
        myRegistration.setOnClickListener(event->viewRegistration());
        buildScreen(userEntry);
        assetManager = getActivity().getAssets();
        return homeView;
    }


    private void buildScreen(JSONObject userData){
        TableRow.LayoutParams parameters = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT);
        Iterator keysIterator = userData.keys();
        while(keysIterator.hasNext()){
            try {
                String keys = keysIterator.next().toString();
                TableRow nextRow = new TableRow(getActivity());
                nextRow.setLayoutParams(parameters);
                TextView nextText = new TextView(getActivity());
                nextText.setText(keys + ":\t\t" + userData.get(keys));
                nextRow.addView(nextText);
                Log.i("jsf", "buildScreen: Hash k,v -> " + keys + userData.get(keys));
                if (userTable == null)
                    Log.i("null", "buildScreen: Usertable");
                if (nextRow == null)
                    Log.i("null", "buildScreen: nextRow");
                userTable.addView(nextRow, parameters);
            }
            catch(JSONException e){
                Log.i("jsf", "buildScreen: jsonexception " + e);
            }
        }
    }

    private void beginSearch(){
        Log.i("jsf", "beginSearch: pressed button");
        SearchListener searchListener = (SearchListener)getActivity();
        searchListener.launchSearch();
    }

    public interface SearchListener{
        void launchSearch();
    }

    private void viewRegistration(){
        ViewRegistrationListener registrationListener = (ViewRegistrationListener)getActivity();
        registrationListener.launchViewRegistration();
    }

    public interface ViewRegistrationListener{
        void launchViewRegistration();
    }

}
