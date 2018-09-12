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

import java.io.File;
import java.util.ArrayList;


public class ViewMyCourses extends Fragment implements AdapterView.OnItemClickListener{

    private static final String MY_COURSES_POST = "https://bismarck.sdsu.edu/registration/studentclasses";
    private static final String UNREGISTER_POST = "https://bismarck.sdsu.edu/registration/unregisterclass";
    private static final String UNWAITLIST_POST = "https://bismarck.sdsu.edu/registration/unwaitlistclass";
    private static final String RESET_GET = "https://bismarck.sdsu.edu/registration/resetstudent?";
    private final static String JSON_FILENAME = "userJson";
    private static final String RED_ID = "redid";
    private static final String PASSWORD = "password";
    private static final String COURSE_ID = "courseid";
    private static final String TAG = "jsf";
    private static final String REGISTERED = "classes";
    private static final String WAITLISTED = "waitlist";
    private final static String CLASS_DETAIL_URL = "https://bismarck.sdsu.edu/registration/classdetails?classid=";

    private JSONObject myClasses;
    private JSONArray registeredClasses;
    private JSONArray waitlistedClasses;
    private ArrayList registeredArray;
    private ArrayList waitlistedArray;
    private ArrayList loadingArray;
    private ArrayList noneArray;
    private String userRedId;
    private String userPassword;
    private String loading;
    private String none;
    private Button resetButton;
    private Button deleteUserButton;

    private RequestClassDetails registerRequest;
    private RequestClassDetails waitlistRequest;
    private static final String REGISTERED_REQUEST_NAME = "myCoursesRegistered";
    private static final String WAITLISTED_REQUEST_NAME = "myCoursesWaitlisted";


    private ListView registeredList;
    private ListView waitlistedList;

    public ViewMyCourses() {
        // Required empty public constructor
    }

