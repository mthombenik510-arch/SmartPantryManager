package com.example.smartpantry.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.matching.QuantityFormatter;
import com.example.smartpantry.util.ExpiryUtils;

import java.util.ArrayList;
import java.util.List;

/** Custom adapter that binds pantry rows (from the database) to the RecyclerView. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.Holder> {

    public interface Listener {
        void onEdit(PantryItem item);

        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean alertsEnabled = true;
    private int alertDays = 3;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setAlerts(boolean enabled, int days) {
        alertsEnabled = enabled;
        alertDays = days;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        PantryItem item = items.get(position);
        Context c = h.itemView.getContext();

        h.name.setText(item.name);
        h.quantity.setText(QuantityFormatter.format(item.quantity, item.unit));

        ExpiryUtils.State state = ExpiryUtils.state(item.expiryDate, alertDays);
        if (state == ExpiryUtils.State.NONE) {
            h.expiry.setVisibility(View.GONE);
        } else {
            h.expiry.setVisibility(View.VISIBLE);
            boolean expired = state == ExpiryUtils.State.EXPIRED;
            h.expiry.setText(c.getString(expired ? R.string.expired_on : R.string.expires_on, item.expiryDate));
            int color = R.color.text_secondary;
            if (alertsEnabled && expired) {
                color = R.color.red_700;
            } else if (alertsEnabled && state == ExpiryUtils.State.SOON) {
                color = R.color.orange_700;
            }
            h.expiry.setTextColor(ContextCompat.getColor(c, color));
        }

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final ImageButton delete;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            quantity = itemView.findViewById(R.id.item_quantity);
            expiry = itemView.findViewById(R.id.item_expiry);
            delete = itemView.findViewById(R.id.item_delete);
        }
    }
}
