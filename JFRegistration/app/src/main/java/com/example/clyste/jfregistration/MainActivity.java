package com.example.clyste.jfregistration;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends FragmentActivity implements RegisterUser.RegisterListener, HomePage.ViewRegistrationListener, HomePage.SearchListener, Search.SearchListener, RequestClassDetails.RequestClassDetailsListener{

    private static final String TAG = "jsf";
    private static final String JSON_FILENAME = "userJson";
    private static final String PASSWORD = "password";
    private static final String RED_ID = "redid";
    private static final String SEARCH_RESULTS = "SearchResults";
    private static final String REGISTERED_REQUEST_NAME = "myCoursesRegistered";
    private static final String WAITLISTED_REQUEST_NAME = "myCoursesWaitlisted";

    private SearchResults resultsFragment;
    private ViewMyCourses myCoursesFragment;

    private FragmentManager fragmentManager;
    private JSONObject userData;


    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    registered();
                    return true;
                case R.id.navigation_search:
                    launchSearchFragment();
                    return true;
                case R.id.navigation_myCourses:
                    launchViewRegistrationFragment();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        try {
            // Check if user has login
            File userFile = new File(this.getFilesDir(), JSON_FILENAME);
            Log.i(TAG, "onCreate:"+ this.getFilesDir());
            if (userFile.exists()) {
                // if yes --> login and go to home
                userData = readUserData();
                HomePage homeFragment = HomePage.newInstance(userData);
                Log.i(TAG, "onCreate: UserDataExists - load home fragment");
                //homeFragment.setArguments(getIntent().getExtras());
                fragmentManager.beginTransaction().add(R.id.fragmentContainer, homeFragment).commit();

            }
            else {
                // if no --> go to login creation
                RegisterUser loginFragment = RegisterUser.newInstance();
                Log.i(TAG, "onCreate: UserDataEmpty - load Login\n"+ getFilesDir() + JSON_FILENAME);
                fragmentManager.beginTransaction().add(R.id.fragmentContainer, loginFragment).commit();
            }
        }
        catch(Exception e){
            Log.i(TAG, "onCreate: Exception " + e );
        }
        catch(Throwable t){
            Log.i(TAG, "onCreate: " + Log.getStackTraceString(t));
        }
    }

    public void registered(){
        userData = readUserData();
        HomePage homeFragment = HomePage.newInstance(userData);
        Log.i(TAG, "onCreate: UserDataExists - load home fragment");
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, homeFragment).commit();
    }

    private JSONObject readUserData(){
        try{
            BufferedReader jsonFile = new BufferedReader(new FileReader(getFilesDir() + "/" + JSON_FILENAME));
            userData = new JSONObject(jsonFile.readLine());
            Log.i("jsf", "getUserJSON: \n" + userData.toString());
        }
        catch (FileNotFoundException e){
            Log.i("jsf", "readUserData: File Not Found: "  + JSON_FILENAME);
        }
        catch (IOException e){
            Log.i("jsf", "readUserData: IO: " + e);
        }
        catch (Exception e){
            Log.i("jsf", "readUserData: other exception " + e);
        }
        return userData;
    }


    private void launchSearchFragment(){
        Search searchFragment = Search.newInstance();
        Log.i(TAG, "launch search fragment");
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, searchFragment).commit();
    }

    private void launchViewRegistrationFragment(){

        Log.i(TAG, "launch view Registration fragment");
        try {
            myCoursesFragment = ViewMyCourses.newInstance(userData.get(RED_ID).toString(), userData.get(PASSWORD).toString());
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, myCoursesFragment).commit();
        }
        catch(JSONException e){
            Log.i(TAG, "launchViewRegistrationFragment: json exception " + e);
        }

    }

    public void launchSearch(){
        Log.i(TAG, "launchSearch: called");
        launchSearchFragment();
    }

    public void launchViewRegistration(){
        launchViewRegistrationFragment();
    }

    public void openSearchResult(JSONArray response){
        resultsFragment = SearchResults.newInstance(response);
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, resultsFragment).commit();
    }

    @Override
    public void finishedGettingClasses(String callingFragmentName) {
        if (callingFragmentName == SEARCH_RESULTS)
            resultsFragment.serverResponse();
        else if (callingFragmentName == REGISTERED_REQUEST_NAME)
            myCoursesFragment.registerResponse();
        else if (callingFragmentName == WAITLISTED_REQUEST_NAME)
            myCoursesFragment.waitlistResponse();

    }
}
