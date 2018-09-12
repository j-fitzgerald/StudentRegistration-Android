package com.example.clyste.jfregistration;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;


public class RegisterUser extends Fragment {

    private Button login;
    private static final String JSON_FILENAME = "userJson";
    private final static String TAG = "jsf";
    private final static String ADD_STUDENT_URL = "https://bismarck.sdsu.edu/registration/addstudent";
    private final static String OK = "ok";
    private final static String ERROR = "error";
    private String successMessage;
    private View registerView;
    private JSONObject userJSON;

    public RegisterUser() {
        // Required empty public constructor
    }

    public static RegisterUser newInstance() {
        RegisterUser fragment = new RegisterUser();
        Log.i(TAG, "newInstance: homeConstructor");
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        registerView = inflater.inflate(R.layout.fragment_register_user, container, false);
        login = registerView.findViewById(R.id.loginButton);
        login.setOnClickListener(event->loginAttempt());
        successMessage = getResources().getString(R.string.successfulRegistration);
        return registerView;
    }

    private void loginAttempt() {
        Log.i(TAG, "loginAttempt: login pressed");
        userJSON = buildJSON();
        Iterator keyIterator = userJSON.keys();
        while (keyIterator.hasNext()){
            String key = keyIterator.next().toString();
            try {
                if (userJSON.get(key).toString().matches("")) {
                    Log.i(TAG, "loginAttempt: FAILURE");
                    Toast.makeText(getContext(), R.string.loginFailed, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            catch(JSONException e){
                Log.i(TAG, "loginAttempt: json exception" + e);
            }
        }
        registerWithServer();
    }

    private JSONObject buildJSON(){
        JSONObject userEntry = new JSONObject();
        EditText firstNameEntry = getView().findViewById(R.id.registerFirstName);
        EditText lastNameEntry = getView().findViewById(R.id.registerLastName);
        EditText redIDEntry = getView().findViewById(R.id.registerRedID);
        EditText emailEntry = getView().findViewById(R.id.registerEmail);
        EditText passwordEntry = getView().findViewById(R.id.registerPassword);
        try {
            userEntry.put("firstname", firstNameEntry.getText().toString());
            userEntry.put("lastname", lastNameEntry.getText().toString());
            userEntry.put("redid", redIDEntry.getText().toString());
            userEntry.put("password", passwordEntry.getText().toString());
            userEntry.put("email", emailEntry.getText().toString());
        }
        catch(JSONException e){
            Log.i(TAG, "buildJSON: json exception" + e);
        }
        return userEntry;
    }

    private void registerWithServer(){
        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>(){
            public void onResponse(JSONObject response){
                Log.i("jsf", "onResponse: getRegisterResponse\n\t" + response.toString());
                try {
                    if (response.getString(OK).equals(successMessage)) {
                        if (!saveData(userJSON)){
                            Log.i(TAG, "Failed to save user data");
                            return;
                        }
                        login();
                    }
                    else
                        Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                }
                catch (JSONException e){
                    Log.i("jsf", "onResponse: JSON Exception");
                    try {
                        Toast.makeText(getActivity(), response.getString(ERROR), Toast.LENGTH_SHORT).show();
                    }
                    catch (JSONException f){
                        Toast.makeText(getActivity(), f.toString(), Toast.LENGTH_SHORT).show();
                    }
                }

                Log.i(TAG, "onResponse: " + response);
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener(){
            public void onErrorResponse(VolleyError error){
                Log.i("jsf", "onErrorResponse: subject error "  + error.toString());
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        JsonObjectRequest getRequest = new JsonObjectRequest(ADD_STUDENT_URL, userJSON, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        queue.add(getRequest);
    }

    private void login(){
        RegisterListener listener = (RegisterListener)getActivity();
        listener.registered();
    }

    public interface RegisterListener{
        void registered();
    }

    private boolean saveData(JSONObject userJson){
        Log.i("Save", "saveData: dir Before save: " + getContext().getFilesDir());
        try {
            FileWriter userFile = new FileWriter(getContext().getFilesDir() + "/" + JSON_FILENAME);
            userFile.write(userJson.toString());
            userFile.close();
            Log.i(TAG, "saveData: save success\n" + getContext().getFilesDir() + JSON_FILENAME);
            return true;
        }
        catch(IOException e){
            Log.d(TAG, "saveData: IOException " + e);
        }
        catch(Exception e){
            Log.d(TAG, "saveData: Other Exceptions:" + e);
        }
        return false;
    }
}
