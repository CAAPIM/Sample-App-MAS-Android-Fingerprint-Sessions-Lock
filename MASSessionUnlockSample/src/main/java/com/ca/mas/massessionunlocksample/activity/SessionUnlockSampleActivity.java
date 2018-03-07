/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.massessionunlocksample.activity;

import android.app.Application;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASSessionUnlockCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.massessionunlocksample.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class SessionUnlockSampleActivity extends AppCompatActivity {
    private final String TAG = SessionUnlockSampleActivity.class.getSimpleName();
    private Context mContext;
    private RelativeLayout mContainer;
    private Button mLoginButton;
    private Button mInvokeButton;
    private TextInputLayout mUsernameInputLayout;
    private TextInputEditText mUsernameEditText;
    private TextInputLayout mPasswordInputLayout;
    private TextInputEditText mPasswordEditText;
    private Switch mLockSwitch;
    private TextView mProtectedContent;
    private int REQUEST_CODE = 0x1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prevents screenshotting of content in Recents
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_session_unlock_sample);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mContext = this;
        mContainer = findViewById(R.id.container);
        mUsernameEditText = findViewById(R.id.edit_text_username);
        mUsernameInputLayout = findViewById(R.id.text_input_layout_username);
        mPasswordEditText = findViewById(R.id.edit_text_password);
        mPasswordInputLayout = findViewById(R.id.text_input_layout_password);
        mLoginButton = findViewById(R.id.login_button);
        mLockSwitch = findViewById(R.id.checkbox_lock);
        mProtectedContent = findViewById(R.id.data_text_view);
        mInvokeButton = findViewById(R.id.invoke_button);

        mLoginButton.setOnClickListener(getLoginListener());
        mInvokeButton.setOnClickListener(getInvokeListener());
        mLockSwitch.setOnCheckedChangeListener(getLockListener(this));

        MAS.start(this, true);
    }

    private View.OnClickListener getLoginListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameEditText.getEditableText().toString();
                String password = mPasswordEditText.getEditableText().toString();

                MASUser.login(username, password.toCharArray(), getLoginCallback());
            }
        };
    }

    private View.OnClickListener getLogoutListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MASUser currentUser = MASUser.getCurrentUser();
                currentUser.logout(getLogoutCallback());
            }
        };
    }

    private View.OnClickListener getInvokeListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeApi();
            }
        };
    }

    private Switch.OnCheckedChangeListener getLockListener(final SessionUnlockSampleActivity activity) {
        return new Switch.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MASUser.getCurrentUser().lockSession(getLockCallback());
                } else {
                    MASUser.getCurrentUser().unlockSession(getUnlockCallback(activity));
                }
            }
        };
    }

    private void onLogin() {
        mInvokeButton.setVisibility(View.VISIBLE);
        mLockSwitch.setVisibility(View.VISIBLE);
        mLoginButton.setText(R.string.logout_button_text);
        mLoginButton.setOnClickListener(getLogoutListener());

        mUsernameInputLayout.setVisibility(View.GONE);
        mPasswordInputLayout.setVisibility(View.GONE);
    }

    private void onLogout() {
        mInvokeButton.setVisibility(View.GONE);
        mLockSwitch.setVisibility(View.GONE);
        mLoginButton.setText(R.string.login_button_text);
        mLoginButton.setOnClickListener(getLoginListener());
        mProtectedContent.setText(R.string.protected_info);

        mUsernameInputLayout.setVisibility(View.VISIBLE);
        mPasswordInputLayout.setVisibility(View.VISIBLE);
    }

    private MASCallback<MASUser> getLoginCallback() {
        return new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser user) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onLogin();
                    }
                });

                String textToSet = "Logged in as " + user.getDisplayName();
                mProtectedContent.setText(textToSet);
            }

            @Override
            public void onError(Throwable e) {
                Snackbar.make(mContainer, e.toString(), Snackbar.LENGTH_LONG).show();
            }
        };
    }

    private MASCallback<Void> getLogoutCallback() {
        return new MASCallback<Void>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(Void result) {
                onLogout();
            }

            @Override
            public void onError(Throwable e) {
                Snackbar.make(mContainer, e.toString(), Snackbar.LENGTH_LONG).show();
            }
        };
    }

    private MASCallback<Void> getLockCallback() {
        return new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Snackbar.make(mContainer, "Session Locked", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e) {
                Snackbar.make(mContainer, e.toString(), Snackbar.LENGTH_LONG).show();
            }
        };
    }

    private MASSessionUnlockCallback<Void> getUnlockCallback(final SessionUnlockSampleActivity activity) {
        return new MASSessionUnlockCallback<Void>() {
            @Override
            public void onUserAuthenticationRequired() {
                KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService(Application.KEYGUARD_SERVICE);
                Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("Session Unlock", "Provide PIN or Fingerprint To unlock Session");
                activity.startActivityForResult(intent, REQUEST_CODE);
            }

            @Override
            public void onSuccess(Void result) {
                Snackbar.make(mContainer, "Session Unlocked", Snackbar.LENGTH_LONG).show();

            }

            @Override
            public void onError(Throwable e) {
                Snackbar.make(mContainer, e.toString(), Snackbar.LENGTH_LONG).show();
            }
        };
    }

    private void invokeApi() {
        String path = "/protected/resource/products";
        Uri.Builder uriBuilder = new Uri.Builder().encodedPath(path);
        uriBuilder.appendQueryParameter("operation", "listProducts");
        uriBuilder.appendQueryParameter("pName2", "pValue2");

        MASRequest.MASRequestBuilder requestBuilder = new MASRequest.MASRequestBuilder(uriBuilder.build());
        requestBuilder.header("hName1", "hValue1");
        requestBuilder.header("hName2", "hValue2");
        MASRequest request = requestBuilder.get().build();

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    List<String> objects = parseProductListJson(result.getBody().getContent());
                    String objectString = "";
                    int size = objects.size();
                    for (int i = 0; i < size; i++) {
                        objectString += objects.get(i);
                        if (i != size - 1) {
                            objectString += "\n";
                        }
                    }

                    mProtectedContent.setText(objectString);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                MASUser.getCurrentUser().unlockSession(getUnlockCallback(this));
            }
        }
    }

    @Override
    protected void onPause() {
        if (mLockSwitch.isChecked()) {
            MASUser currentUser = MASUser.getCurrentUser();
            if (currentUser != null) {
                currentUser.lockSession(null);
            }
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MASUser currentUser = MASUser.getCurrentUser();
        if (currentUser != null && currentUser.isSessionLocked()) {
            launchLockActivity();
        } else
        if (currentUser != null && currentUser.isAuthenticated()) {
            onLogin();
        }
    }

    private void launchLockActivity() {
        Intent i = new Intent("MASUI.intent.action.SessionUnlock");
        startActivityForResult(i, REQUEST_CODE);
    }

    private static List<String> parseProductListJson(JSONObject json) throws JSONException {
        try {
            List<String> objects = new ArrayList<>();
            JSONArray items = json.getJSONArray("products");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = (JSONObject) items.get(i);
                Integer id = (Integer) item.get("id");
                String name = (String) item.get("name");
                String price = (String) item.get("price");
                objects.add(id + ": " + name + ", $" + price);
            }
            return objects;
        } catch (ClassCastException e) {
            throw (JSONException) new JSONException("Response JSON was not in the expected format").initCause(e);
        }
    }
}
