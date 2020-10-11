package com.example.migrationapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.migrationapp.R;
import com.example.migrationapp.dto.LoginDto;
import com.example.migrationapp.dto.User;
import com.example.migrationapp.rest_servers.LoginRegistrationServer;
import com.example.migrationapp.service.NetworkService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Base64;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static java.util.Arrays.asList;

public class LoginActivity extends AppCompatActivity {

    private EditText login;
    private EditText password;

    private Button loginButton;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.loginLoginEditText);
        password = findViewById(R.id.loginPasswordEditText);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(view -> loginAction());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loginAction() {
        String loginString = login.getText().toString();
        String passwordString = password.getText().toString();

        if ("".equals(loginString) || "".equals(passwordString)) {
            Toast toast = makeText(
                    getApplicationContext(),
                    "Введены не все поля",
                    LENGTH_SHORT
            );
            toast.show();
            return;
        }

        String token = loginString + ":" + passwordString;
        String encode = Base64.getEncoder()
                .encodeToString(token.getBytes());

        LoginDto loginDto = LoginDto.builder()
                .login(loginString)
                .build();

        Call<User> loginCall = getUnauthorizedLoginCall(encode, loginDto);

        Context thisContext = this;

        loginCall.enqueue(new Callback<User>() {
            Toast toast;

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    NetworkService.createInstance(thisContext, encode);
                    Intent profileIntent = new Intent(LoginActivity.this, AddPhotoActivity.class);
                    startActivity(profileIntent);
                } else {
                    toast = makeText(
                            getApplicationContext(),
                            response.message(),
                            LENGTH_SHORT
                    );
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                toast = makeText(
                        getApplicationContext(),
                        "Извините, технические неполадки. Попробуйте позднее.",
                        LENGTH_SHORT
                );
                toast.show();
            }
        });
    }

    private Call<User> getUnauthorizedLoginCall(String encode, LoginDto loginDto) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                .build();
        ConnectionSpec spec2 = new ConnectionSpec.Builder(ConnectionSpec.CLEARTEXT)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectionSpecs(asList(spec, spec2))
                .addInterceptor(loggingInterceptor)
                .addInterceptor(createLoginRequestInterceptor(encode))
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl(getResources().getString(R.string.server_url))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        return mRetrofit.create(LoginRegistrationServer.class)
                .login(loginDto);
    }

    private Interceptor createLoginRequestInterceptor(String token) {
        return chain -> {
            Request original = chain.request();

            Request request = original.newBuilder()
                    .header("Accept", "application/json")
                    .header("Authorization", "Basic " + token)
                    .method(original.method(), original.body())
                    .build();

            return chain.proceed(request);
        };
    }
}
