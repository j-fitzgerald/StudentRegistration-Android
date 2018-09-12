package com.example.clyste.jfregistration;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;

import static android.view.View.VISIBLE;

public class SearchResults extends Fragment implements AdapterView.OnItemClickListener{

    public SearchResults() {
        // Required empty public constructor
    }

    private final static String RESPONSE_KEY = "responseKey";
    private final static String CLASS_DETAIL_URL = "https://bismarck.sdsu.edu/registration/classdetails?classid=";
    private final static String REGISTER_URL = "https://bismarck.sdsu.edu/registration/registerclass";
    private final static String WAITLIST_URL = "https://bismarck.sdsu.edu/registration/waitlistclass";
    private final static String JSON_FILENAME = "userJson";
    private final static String ENROLLED = "enrolled";
    private final static String SEATS = "seats";
    private final static String COURSE_ID = "id";
    private final static String COURSE_ID_KEY = "courseid";
    private final static String PASSWORD = "password";
    private final static String RED_ID = "redid";
    private RequestClassDetails request;

    private JSONArray classesJsonObjects;
    private JSONObject userJson;
    private String responseString;
    private ListView responseList;
    private ArrayList<String> responseArrayList;
    private ArrayList<String> truncatedResultArrayList;
    private ArrayList fullDetail;
    private boolean viewFullDetail = false;
    private Button backButton;
    private Button addButton;
    private JSONObject displayedClass;
    private static final String FRAGMENT_NAME = "SearchResults";


    public static SearchResults newInstance(JSONArray response) {
        SearchResults fragment = new SearchResults();
        Bundle args = new Bundle();
        args.putString(RESPONSE_KEY, response.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            responseString = getArguments().getString(RESPONSE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View searchResultView = inflater.inflate(R.layout.fragment_search_results, container, false);
        responseList = searchResultView.findViewById(R.id.classListView);
        responseArrayList = new ArrayList<String>();
        truncatedResultArrayList = new ArrayList();
        classesJsonObjects = new JSONArray();
        ArrayList<String> loading = new ArrayList();
        loading.add(getActivity().getResources().getString(R.string.loading));
        updateAdapter(loading);
        parseResponse();
        getUserJSON();
        responseList.setOnItemClickListener(this);
        backButton = searchResultView.findViewById(R.id.backSearchResults);
        backButton.setOnClickListener(event->backToResult());
        addButton = searchResultView.findViewById(R.id.registerButton);
        addButton.setOnClickListener(event->registerCourse());
        return searchResultView;
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id){
        if (!viewFullDetail) {
            fullDetail = new ArrayList();
            try {
                displayedClass = (JSONObject)classesJsonObjects.get(position);
                Iterator<String> iter = displayedClass.keys();
                while (iter.hasNext()){
                    String key = iter.next();
                    fullDetail.add(key + ":" + displayedClass.get(key));
                }
            }
            catch(JSONException e){
                Log.i("jsf", "onItemClick: json exception " + e);
            }
            updateAdapter(fullDetail);
            viewFullDetail = true;
            backButton.setVisibility(VISIBLE);
            addButton.setVisibility(VISIBLE);
            try {
                if (Integer.parseInt(displayedClass.get(SEATS).toString()) <= Integer.parseInt(displayedClass.get(ENROLLED).toString()))
                    addButton.setText(getContext().getString(R.string.waitlistButton));
                else
                    addButton.setText(getContext().getString(R.string.registerButton));

            }
            catch(JSONException e){
                Log.d("jsf", "full class detail: json exception " +e);
            }
        }
    }

    private void parseResponse(){
        // extract individual IDs
        JSONArray ids = new JSONArray();
        String[] splitString = responseString.replace("[","").replace("]","").split(",");
        for (int i=0; i < splitString.length; i++) {
            // send a request for details
            if (splitString[i].isEmpty())
                continue;
            if (splitString[i].equals("\""))
                continue;
            Log.i("jsfSplit", "parseResponse: " + splitString[i]);
            ids.put(Integer.parseInt(splitString[i]));
        }
        //requestDetails(ids, 0);
        request = new RequestClassDetails(ids, CLASS_DETAIL_URL, getActivity(), FRAGMENT_NAME);
        request.beginRequest();
    }

    private void updateAdapter(ArrayList updatedList){
        Log.i("jsf", "updateAdapter: " + updatedList.toString());
        ArrayAdapter listViewAdapter = new ArrayAdapter(this.getActivity(),android.R.layout.simple_list_item_1,updatedList);
        responseList.setAdapter(listViewAdapter);
    }

    private void backToResult(){
        viewFullDetail = false;
        backButton.setVisibility(View.INVISIBLE);
        addButton.setVisibility(View.INVISIBLE);
        updateAdapter(truncatedResultArrayList);
    }

    private void registerCourse(){
        Log.i("jsf", "registerCourse: register\n" + fullDetail.toString());
        Response.Listener<JSONObject> successRegistration = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                String responseKey = response.keys().next();
                try {
                        Log.i("jsf", "onResponse: registerResponse " +response.get(responseKey).toString());
                        Toast.makeText(getContext(), response.get(responseKey).toString(), Toast.LENGTH_SHORT).show();
                        backToResult();
                }
                catch(JSONException e){
                        Log.i("jsf", "onResponse: JSONException " + e);
                    }
            }
        };
        Response.ErrorListener failureRegistration = new Response.ErrorListener(){
            public void onErrorResponse(VolleyError error){
                Log.i("jsf", "onErrorResponse: registration "  + error.toString());
            }
        };
        String registrationURL = getURL();
        Log.i("jsf", "generatedURL: " + registrationURL);
        if (registrationURL.isEmpty()){
            Log.i("jsf", "registerCourse: error with url" + registrationURL);
            return;
        }
        JSONObject postJSON = generatePostJSON();
        Log.i("jsf", "generatedJSON: " + postJSON.toString());
        JsonObjectRequest getRequest = new JsonObjectRequest(registrationURL, postJSON, successRegistration, failureRegistration);
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        queue.add(getRequest);
    }

    private JSONObject generatePostJSON(){
        JSONObject registerJSON = new JSONObject();
        try {
            registerJSON.put(RED_ID, userJson.get(RED_ID));
            registerJSON.put(PASSWORD, userJson.get(PASSWORD));
            registerJSON.put(COURSE_ID_KEY, displayedClass.get(COURSE_ID));
        }
        catch(JSONException e){
            Log.i("jsf", "generatePostJSON: json exception" + e);
        }
        return registerJSON;
    }

    private String getURL(){
        try {
            if (displayedClass.getInt(SEATS) == displayedClass.getInt(ENROLLED)){
                return WAITLIST_URL;
            }
            return REGISTER_URL;
        }
        catch(JSONException e){
            Log.i("jsf", "getURL: jsonExceotion:" + e);
        }
        return "";
    }

    private void getUserJSON(){
        try {
            BufferedReader jsonFile = new BufferedReader(new FileReader(getContext().getFilesDir() + "/" + JSON_FILENAME));
            userJson = new JSONObject(jsonFile.readLine());
            Log.i("jsf", "getUserJSON: \n" + userJson.toString());
        }
        catch (Exception e){
            Log.i("jsf", "getUserJSON: failed to read file");
        }
    }

    public void serverResponse() {
        truncatedResultArrayList = request.getTruncatedResult();
        classesJsonObjects = request.getArrayOfObjects();
        responseArrayList = request.getFullDetails();
        updateAdapter(truncatedResultArrayList);
    }
}
