/** Displays the news in a webView
 *
 */

package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Webviews extends AppCompatActivity {
    String website;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webviews);
        website = "";
        Intent intent = getIntent();
        website = intent.getStringExtra("URL");
        //Checks if website is a valid string then displays if valid
        if(website.length() != 0){
            webView = findViewById(R.id.webView);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());

            webView.loadUrl(website);
        }

    }
}