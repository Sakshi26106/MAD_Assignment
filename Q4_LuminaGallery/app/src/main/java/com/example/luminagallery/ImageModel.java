package com.example.luminagallery;

import android.net.Uri;

public class ImageModel {
    private final Uri uri;
    private final String name;
    private final String path;
    private final String size;
    private final String date;

    public ImageModel(Uri uri, String name, String path, String size, String date) {
        this.uri = uri;
        this.name = name;
        this.path = path;
        this.size = size;
        this.date = date;
    }

    public Uri getUri() { return uri; }
    public String getName() { return name; }
    public String getPath() { return path; }
    public String getSize() { return size; }
    public String getDate() { return date; }
}
