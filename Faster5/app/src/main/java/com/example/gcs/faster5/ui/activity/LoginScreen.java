package com.example.gcs.faster5.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gcs.faster5.R;
import com.example.gcs.faster5.network.ConnectivityChangeReceiver;
import com.example.gcs.faster5.network.SocketIO;
import com.example.gcs.faster5.util.JSONParser;
import com.example.gcs.faster5.util.NetworkUtils;
import com.example.gcs.faster5.util.PrefUtils;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

/**
 * Created by Kien on 07/05/2016.
 */
public class LoginScreen extends AppCompatActivity {

    private static String url = "http://209.58.180.196/json/"; //URL to get JSON Array
    private static final String TAG_CITY = "city";
    private LoginButton mLoginButtonFb;
    private CallbackManager mCallbackManager;
    final Context context = this;
    AccessTokenTracker mAccessTokenTracker;
    RelativeLayout mRelativeLayoutBg;
    EditText mEditText;
    String mStringUserName;
    Button mImageButtonPlay;
    TextView mTextViewCity;
    Typeface font;
    public static String city;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getSupportActionBar().hide();

        checkLogin();
        setContentView(R.layout.login_screen);

        SocketIO socket = (SocketIO) getApplication();
        mSocket = socket.getSocket();
        mSocket.connect();
        attemptSend();
        mSocket.on("ping", sendPong);

        strictMode();
        findViewbyId();
        editTexConfig();
        parserJSON();
        loginFB();
        loginManual();

