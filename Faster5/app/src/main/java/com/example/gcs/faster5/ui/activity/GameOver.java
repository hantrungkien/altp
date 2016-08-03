package com.example.gcs.faster5.ui.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.gcs.faster5.R;

import com.example.gcs.faster5.util.PrefUtils;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;

/**
 * Created by Kien on 07/14/2016.
 */
public class GameOver extends AppCompatActivity {

    public static final String EXTRA_SCORE = "score";

    ImageView mImageViewUserAvatar1, mImageViewUserAvatar2;
    TextView mTextViewNameUser1, mTextViewNameUser2, mResult, mUserScore1, mUserScore2;
    RelativeLayout mRelativeLayoutBg;
    ImageButton mImageButtonOk;
    Integer mScore1 = 0, mScore2 = 0, mMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.game_over);

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/roboto.ttf");
        mRelativeLayoutBg = (RelativeLayout) findViewById(R.id.background);
        mRelativeLayoutBg.setBackgroundResource(R.drawable.background);

        mImageViewUserAvatar1 = (ImageView) findViewById(R.id.image_useravatar1);

        mTextViewNameUser1 = (TextView) findViewById(R.id.text_username1);
        mTextViewNameUser2 = (TextView) findViewById(R.id.text_username2);
        mTextViewNameUser1.setTypeface(font);
        mTextViewNameUser2.setTypeface(font);

        mResult = (TextView) findViewById(R.id.text_result);
        mResult.setTypeface(font);

        mUserScore1 = (TextView) findViewById(R.id.text_userscore1);
        mUserScore1.setTypeface(font);
        mUserScore2 = (TextView) findViewById(R.id.text_userscore2);
        mUserScore2.setTypeface(font);

        FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {
            @Override
            public void onInitialized() {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken != null) {
                    mTextViewNameUser1.setText(InfoScreen.sFullNameFb);
                    Glide.with(getApplicationContext())
                            .load("https://graph.facebook.com/" + InfoScreen.sUserFbId + "/picture?width=500&height=500").into(mImageViewUserAvatar1);
                } else {
                    mTextViewNameUser1.setText(InfoScreen.sFullNameFb);
                    mImageViewUserAvatar1.setImageResource(R.drawable.avatar);
                }
            }
        });
        SearchOpponent.questions.clear();
        Score();
        buttonOkPressed();
    }

    public void Score() {
        Bundle extrasName = getIntent().getExtras();
        if (extrasName != null) {
            mScore1 = extrasName.getInt(EXTRA_SCORE);
            if (mScore1 > mScore2) {
                mResult.setText("YOU WIN");
            } else if (mScore1 < mScore2) {
                mResult.setText("YOU LOSE");
            } else if (mScore1 == mScore2) {
                mResult.setText("DRAW");
            }
            mUserScore1.setText(Integer.toString(mScore1));
            mUserScore2.setText(Integer.toString(mScore2));

        }
    }

    public void buttonOkPressed() {
        mImageButtonOk = (ImageButton) findViewById(R.id.button_ok);
        mImageButtonOk.setImageResource(R.drawable.okbutton);
        mImageButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InfoScreen.class);
                mMoney = PrefUtils.getInstance(GameOver.this).get(PrefUtils.KEY_MONEY, 0);
                mMoney = mMoney + mScore1;
                PrefUtils.getInstance(GameOver.this).set(PrefUtils.KEY_MONEY, mMoney);
                startActivity(intent);
                overridePendingTransition(R.animator.in_from_left, R.animator.out_to_right);
                finish();
            }
        });
    }

    public void onBackPressed() {
    }
}