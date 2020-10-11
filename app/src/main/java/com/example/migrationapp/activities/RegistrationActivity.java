package com.example.migrationapp.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.migrationapp.R;
import com.example.migrationapp.dto.Token;
import com.example.migrationapp.dto.RegisterDto;
import com.example.migrationapp.rest_servers.LoginRegistrationServer;
import com.example.migrationapp.service.NetworkService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Calendar;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
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

public class RegistrationActivity extends AppCompatActivity {

    private EditText phone;
    private EditText userName;
    private EditText nationality;
    private TextView birthDate;
    private EditText password;

    private Button registration;

    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        phone = findViewById(R.id.registrationPhoneEditText);
        userName = findViewById(R.id.registrationNameEditText);
        nationality = findViewById(R.id.registrationNationalityEditText);
        birthDate = findViewById(R.id.registrationBirthDateDatePicker);
        password = findViewById(R.id.registrationPasswordEditText);

        registration = findViewById(R.id.registration_button);

        inputBirthDate();

        registration.setOnClickListener(view -> registrationAction());
    }

    private void inputBirthDate() {
        birthDate.setOnClickListener(view -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(RegistrationActivity.this,
                    android.R.style.Theme_Holo_Dialog_MinWidth, mDateSetListener, year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });
        mDateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            String date = day + "/" + month + "/" + year;
            birthDate.setText(date);
        };
    }

    private void registrationAction() {
        String phoneString = phone.getText().toString();
        String userNameString = userName.getText().toString();
        String nationalityString = nationality.getText().toString();
        String birthDateString = birthDate.getText().toString();
        String passwordString = password.getText().toString();

        if ("".equals(phoneString)
                || "".equals(userNameString)
                || "".equals(nationalityString)
                || "".equals(passwordString)
                || "Нажмите для выбора даты".equals(birthDateString)) {
            Toast toast = makeText(
                    getApplicationContext(),
                    "Введены не все поля",
                    LENGTH_SHORT
            );
            toast.show();
            return;
        }

        RegisterDto registerDto = RegisterDto.builder()
                .phone(phoneString)
                .userName(userNameString)
                .nationality(nationalityString)
                .dateOfBirth(birthDateString)
                .password(passwordString)
                .build();

        Call<Token> registerCall = getUnauthorizedRegistrationCall(registerDto);

        Context thisContext = this;

        registerCall.enqueue(new Callback<Token>() {
            Toast toast;

            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {
                    NetworkService.createInstance(thisContext, response.body().getText());
                    Intent profileIntent = new Intent(RegistrationActivity.this, AddPhotoActivity.class);
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
            public void onFailure(Call<Token> call, Throwable t) {
                toast = makeText(
                        getApplicationContext(),
                        "Извините, технические неполадки. Попробуйте позднее.",
                        LENGTH_SHORT
                );
                toast.show();
            }
        });


    }

    private Call<Token> getUnauthorizedRegistrationCall(RegisterDto registerDto){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

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
                .addInterceptor(interceptor)
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
                .registration(registerDto);
    }
}
