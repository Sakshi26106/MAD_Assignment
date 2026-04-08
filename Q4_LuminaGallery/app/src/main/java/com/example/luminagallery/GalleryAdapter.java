package com.example.luminagallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

public class GalleryAdapter extends BaseAdapter {

    private final Context context;
    private final List<ImageModel> imageList;

    public GalleryAdapter(Context context, List<ImageModel> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_gallery_image, parent, false);
        }

        ImageView imageView = (ImageView) convertView;
        ImageModel image = imageList.get(position);

        Glide.with(context)
                .load(image.getUri())
                .centerCrop()
                .into(imageView);

        return convertView;
    }
}
