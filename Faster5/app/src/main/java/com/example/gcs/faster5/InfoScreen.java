package com.example.gcs.faster5;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kien on 07/05/2016.
 */
public class InfoScreen extends AppCompatActivity {
    RelativeLayout hinhNen;
    Button doneButton;
    ImageView avatarfb;
    String idUserFB, fullNameFb;
    TextView userName;
    Intent idfB, moveMainScreen, logout;
    ImageButton logoutButton;
    public ConnectivityManager connectivityManager;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.info_screen);
        userName = (TextView) findViewById(R.id.userName);
        avatarfb = (ImageView) findViewById(R.id.avatarUser);

        hinhNen = (RelativeLayout) findViewById(R.id.BackGround);
        hinhNen.setBackgroundResource(R.drawable.bgaccount);
        doneButton = (Button) findViewById(R.id.buttonDone);
        doneButton.setOnClickListener(new PlayGame());

        logout = new Intent(InfoScreen.this, LoginScreen.class);

        if (checkInternetConnection(InfoScreen.this)) {
            GetUserInfo();
        } else {

            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Connection failed");
            alertDialog.setMessage("Unable to establish connection with the server");
            alertDialog.setCancelable(false);
            alertDialog.setButton("Try Again", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    LoginManager.getInstance().logOut();
                    startActivity(logout);
                    finish();
                }
            });
            alertDialog.show();
        }

        logoutButton = (ImageButton) findViewById(R.id.fblogout_button);
        logoutButton.setImageResource(R.drawable.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                startActivity(logout);
                finish();
            }
        });

    }

    public boolean checkInternetConnection(Context context) {
        connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isAvailable()
                && connectivityManager.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private void GetUserInfo() {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code
                        try {
                            idUserFB = object.getString("id");
                            idfB = new Intent(InfoScreen.this, MainScreen.class);
                            idfB.putExtra("IDFB", idUserFB);

                            if (idUserFB == null) {
                                avatarfb.setImageResource(R.drawable.avatar);
                            } else {
                                Glide.with(getApplicationContext()).load("https://graph.facebook.com/" + idUserFB + "/picture?width=100&height=100").into(avatarfb);
                            }

                            fullNameFb = object.getString("name");
                            userName.setText(fullNameFb);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,gender,name,birthday,picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();

    }

    public class PlayGame implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            moveMainScreen = new Intent(getApplicationContext(), MainScreen.class);
            moveMainScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(moveMainScreen);
            startActivity(idfB);
            finish();

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkInternetConnection(InfoScreen.this)) {
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Connection failed");
            alertDialog.setMessage("Unable to establish connection with the server");
            alertDialog.setCancelable(false);
            alertDialog.setButton("Try Again", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    LoginManager.getInstance().logOut();
                    startActivity(logout);
                    finish();
                }
            });
            alertDialog.show();
        }
    }

    public void onBackPressed() {
    }
}