package com.joelimyx.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String ENCODED = "V1RZd0k3Y0N6cE43VmNCYVVIT0dSb252OTpzNEM3aG9tT2FxSkpFeXV1ZWJNNHlGQXd2ZkFid0xqN21uNThMMjhKQmVGUzU0VzdPSQ==";
    private static String mToken;

    RecyclerView mRecyclerView;
    EditText mEditText;
    Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mEditText = (EditText) findViewById(R.id.input);
        mButton = (Button) findViewById(R.id.button);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        getAccessToken();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTweet(mEditText.getText().toString());
            }
        };
        mButton.setOnClickListener(listener);
    }
    private void getAccessToken(){
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();
        Headers headers = new Headers.Builder()
                .add("Authorization","Basic "+ENCODED)
                .add("Content-Type","application/x-www-form-urlencoded;charset=UTF-8")
                .build();

        Request request = new Request.Builder().url("https://api.twitter.com/oauth2/token")
                .headers(headers)
                .post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    throw new IOException("Unexpected Error: "+response);
                }
                try {
                    JSONObject result =  new JSONObject(response.body().string());
                    mToken = result.getString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void getTweet(String s){
        OkHttpClient client = new OkHttpClient();
        Headers headers = new Headers.Builder()
                .add("Authorization","Bearer "+mToken)
                .build();
        Request request = new Request.Builder().url("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name="+s).headers(headers).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    throw new IOException("Unexpected Error: "+response);
                }
                Gson gson = new Gson();
                Type listtype = new TypeToken<List<Tweet>>(){}.getType();
                final List<Tweet> tweets = gson.fromJson(response.body().string(),listtype);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.setAdapter(new MainAdapter(tweets));
                    }
                });

            }
        });
    }
}
