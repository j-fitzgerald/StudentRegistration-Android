package com.example.clyste.jfregistration;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;


public class Search extends Fragment implements AdapterView.OnItemSelectedListener{

    private static final String MAJOR_NAME = "title";
    private static final String SUBJECT_URL = "https://bismarck.sdsu.edu/registration/subjectlist";
    private static final String CLASS_LIST_IDS_URL = "https://bismarck.sdsu.edu/registration/classidslist";
    private static final String ID = "id";
    private static final String SUBJECT_IDS = "subjectids";
    private static final String LEVEL = "level";
    private static final String START_TIME = "starttime";
    private static final String END_TIME = "endtime";
    private static final int TOO_MANY_RESULTS = 50;
    private String subjectHeader;
    private String levelHeader;
    private String timeHeader;
    private String subjectPreface;
    public String noResultsToast;
    public String tooManyResultsToast;
    private int startTime = 700;
    private int endTime = 2000;
    private static final int SUBJECT_NUMBER = 0;
    private static final int LEVEL_NUMBER = 1;
    private static final int TIME_NUMBER = 2;
    private int selectedScreen = SUBJECT_NUMBER;

    private TextView headerText;
    private TextView displayLevel;
    private TextView displayBeforeTime;
    private TextView displayAfterTime;
    private View searchView;
    private Spinner subjectSpinner;

    private ArrayList<JSONObject> subjectArrayList;
    private ArrayList<String> myFilteredSubjects;

    private ArrayList<String> subjectList;
    private ArrayList<String> levelList;
    private ArrayList<String> timeList;
    private String[] levelListArray;

    private JSONArray subjectFilterJSON;
    private String endBeforeFilterString = "";
    private String startAfterFilterString = "";
    private String levelFilterString ="";

    private TableLayout subjectTable;
    private JSONArray classes;


    private Button clearFilter;
    private Button performSearchButton;
    private Button subjectButton;
    private Button levelButton;
    private Button timeButton;
    private RadioGroup timeGroup;
    private RadioButton beforeRadioButton;
    private RadioButton afterRadioButton;

    public Search() {
        // Required empty public constructor
    }

    public static Search newInstance() {
        Search fragment = new Search();
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
        searchView =  inflater.inflate(R.layout.fragment_search_filter, container, false);
        headerText = searchView.findViewById(R.id.textHeader);
        subjectHeader = getResources().getString(R.string.subjectFilterHeader);
        levelHeader = getResources().getString(R.string.levelFilterHeader);
        timeHeader = getResources().getString(R.string.timeFilterHeader);
        subjectPreface = getResources().getString(R.string.subjectPreface);
        noResultsToast = getActivity().getResources().getString(R.string.noResultsToast);
        tooManyResultsToast = getActivity().getResources().getString(R.string.tooManyResultsToast);
        displayLevel = searchView.findViewById(R.id.displayLevel);
        displayBeforeTime = searchView.findViewById(R.id.displayBefore);
        displayAfterTime = searchView.findViewById(R.id.displayAfter);

        timeGroup = searchView.findViewById(R.id.timeFilterRadioGroup);
        beforeRadioButton = searchView.findViewById(R.id.beforeRadio);
        afterRadioButton = searchView.findViewById(R.id.afterRadio);

        getSubjects();

        subjectList = new ArrayList();
        subjectArrayList = new ArrayList();
        levelList = new ArrayList();
        timeList = new ArrayList();
        myFilteredSubjects = new ArrayList();
        levelList.add(getResources().getString(R.string.levelFilter));
        timeList.add(getResources().getString(R.string.timeFilter));
        levelListArray = getResources().getStringArray(R.array.levels);
        levelList = new ArrayList<>(Arrays.asList(levelListArray));
        makeTime(startTime,endTime, timeList);


        subjectFilterJSON = new JSONArray();
        String subjectDefaultLine = getResources().getString(R.string.subjectFilter);
        subjectList.add(subjectDefaultLine);
        subjectSpinner = searchView.findViewById(R.id.subjectSpinner);
        subjectTable = searchView.findViewById(R.id.subjectTable);
        ArrayList loadingList = new ArrayList();
        loadingList.add(getActivity().getResources().getString(R.string.loading));
        fillSpinner(loadingList);

        clearFilter = searchView.findViewById(R.id.clearButton);
        clearFilter.setOnClickListener(event->clearSubjects());

        performSearchButton = searchView.findViewById(R.id.performSearch);
        performSearchButton.setOnClickListener(event->initiateSearch());

        subjectButton = searchView.findViewById(R.id.subjectButton);
        subjectButton.setOnClickListener(event->updateScreen(subjectHeader, subjectList, subjectButton, SUBJECT_NUMBER));

        levelButton = searchView.findViewById(R.id.levelButton);
        levelButton.setOnClickListener(event->updateScreen(levelHeader, levelList, levelButton, LEVEL_NUMBER));

        timeButton = searchView.findViewById(R.id.timeButton);
        timeButton.setOnClickListener(event->updateScreen(timeHeader, timeList, timeButton, TIME_NUMBER));

        return searchView;
    }


