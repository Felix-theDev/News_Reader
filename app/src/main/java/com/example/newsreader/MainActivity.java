/**
 * A news reader app that sources news headline from an API
 * Displays the news headlines to the screen in a listView
 * On the click of a news headline sends an http request to get the news article
 * Displays the web page containing the contents of the news article in a webView
 *
 * @author Ogbonnaya Felix
 */

package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    SQLiteDatabase database;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    ArrayAdapter arrayAdapter;
    ListView listView;

    //Stores the title of the news article
    ArrayList<String> titles;
    URL url;
    boolean exist = false;
    //ArrayList indexes;
    HttpURLConnection urlConnection;

    //Creates an arrayList of News object
    ArrayList<News> newsArray;

    //Declaring the number of the news contents
    int SIZE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);

        titles = new ArrayList<>();
//        indexes = new ArrayList();
        newsArray = new ArrayList<>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);

        listView.setAdapter(arrayAdapter);
        try {
            //Create an SQL table to store the title of the news article together with the url
            database = this.openOrCreateDatabase("News", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS newNews (title VARCHAR, url VARCHAR, id INTEGER PRIMARY KEY)");

            //Points the cursor to the first row in the SQL table
            Cursor c = database.rawQuery("SELECT * FROM newNews", null);
            int titleIndex = c.getColumnIndex("title");
            int urlIndex = c.getColumnIndex("url");
            int keyIndex = c.getColumnIndex("id");
            c.moveToFirst();

            //Check if the table has contents and if the particular cursor points at a row containing items
            while (c != null && c.getCount() > 0) {
                Log.i("INFO", c.getString(titleIndex));
                exist = true;
                String title = c.getString(titleIndex);
                String url = c.getString(urlIndex);
                int id = c.getInt(keyIndex);
                //Creates an instance of the News and adds it to the arrayList
                newsArray.add(new News(title, url, id));
                titles.add(title);
                //Points to the next item in the table
                c.moveToNext();
                Log.i("INFO", "Executing the while statement");
            }
        } catch (Exception e) {
            Log.i("INFO", "Error in creating database");
            e.printStackTrace();
        }

        //Notify the array adapter and Update the listView
        arrayAdapter.notifyDataSetChanged();


        executor.execute(() -> {
            try {
                //Creates an instance of the URL with an argument of the website
                url = new URL("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
                urlConnection = (HttpURLConnection) url.openConnection();

                String result = processStream(urlConnection);
                JSONArray jsonArray = new JSONArray(result);
                Log.i("INFO", jsonArray.toString());

                try {
                    int total = SIZE;
                    int id = 0;
                    for (int i = 0; i < total; i++) {
                        StringBuilder sb = new StringBuilder("https://hacker-news.firebaseio.com/v0/item/");
                        sb.append(jsonArray.get(i));
                        sb.append(".json?print=pretty");
                        url = new URL(sb.toString());
                        urlConnection = (HttpURLConnection) url.openConnection();
                        String newsDetails = processStream(urlConnection);
                        JSONObject jsonObject = new JSONObject(newsDetails);
                        if ((jsonObject.isNull("title")) || jsonObject.isNull("url")) {
                            total++;
                        } else {
                            String title = jsonObject.getString("title");
                            title = title.replace("'", " ");
                            String site = jsonObject.getString("url");
                            //Creates an instance of the News and adds it to the arrayList
                            newsArray.add(new News(title, site, id));
                            titles.add(title);
                            //Creates the SQL command
                            String sql = "INSERT INTO newNews (title, url, id) VALUES (?, ?, ?)";
                            SQLiteStatement statement = database.compileStatement(sql);
                            statement.bindString(1, title);
                            statement.bindString(2, site);
                            statement.bindString(3, String.valueOf(id));
                            //Adds the SQL statement to database
                            database.execSQL("INSERT INTO newNews (title, url, id) VALUES ('" + title + "', '" + site + "', " + id + ")");
                            ++id;
                        }
                    }
                    //Deletes excess news article : Leaving behind only the first (SIZE variable) as articles
                    int surplus = newsArray.size() - SIZE;
                    for (int i = 0; i < surplus; i++) {
                        newsArray.remove(0);
                        titles.remove(0);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("INFO", "ERROR ENCOUNTERED IN INNER CATCH");
                }
            } catch (Exception e) {
                Log.i("INFO", "ERROR ENCOUNTERED");
                e.printStackTrace();
            }

            //Run this command to update the listView after the executor service has finished running
            runOnUiThread(() -> {
                arrayAdapter.notifyDataSetChanged();
                Log.i("INFO", "I'm done");
            });
        });

        //Responds to clicks on the news headline and create an intent to diplay the news
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Sends the url or the clicked news article and display on a new screen
                Intent intent = new Intent(getApplicationContext(), Webviews.class);
                String ur = newsArray.get(position).getUrl();
                intent.putExtra("URL", ur);
                startActivity(intent);

            }
        });


    }

    //Takes as parameter the url instance and returns a string containing the json array of all the news and for individual new articles
    public static String processStream(URLConnection urlConnection) throws IOException {

        InputStream inputStream = urlConnection.getInputStream();

        Log.i("INFO", "Got passed here");
        InputStreamReader io = new InputStreamReader(inputStream);
        Log.i("INFO", "Got passed here");
        String result = "";

        int data = io.read();

        //Read data from the InputStream and concatenate it to the string result
        while (data != -1) {
            char current = (char) data;
            result += current;
            data = io.read();
        }
        io.close();
        return result;
    }


}