        if (city == null) {
            mTextViewCity.setText("VIETNAM" );
        } else {
            mTextViewCity.setText(city);
        }

    }

    private void attemptSend() {
        if (!mSocket.connected()) {
            Log.e("Connecting...", "Connecting" );

        } else {
            Log.e("Connected...", "Connected" );
        }
        String ping = "3";
        mSocket.emit("ping", ping);
    }

    private Emitter.Listener sendPong = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String ping;
                    try {
                        ping = data.getString("ping" );
                    } catch (JSONException e) {
                        return;
                    }
                    if (ping.equals("4" )) {
                        Log.e("Connected...", "Connected" );
                    }
                    Log.e("SOCKETPING", "RECEIVED PING! " );
                }
            });
        }
    };

    public void findViewbyId() {
        mTextViewCity = (TextView) findViewById(R.id.textview_city_login);
        mRelativeLayoutBg = (RelativeLayout) findViewById(R.id.background);
        mEditText = (EditText) findViewById(R.id.text_edit);
        mImageButtonPlay = (Button) findViewById(R.id.button_login);
        font = Typeface.createFromAsset(getAssets(),
                "fonts/roboto.ttf" );
    }

    public void editTexConfig() {
        mEditText.setTypeface(font);
        mEditText.setHint("Choose username" );
        mEditText.setFocusableInTouchMode(false);
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEditText.setHint("" );
                mEditText.requestFocusFromTouch();
                mEditText.setFocusableInTouchMode(true);
                InputMethodManager keyBoard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                keyBoard.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
                return false;
            }
        });
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mEditText.clearFocus();
                    mEditText.setHint("Choose username" );
                    mEditText.setFocusableInTouchMode(false);
                }
            }
        });

        mRelativeLayoutBg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mRelativeLayoutBg.setFocusable(true);
                mEditText.clearFocus();
                InputMethodManager keyBoard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                keyBoard.hideSoftInputFromWindow(mRelativeLayoutBg.getWindowToken(), 0);
                return false;
            }
        });
    }

    public void loginFB() {
        mCallbackManager = CallbackManager.Factory.create();

        mLoginButtonFb = (LoginButton) findViewById(R.id.button_fb);
        mLoginButtonFb.setBackgroundResource(R.drawable.fbbutton);
        mLoginButtonFb.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        mLoginButtonFb.setReadPermissions("public_profile" );
        mLoginButtonFb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (NetworkUtils.checkInternetConnection(LoginScreen.this)) {
                    mAccessTokenTracker = new
                            AccessTokenTracker() {
                                @Override
                                protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                                    AccessToken mAccessToken = newToken;
                                }
                            };
                    FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {
                                @Override
                                public void onInitialized() {
                                    AccessToken mAccessToken = AccessToken.getCurrentAccessToken();
                                    if (mAccessToken != null) {
                                        parserJSON();
                                        Intent intent = new Intent(LoginScreen.this, InfoScreen.class);
                                        overridePendingTransition(R.animator.right_in, R.animator.left_out);
                                        startActivity(intent);
                                        finish();
                                        return;
                                    } else {
                                        mLoginButtonFb.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                                                    @Override
                                                    public void onSuccess(LoginResult loginResult) {
                                                        mLoginButtonFb.setVisibility(View.INVISIBLE);
                                                        AccessToken mAccessToken = loginResult.getAccessToken();
                                                        PrefUtils.getInstance(LoginScreen.this).set(PrefUtils.KEY_ACCESS_TOKEN, mAccessToken.getToken());
                                                        Intent intent = new Intent(LoginScreen.this, InfoScreen.class);
                                                        startActivity(intent);
                                                        overridePendingTransition(R.animator.right_in, R.animator.left_out);
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onCancel() {
                                                    }

                                                    @Override
                                                    public void onError(FacebookException exception) {
                                                    }
                                                }
                                        );
                                        return;
                                    }

                                }
                            }
                    );

                    mAccessTokenTracker.startTracking();
                } else {
                    NetworkUtils.movePopupConnection(LoginScreen.this);
                }
                return false;
            }
        });
    }

    public void loginManual() {

        mImageButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.checkInternetConnection(LoginScreen.this)) {
                    if (PrefUtils.getInstance(LoginScreen.this).get(PrefUtils.KEY_LOGGED_IN, false)) {
                        parserJSON();
                        Intent intent = new Intent(LoginScreen.this, InfoScreen.class);
                        startActivity(intent);
                        finish();
                        return;
                    } else {
                        mStringUserName = mEditText.getText().toString();
                        if (mStringUserName.length() <= 3) {
                            AlertDialog alertDialogLogin = new AlertDialog.Builder(context).create();
                            alertDialogLogin.setMessage("Username incorrect. Username must be at least 4 characters!" );
                            alertDialogLogin.setCancelable(false);
                            alertDialogLogin.setButton("Try Again", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialogLogin.show();
                        } else {
                            PrefUtils.getInstance(LoginScreen.this).set(PrefUtils.KEY_NAME, mStringUserName);
                            PrefUtils.getInstance(LoginScreen.this).set(PrefUtils.KEY_MONEY, 0);
                            Intent intent = new Intent(LoginScreen.this, InfoScreen.class);
                            startActivity(intent);
                            finish();

                        }
                    }
                } else {
                    NetworkUtils.movePopupConnection(LoginScreen.this);
                }
            }

        });
    }

    public void checkLogin() {
        FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {
                    @Override
                    public void onInitialized() {
                        AccessToken mAccessToken = AccessToken.getCurrentAccessToken();
                        if (mAccessToken != null && NetworkUtils.checkInternetConnection(LoginScreen.this)) {
                            parserJSON();
                            Intent intent = new Intent(LoginScreen.this, InfoScreen.class);
                            overridePendingTransition(R.animator.right_in, R.animator.left_out);
                            startActivity(intent);
                            finish();
                            return;
                        }

                    }
                }
        );
        if (PrefUtils.getInstance(this).get(PrefUtils.KEY_LOGGED_IN, false) && NetworkUtils.checkInternetConnection(LoginScreen.this)) {
            parserJSON();
            Intent intent = new Intent(LoginScreen.this, InfoScreen.class);
            startActivity(intent);
            finish();
            return;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void parserJSON() {
        if (NetworkUtils.checkInternetConnection(LoginScreen.this)) {
            JSONParser jParser = new JSONParser();
            JSONObject json = jParser.getJSONFromUrl(url);
            try {
                city = json.getString(TAG_CITY);
                Log.e("CITY", " " + city);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void strictMode() {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
    }

}