/*
 * (C) Copyright 2016 VTT (http://www.vtt.fi)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.elex.chatservice.view.kurento;

import com.elex.chatservice.R;
import com.elex.chatservice.model.kurento.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Login Activity for the first time the app is opened, or when a user clicks the sign out button.
 * Saves the username in SharedPreferences.
 */
public class LoginActivity extends  Activity {
    private String TAG = "LoginActivity";
    private EditText mUsername, mRoomname;
    private Context context;
    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mUsername = (EditText) findViewById(R.id.username);
        mRoomname = (EditText) findViewById(R.id.roomname);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    };

    /**
     * Takes the username from the EditText, check its validity and saves it if valid.
     *   Then, redirects to the MainActivity.
     * @param view Button clicked to trigger call to joinChat
     */
    public void joinRoom(View view){
        String username = mUsername.getText().toString();
        String roomname = mRoomname.getText().toString();
        if (!validUsername(username) || !validRoomname(roomname))
            return;

        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(Constants.USER_NAME, username);
        edit.putString(Constants.ROOM_NAME, roomname);
        edit.apply();

        Intent intent = new Intent(context, MutiPeerVideoActivity.class);
        startActivity(intent);
    }

    public void showToast(String string) {
        try {
            Toast toast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
            toast.show();
        }
        catch (Exception e){e.printStackTrace();}
    }

    /**
     * Optional function to specify what a username in your chat app can look like.
     * @param username The name entered by a user.
     * @return is username valid
     */
    private boolean validUsername(String username) {
        if (username.length() == 0) {
            mUsername.setError("Username cannot be empty.");
            return false;
        }
        if (username.length() > 16) {
            mUsername.setError("Username too long.");
            return false;
        }
        return true;
    }

    /**
     * Optional function to specify what a username in your chat app can look like.
     * @param roomname The name entered by a user.
     * @return is username valid
     */
    private boolean validRoomname(String roomname) {
        if (roomname.length() == 0) {
            mRoomname.setError("Roomname cannot be empty.");
            return false;
        }
        if (roomname.length() > 16) {
            mRoomname.setError("Roomname too long.");
            return false;
        }
        return true;
    }


}
