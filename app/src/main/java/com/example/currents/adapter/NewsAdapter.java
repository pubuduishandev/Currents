package com.example.currents.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.currents.R;
import com.example.currents.model.NewsItem;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsItem> newsList;
    private OnNewsClickListener listener; // Declare the listener

    // Interface for click events
    public interface OnNewsClickListener {
        void onNewsClick(NewsItem newsItem);
    }

    // Modified constructor to accept the listener
    public NewsAdapter(List<NewsItem> newsList, OnNewsClickListener listener) {
        this.newsList = newsList;
        this.listener = listener;
    }

    public void setNewsList(List<NewsItem> newsList) {
        this.newsList = newsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure this layout (item_news_saved) is correctly defined for your news cards
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_saved, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem newsItem = newsList.get(position);
        holder.titleTextView.setText(newsItem.getTitle());
        holder.postedDateTextView.setText(newsItem.getPostedDate());
        holder.newsImageView.setImageResource(newsItem.getImageResId());
        // If your image logic is more complex (e.g., Glide/Picasso), use that here.

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNewsClick(newsItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView postedDateTextView;
        ImageView newsImageView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.newsTitle);
            postedDateTextView = itemView.findViewById(R.id.newsPostedDate);
            newsImageView = itemView.findViewById(R.id.newsImage);
        }
    }
}