package com.pinneapple.dojocam_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.cometchat.pro.core.AppSettings;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.AppCheckToken;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pinneapple.dojocam_app.Login.LoginActivity;
import com.pinneapple.dojocam_app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_DojoCamApp);

        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        if( firebaseAuth.getCurrentUser() == null ){
            Intent loginActivity = new Intent(this, LoginActivity.class);
            startActivity(loginActivity);
            this.finish();
        }


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setSupportActionBar(binding.appBarMain.toolbar);
        initCometChat();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public String getRandomString(int length) {
        String randomChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String result = "";
        for ( int i = 0; i < length; i++ ) {
            result += randomChars.charAt((int)Math.floor(Math.random() * randomChars.length()));
        }
        return result;
    }

    private void CreateUserChat(){
        String authKey = "8d0188e55822c22f0d2f20cdcd8601c694b6780f"; // Replace with your App Auth Key
        User user = new User();
        FirebaseUser user_email = FirebaseAuth.getInstance().getCurrentUser();
        user.setUid(user_email.getEmail());
        user.setName(getRandomString((int)Math.floor(Math.random() * (100 - 1) + 1))); // Replace with the name of the user
        CometChat.createUser(user, authKey, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
            }

            @Override
            public void onError(CometChatException e) {
            }
        });
    }


    public void initCometChat(){
        String appID = "1985642356a8baff"; // Replace with your App ID
        String region = "us"; // Replace with your App Region ("eu" or "us")
        String authKey = "8d0188e55822c22f0d2f20cdcd8601c694b6780f"; //Replace with your Auth Key.
        FirebaseUser user_email = FirebaseAuth.getInstance().getCurrentUser();
        String UID = user_email.getEmail(); // Replace with the UID of the user to login
        AppSettings appSettings=new AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(region).build();

        CometChat.init(this, appID,appSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {
            }
            @Override
            public void onError(CometChatException e) {
            }
        });

        if (CometChat.getLoggedInUser() == null) {
            CometChat.login("superhero1", authKey, new CometChat.CallbackListener<User>() {
                @Override
                public void onSuccess(User user) {
                    Log.d("das", "Login Successful : " + user.toString());
                }

                @Override
                public void onError(CometChatException e) {
                    Log.d("error", "Login failed with exception: " + e.getMessage());
                }
            });
        }
    }
}