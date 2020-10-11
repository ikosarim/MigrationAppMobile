package com.example.migrationapp.service;

import android.content.Context;

import com.example.migrationapp.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.util.Arrays.asList;

public class NetworkService {

    private static NetworkService mInstance;
    private Retrofit mRetrofit;

    private NetworkService(Context context, String token) {
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
                .addInterceptor(createRequestInterceptor(token))
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(context.getResources().getString(R.string.server_url))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }

    public static void createInstance(Context context, String token) {
        mInstance = new NetworkService(context, token);
    }

    public static NetworkService getInstance() {
        return mInstance;
    }

    private Interceptor createRequestInterceptor(String token) {
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
