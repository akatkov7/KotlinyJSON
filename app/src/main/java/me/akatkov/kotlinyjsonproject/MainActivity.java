package me.akatkov.kotlinyjsonproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.akatkov.kotlinyjson.JSON;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JSON json = new JSON();
        json.set("test", "value");
    }
}
