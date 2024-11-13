package com.v2ray.ang.ui

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ActivitySubEditBinding
import com.v2ray.ang.dto.SubscriptionItem
import com.v2ray.ang.extension.toast
import com.v2ray.ang.handler.MmkvManager
import com.v2ray.ang.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl
import okhttp3.Response
import com.google.gson.Gson
import com.v2ray.ang.viewmodel.MainViewModel
import me.drakeet.support.toast.ToastCompat
import java.io.IOException
import kotlin.getValue
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.widget.CheckBox

class LoginActivity : BaseActivity() {
    val mainViewModel: MainViewModel by viewModels()

    data class ApiResponse(
        val error: Boolean,
        val message: String,
        val data: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (sharedPreferences.getBoolean("is_logged_in", false)) {
            navigateToMainActivity(null)
        } else {
            super.onCreate(savedInstanceState)
            supportActionBar?.hide()
            setContentView(R.layout.activity_auth)

            val usernameEditText = findViewById<EditText>(R.id.auth_username)
            val passwordEditText = findViewById<EditText>(R.id.auth_password)
            val loginButton = findViewById<Button>(R.id.auth_login_btn)
            val rememberMe = findViewById<CheckBox>(R.id.auth_remember_me)

            loginButton.setOnClickListener {
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()

                if (username.isNotEmpty() && password.isNotEmpty()) {
                    val client = OkHttpClient()
                    val url = HttpUrl.Builder()
                        .scheme("https")
                        .host("v2.pixelated.ir")
                        .addPathSegment("get-v2ray-subscription-link")
                        .addQueryParameter("username", username)
                        .addQueryParameter("password", password)
                        .build()
                    val request = Request.Builder()
                        .url(url)
                        .build()
                    try {
                        client.newCall(request).enqueue(object : okhttp3.Callback {
                            override fun onFailure(call: okhttp3.Call, e: IOException) {
                                runOnUiThread {
                                    toastLoginFailure()
                                }
                                e.printStackTrace()
                            }

                            override fun onResponse(call: okhttp3.Call, response: Response) {
                                if (response.isSuccessful) {
                                    val responseBody = response.body?.string()
                                    if (responseBody != null) {
                                        try {
                                            val apiResponse = Gson().fromJson(responseBody, ApiResponse::class.java)
                                            val error = apiResponse?.error ?: true
                                            val url = apiResponse?.data

                                            if (!error && url != null) {
                                                runOnUiThread {
                                                    if (rememberMe.isChecked) {
                                                        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                                                        val editor = sharedPreferences.edit()
                                                        editor.putBoolean("is_logged_in", true)
                                                        editor.apply()
                                                    }
                                                    navigateToMainActivity(url)
                                                }
                                            } else {
                                                runOnUiThread {
                                                    toastLoginFailure()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            runOnUiThread {
                                                toastLoginFailure()
                                            }
                                        }
                                    } else {
                                        runOnUiThread {
                                            toastLoginFailure()
                                        }
                                    }
                                } else {
                                    runOnUiThread {
                                        toastLoginFailure()
                                    }
                                }
                            }
                        })
                    }  catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            toastLoginFailure()
                        }
                    }
                } else {
                    Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMainActivity(url: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("customUrl", url)
        }
        startActivity(intent)
        finish()
    }

    private fun toastLoginFailure() {
        Toast.makeText(this, "Login Failed! Please check out your credentials.", Toast.LENGTH_SHORT).show()
    }
}