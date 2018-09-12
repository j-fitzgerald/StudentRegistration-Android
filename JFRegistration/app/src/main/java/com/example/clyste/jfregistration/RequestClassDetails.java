package com.example.clyste.jfregistration;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Clyste on 4/6/2018.
 */

public class RequestClassDetails {
    private JSONArray requestedList;
    private JSONArray classArrayOfObjects;
    private ArrayList responseArrayList;
    private ArrayList truncatedResultArrayList;
    private String getURL;
    private Activity activity;
    private RequestClassDetailsListener listener;
    private String callingFragmentName;
    private Fragment callingFragment;
    private Fragment frag;

    public RequestClassDetails(JSONArray list, String url, Activity thisActivity, String name){
        requestedList = list;
        getURL = url;
        activity = thisActivity;
        responseArrayList = new ArrayList();
        truncatedResultArrayList = new ArrayList();
        classArrayOfObjects = new JSONArray();
        listener = (RequestClassDetailsListener)activity;
        callingFragmentName = name;
    }


    public void beginRequest(){
        requestDetails(0);
    }


    private void requestDetails(int index){
        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
                Log.i("jsfA", "onResponse: getSubjectResponse\n\t" + response.toString());
                updateResponse(response);
                if (index + 1< requestedList.length())
                    requestDetails(index+1);
                else {
                    listener.finishedGettingClasses(callingFragmentName);
                }
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener(){
            public void onErrorResponse(VolleyError error){
                Log.i("jsf", "onErrorResponse: id error "  + error.toString());
            }
        };
        try {
            JsonObjectRequest getRequest = new JsonObjectRequest(getURL + requestedList .get(index), null, success, failure);
            RequestQueue queue = Volley.newRequestQueue(activity);
            queue.add(getRequest);
        }
        catch(JSONException e){
            Log.i("jsf", "requestDetails: " +e);
        }
    }

    private void updateResponse(JSONObject response){
        responseArrayList.add(response.toString());
        classArrayOfObjects.put(response);
        truncate(response);
    }

    private void truncate(JSONObject response){
        Log.i("jsf", "truncate response: " + response);
        try {
            // get course #
            String courseNumber = response.getString("course#");
            // get department
            String department = response.getString("department");
            // get title
            String title = response.getString("title");
            Log.i("jsf", "truncate title: " + title);
            truncatedResultArrayList.add(courseNumber + "\n" + department + "\n" + title);
        }
        catch (JSONException e){
            Log.d("jsf", "truncate: " + e);
        }
        catch(Exception e){
            Log.i("jsf", "truncate: other exception" + e);
        }
    }

    public interface RequestClassDetailsListener{
        public void finishedGettingClasses(String callingFragmentName);
    }

    public JSONArray getArrayOfObjects(){
        return classArrayOfObjects;
    }

    public ArrayList getFullDetails(){
        return responseArrayList;
    }

    public ArrayList getTruncatedResult(){
        return truncatedResultArrayList;
    }

    public String getName(){
        return callingFragmentName;
    }


}
