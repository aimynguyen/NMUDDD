package com.example.diary_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diary_app.R;
import com.example.diary_app.DTOs.InventoryItem;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<InventoryItem> inventoryItems;
    private String equippedBackgroundId;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public InventoryAdapter(List<InventoryItem> inventoryItems, String equippedBackgroundId, OnItemClickListener listener) {
        this.inventoryItems = inventoryItems;
        this.equippedBackgroundId = equippedBackgroundId;
        this.listener = listener;
    }

    public void updateData(List<InventoryItem> newItems, String newEquippedId) {
        this.inventoryItems = newItems;
        this.equippedBackgroundId = newEquippedId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = inventoryItems.get(position);

        holder.txtItemName.setText(item.getName());
        if (item.getImageResId() != 0) {
            holder.imgItem.setImageResource(item.getImageResId());
        }

        if (item.isUnlocked()) {
            holder.layoutLocked.setVisibility(View.GONE);
            
            if (item.getId().equals(equippedBackgroundId)) {
                holder.txtEquipped.setVisibility(View.VISIBLE);
                holder.btnUse.setVisibility(View.GONE);
                holder.imgEquipped.setVisibility(View.VISIBLE);
            } else {
                holder.txtEquipped.setVisibility(View.GONE);
                holder.btnUse.setVisibility(View.VISIBLE);
                holder.imgEquipped.setVisibility(View.GONE);
            }
        } else {
            holder.layoutLocked.setVisibility(View.VISIBLE);
            holder.txtEquipped.setVisibility(View.GONE);
            holder.btnUse.setVisibility(View.GONE);
            holder.imgEquipped.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
        
        holder.btnUse.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return inventoryItems == null ? 0 : inventoryItems.size();
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItem, imgEquipped;
        TextView txtItemName, txtEquipped;
        Button btnUse;
        LinearLayout layoutLocked;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.imgItem);
            imgEquipped = itemView.findViewById(R.id.imgEquipped);
            txtItemName = itemView.findViewById(R.id.txtItemName);
            txtEquipped = itemView.findViewById(R.id.txtEquipped);
            btnUse = itemView.findViewById(R.id.btnUse);
            layoutLocked = itemView.findViewById(R.id.layoutLocked);
        }
    }
}
