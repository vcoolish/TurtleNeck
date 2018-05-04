package com.example.vcoolish.turtleneck

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import android.content.Intent
import com.facebook.*
import com.facebook.appevents.AppEventsLogger


class SignActivity : AppCompatActivity() {
    lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        callbackManager = CallbackManager.Factory.create()
        val loginButton = findViewById(R.id.login_button) as LoginButton
        loginButton.setReadPermissions("email")

        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken == null
        val isExpired = accessToken?.isExpired

        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Toast.makeText(applicationContext,"Success", Toast.LENGTH_LONG).show()
                startActivity(Intent(applicationContext, AlrmActivity::class.java))}

            override fun onCancel() {
                Toast.makeText(applicationContext,"Cancel", Toast.LENGTH_LONG).show()            }

            override fun onError(exception: FacebookException) {
                Toast.makeText(applicationContext,"Error", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