    private void fillSpinner(ArrayList<String> list){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, list);
        subjectSpinner.setAdapter(adapter);
        subjectSpinner.setOnItemSelectedListener(this);
    }

    private void getSubjects(){
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>(){
            public void onResponse(JSONArray response){
                Log.i("jsf", "onResponse: getSubjectResponse\n\t" + response.toString());
                classes = response;
                try {
                    for (int i = 0; i < classes.length(); i++) {
                        JSONObject next = (JSONObject)classes.get(i);
                        subjectList.add(next.getString(MAJOR_NAME));
                        subjectArrayList.add((JSONObject)classes.get(i));
                        Log.i("jsf", "onResponse: added " + next.getString(MAJOR_NAME));
                        if (headerText.getText() == subjectHeader)
                            fillSpinner(subjectList);
                    }
                }
                catch (JSONException e){
                    Log.i("jsf", "onResponse: JSON Exception");
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener(){
            public void onErrorResponse(VolleyError error){
                Log.i("jsf", "onErrorResponse: subject error "  + error.toString());
            }
        };
        JsonArrayRequest getRequest = new JsonArrayRequest(SUBJECT_URL, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        queue.add(getRequest);

    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
        if (position == 0) return;
        String item = parent.getItemAtPosition(position).toString();
        switch (selectedScreen){
            case SUBJECT_NUMBER:
                Log.i("jsf", "onItemSelected: SubjectSpinner");
                for (int i = 0; i < subjectArrayList.size(); i++){
                    try {
                        if (subjectArrayList.get(i).getString(MAJOR_NAME) == item) {
                            addToFilter(myFilteredSubjects, item, subjectArrayList.get(i).getString(ID), subjectPreface);
                            subjectFilterJSON.put(Integer.parseInt(subjectArrayList.get(i).getString(ID)));
                            Log.i("jsf", "onItemSelected: Added id" + subjectArrayList.get(i).getString(ID) +"\nSubject: " + subjectArrayList.get(i).getString(MAJOR_NAME));
                            break;
                        }
                    }
                    catch (JSONException e){
                        Log.i("jsf", "onItemSelected: jsonexception" +e);
                    }
                }
                break;
            case LEVEL_NUMBER:
                //addToFilter(myFilteredLevels, item, item, levelPreface);
                displayLevel.setText(item);
                //levelFilterJSON.put(item);
                levelFilterString = item;
                break;
            case TIME_NUMBER:
                if (beforeRadioButton.isChecked()) {
                    //addToFilter(myFilteredBeforeTime, item, item, beforePreface);
                    //endBeforeFilterString.put(item);
                    endBeforeFilterString = item;
                    displayBeforeTime.setText(item);
                }
                else {
                    //addToFilter(myFilteredAfterTime, item, item, afterPreface);
                    //startAfterFilterString.put(item);
                    startAfterFilterString = item;
                    displayAfterTime.setText(item);
                }
                break;
            default:
                break;
        }
        parent.setSelection(0);
    }

    private void addToFilter(ArrayList listToAdd, String itemToAdd, String filterListAdd, String preface){
        if (!listToAdd.contains(itemToAdd)) {
            listToAdd.add(filterListAdd);
            updateTable(itemToAdd, preface);
        }
    }


    public void onNothingSelected(AdapterView<?> parent){

    }

    private void updateTable(String item, String preface){
        TableRow.LayoutParams parameters = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow nextRow = new TableRow(getActivity());
        nextRow.setLayoutParams(parameters);
        TextView nextText = new TextView(getActivity());
        nextText.setText(preface + item);
        nextRow.addView(nextText);
        subjectTable.addView(nextRow);
    }

    private void clearSubjects(){
        subjectTable.removeAllViews();
        levelFilterString = "";
        endBeforeFilterString = "";
        startAfterFilterString = "";
        subjectFilterJSON = new JSONArray();
        displayLevel.setText("");
        displayAfterTime.setText("");
        displayBeforeTime.setText("");
    }

    private void initiateSearch(){
        JSONObject searchPattern = new JSONObject();
        if (subjectFilterJSON.length() == 0) return;
        try{
            searchPattern.put(SUBJECT_IDS, subjectFilterJSON);
        }
        catch(JSONException e){
            Log.i("jsf", "initiateSearchError: " + e);
        }
        try{

            searchPattern.put(LEVEL, levelFilterString);
        }
        catch(JSONException e){
            Log.i("jsf", "initiateSearchErrorError: " + e);
        }
        try{
            searchPattern.put(START_TIME, startAfterFilterString);
        }
        catch(JSONException e){
            Log.i("jsf", "initiateSearchError: " + e);
        }
        try{
            searchPattern.put(END_TIME, endBeforeFilterString);
        }
        catch(JSONException e){
            Log.i("jsf", "initiateSearchError: " + e);
        }
        Log.i("jsf", "initiateSearch: JSON: " + searchPattern.toString());
        sendSearchRequest(searchPattern);
    }

    private void sendSearchRequest(JSONObject data){
        Response.Listener<JSONArray> successArray = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                SearchListener listener = (SearchListener) getActivity();
                Log.i("jsf", "onResponse: getSubjectResponse\n\t" + response.toString());
                Log.i("jsf", "onResponse: length:" + response.length());
                if(response.length() !=0){
                    if (response.length() < TOO_MANY_RESULTS)
                        listener.openSearchResult(response);
                    else
                        Toast.makeText(getContext(), tooManyResultsToast, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), noResultsToast, Toast.LENGTH_SHORT).show();
                }
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener(){
            public void onErrorResponse(VolleyError error){
                Log.i("jsf", "onErrorResponse: subject error "  + error.toString());
            }
        };
        String customClassListIdsUrl = makeCustomURL();
        JsonArrayRequest getRequest = new JsonArrayRequest(customClassListIdsUrl, successArray, failure);
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        queue.add(getRequest);
    }



    private String makeCustomURL(){
        String customURL = CLASS_LIST_IDS_URL;
        customURL += "?subjectid";
        try {
            for (int i = 0; i < subjectFilterJSON.length(); i++) {
                if (i!=0)
                    customURL += "&";
                customURL += "=" + subjectFilterJSON.get(i).toString();
            }
        }
        catch(JSONException e){
            Log.i("jsf", "makeCustomURLError: " +e);
        }
        if (!levelFilterString.isEmpty()){
            customURL += "&level=" + levelFilterString;
        }
        if (!endBeforeFilterString.isEmpty()) {
            customURL += "&endtime=" + endBeforeFilterString;
        }
        if (!startAfterFilterString.isEmpty()){
            customURL += "&starttime=" + startAfterFilterString;
        }
        Log.i("cust_url", "makeCustomURL: " + customURL);
        return customURL;

    }




    private void updateScreen(String subjectHeader, ArrayList<String> fill, Button thisButton, int selectNumber){
        if (subjectHeader == timeHeader)
            timeGroup.setVisibility(View.VISIBLE);
        else
            timeGroup.setVisibility(View.INVISIBLE);
        selectedScreen = selectNumber;
        headerText.setText(subjectHeader);
        fillSpinner(fill);
        subjectButton.setBackgroundColor(Color.LTGRAY);
        levelButton.setBackgroundColor(Color.LTGRAY);
        timeButton.setBackgroundColor(Color.LTGRAY);
        thisButton.setBackgroundColor(Color.BLUE);
    }

    private void makeTime(int start, int stop, ArrayList<String> list){
        for (int i=start; i <= stop; i=i+100){
            list.add(String.valueOf(i));
            list.add(String.valueOf(i+30));
        }
    }

    public interface SearchListener{
        void openSearchResult(JSONArray searchResults);
    }

}
