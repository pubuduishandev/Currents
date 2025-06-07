package com.example.currents.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Import Glide
import com.example.currents.R;
import com.example.currents.model.NewsItem;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsItem> newsList;
    private OnNewsClickListener listener;

    public interface OnNewsClickListener {
        void onNewsClick(NewsItem newsItem);
    }

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_saved, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem newsItem = newsList.get(position);
        holder.titleTextView.setText(newsItem.getTitle());
        holder.postedDateTextView.setText(newsItem.getPostedDate());

        // --- Image Loading Logic with Glide ---
        if (newsItem.getImageUrl() != null && !newsItem.getImageUrl().isEmpty()) {
            Glide.with(holder.newsImageView.getContext())
                    .load(newsItem.getImageUrl())
                    .placeholder(R.drawable.news_placeholder) // Show placeholder while loading
                    .error(R.drawable.news_placeholder) // Show placeholder if image loading fails
                    .into(holder.newsImageView);
        } else {
            // If no imageUrl, use the local drawable placeholder
            holder.newsImageView.setImageResource(newsItem.getImageResId() != 0 ? newsItem.getImageResId() : R.drawable.news_placeholder);
        }
        // --- End Image Loading Logic ---

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