/** News class to store News articles
 *
 */
package com.example.newsreader;

public class News {
    String title;
    String url;
    int id;

    public News(String title, String url, int id) {
        this.title = title;
        this.url = url;
        this.id = id;
    }

    public String getUrl() {
        return url;
    }
}
