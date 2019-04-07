package com.example.apptestvalidationandroid44;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ShowUploadedInvoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_uploaded_invoice);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);


        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.editText4);
        textView.setText(message);


    }

}
