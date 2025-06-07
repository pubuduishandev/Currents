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

public class CarouselNewsAdapter extends RecyclerView.Adapter<CarouselNewsAdapter.CarouselNewsViewHolder> {

    private List<NewsItem> newsList;
    private NewsAdapter.OnNewsClickListener listener;

    public CarouselNewsAdapter(List<NewsItem> newsList, NewsAdapter.OnNewsClickListener listener) {
        this.newsList = newsList;
        this.listener = listener;
    }

    public void setNewsList(List<NewsItem> newsList) {
        this.newsList = newsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CarouselNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel_news_card, parent, false);
        return new CarouselNewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselNewsViewHolder holder, int position) {
        NewsItem newsItem = newsList.get(position);
        holder.newsTitleTextView.setText(newsItem.getTitle());
        holder.newsDateTextView.setText(newsItem.getPostedDate());

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

    public static class CarouselNewsViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImageView;
        TextView newsTitleTextView;
        TextView newsDateTextView;

        public CarouselNewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImageView = itemView.findViewById(R.id.newsImageView);
            newsTitleTextView = itemView.findViewById(R.id.newsTitleTextView);
            newsDateTextView = itemView.findViewById(R.id.newsDateTextView);
        }
    }
}