    public static ViewMyCourses newInstance(String redid, String password) {
        ViewMyCourses fragment = new ViewMyCourses();
        Bundle args = new Bundle();
        args.putString(RED_ID, redid);
        args.putString(PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userRedId = getArguments().getString(RED_ID);
            userPassword = getArguments().getString(PASSWORD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myCoursesView = inflater.inflate(R.layout.fragment_view_my_courses, container, false);
        loading = getResources().getString(R.string.loading);
        none = getResources().getString(R.string.none);
        registeredList = myCoursesView.findViewById(R.id.registeredList);
        waitlistedList = myCoursesView.findViewById(R.id.waitlistList);
        registeredList.setOnItemClickListener(this);
        waitlistedList.setOnItemClickListener(this);
        registeredArray = new ArrayList();
        waitlistedArray = new ArrayList();
        noneArray = new ArrayList();
        loadingArray = new ArrayList();
        loadingArray.add(loading);
        noneArray.add(none);
        clearAndUpdate();
        resetButton = myCoursesView.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(event->resetUser());
        deleteUserButton = myCoursesView.findViewById(R.id.deleteUser);
        deleteUserButton.setOnClickListener(event->deleteFiles());
        requestMyClasses();
        return myCoursesView;
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id){
        JSONObject dropJSON = generateJSON();
        if (parent == registeredList){
            try {
                dropJSON.put(COURSE_ID, registeredClasses.get(position));
                Log.i(TAG, "onItemClick: unregister" +registeredClasses.get(position));
                requestDrop(dropJSON, UNREGISTER_POST);
            }
            catch(JSONException e){
                Log.i(TAG, "onItemClick: jsonException:" + e);
            }
        }
        else if (parent==waitlistedList){
            try {
                dropJSON.put(COURSE_ID, waitlistedClasses.get(position));
                Log.i(TAG, "onItemClick: unwait" +waitlistedClasses.get(position));
                requestDrop(dropJSON, UNWAITLIST_POST);
            }
            catch(JSONException e){
                Log.i(TAG, "onItemClick: jsonException:" + e);
            }
        }
        else
            Log.i(TAG, "onItemClick: Failed to extract List");
    }

    private void requestDrop(JSONObject dropJSON, String dropURL) {
        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                Log.i("jsfA", "onResponse: dropCourse\n\t" + response.toString());
                String responseKey = response.keys().next();
                try{
                    Toast.makeText(getContext(), response.get(responseKey).toString(), Toast.LENGTH_SHORT).show();
                    clearAndUpdate();
                }
                catch(JSONException e){
                    Log.i(TAG, "onResponse: drop jsonexception:" + e);
                }
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.i("jsf", "onErrorResponse: myClasses error " + error.toString() + "\n\t" + dropJSON.toString());

            }
        };
        JsonObjectRequest getRequest = new JsonObjectRequest(dropURL, dropJSON, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        queue.add(getRequest);
    }

    private void requestMyClasses(){
        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                Log.i("jsfA", "onResponse: getMyClasses\n\t" + response.toString());
                myClasses = response;
                try {
                    JSONArray registeredResponse = (JSONArray)myClasses.get(REGISTERED);
                    if (registeredResponse.length() == 0)
                        updateAdapter(noneArray,registeredList);
                    Log.i(TAG, "onResponse: " + myClasses.get(REGISTERED).toString());
                }
                catch(JSONException e){
                    Log.i(TAG, "onResponse: json" + e);
                }
                try {
                    JSONArray waitlistedResponse = (JSONArray)myClasses.get(WAITLISTED);
                    if (waitlistedResponse.length() == 0)
                        updateAdapter(noneArray,waitlistedList);
                }
                catch(JSONException e){
                    Log.i(TAG, "onResponse: json" + e);
                }
                requestClassDetails();
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener(){
            public void onErrorResponse(VolleyError error){
                Log.i("jsf", "onErrorResponse: myClasses error "  + error.toString());
            }
        };
        JSONObject coursesPost = generateJSON();
        JsonObjectRequest getRequest = new JsonObjectRequest(MY_COURSES_POST, coursesPost, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        queue.add(getRequest);
    }

    public JSONObject generateJSON(){
        JSONObject coursesPost = new JSONObject();
        try {
            coursesPost.put(RED_ID, userRedId);
            coursesPost.put(PASSWORD, userPassword);
        }
        catch(JSONException e){
            Log.i(TAG, "generateJSON: " + e);
        }
        return coursesPost;
    }

    private void requestClassDetails(){
        try {
            registeredClasses = (JSONArray)myClasses.get(REGISTERED);
            registerRequest = new RequestClassDetails(registeredClasses, CLASS_DETAIL_URL, getActivity(), REGISTERED_REQUEST_NAME);
            registerRequest.beginRequest();
        }
        catch(JSONException e){
            Log.i(TAG, "requestClassDetails: json exception" + e);
            updateAdapter(noneArray, registeredList);
        }
        try{
            waitlistedClasses = (JSONArray)myClasses.get(WAITLISTED);
            waitlistRequest = new RequestClassDetails(waitlistedClasses, CLASS_DETAIL_URL, getActivity(), WAITLISTED_REQUEST_NAME);
            waitlistRequest.beginRequest();
        }
        catch(JSONException e){
            Log.i(TAG, "requestClassDetails: json exception" + e);
            updateAdapter(noneArray, waitlistedList);
        }

    }


    private void updateAdapter(ArrayList updatedList, ListView list){
        Log.i("jsf", "updateAdapter: " + updatedList.toString());
        ArrayAdapter listViewAdapter = new ArrayAdapter(this.getActivity(),android.R.layout.simple_list_item_1,updatedList);
        list.setAdapter(listViewAdapter);
    }

    private void resetUser(){
        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                String responseKey = response.keys().next();
                try {
                    Toast.makeText(getContext(), response.get(responseKey).toString(), Toast.LENGTH_SHORT).show();
                    clearAndUpdate();
                }
                catch(JSONException e){
                    Log.i(TAG, "onResponse: response jsonexception " + e);
                }
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener(){
            public void onErrorResponse(VolleyError error){
                Log.i("jsf", "onErrorResponse: resetUser error "  + error.toString());
                Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        JSONObject resetPost = new JSONObject();
        try {
            resetPost.put(RED_ID, userRedId);
            resetPost.put(PASSWORD, userPassword);
        }
        catch(JSONException e){
            Log.i(TAG, "resetUser: JSONException" + e);
        }
        String getURL = generateGetURL();
        Log.i(TAG, "resetUser: " + resetPost.toString() + "\n" + getURL);
        JsonObjectRequest getRequest = new JsonObjectRequest(getURL, null, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        queue.add(getRequest);
    }

    private String generateGetURL(){
        String getURL = RESET_GET;
        getURL += RED_ID + "=" + userRedId + "&" + PASSWORD + "=" + userPassword;
        return getURL;
    }

    private void deleteFiles(){
        File userFile = new File(this.getActivity().getFilesDir(), JSON_FILENAME);
        try{
            userFile.delete();
            Log.i(TAG, "deleteUserFile: File Deleted");
            Toast.makeText(getContext(), getResources().getString(R.string.deletedUser), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Log.i(TAG, "deleteUserFile: File not deleted");
        }

    }

    private void clearAndUpdate(){
        registeredArray.clear();
        waitlistedArray.clear();
        updateAdapter(loadingArray, registeredList);
        updateAdapter(loadingArray, waitlistedList);
        requestMyClasses();
    }

    public void registerResponse(){
        registeredArray = registerRequest.getTruncatedResult();
        if (registeredArray.size() == 0)
            updateAdapter(noneArray, registeredList);
        else
            updateAdapter(registeredArray, registeredList);
    }

    public void waitlistResponse(){

        waitlistedArray = waitlistRequest.getTruncatedResult();
        Log.i(TAG, "waitlistResponse: "+waitlistedArray.toString());
        if (waitlistedArray.size() == 0)
            updateAdapter(noneArray, waitlistedList);
        else
            updateAdapter(waitlistedArray, waitlistedList);
    }
}
