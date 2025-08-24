package com.nitsoft.ipoallotmentprank;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;        // <- AppCompat dialog
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public final class DialogOptions {

    private DialogOptions() {}

    // Public API
    public interface OnOptionSelected {
        void onSelected(OptionItem item);
    }

    public static class OptionItem {
        public final int id;
        public final String label;
        public final int iconRes;

        public OptionItem(int id, String label, int iconRes) {
            this.id = id;
            this.label = label;
            this.iconRes = iconRes;
        }
    }

    /** Show the options dialog (AppCompat). */
    public static AlertDialog show(@NonNull Context ctx,
                                   @NonNull List<OptionItem> items,
                                   @NonNull OnOptionSelected cb) {

        View content = LayoutInflater.from(ctx).inflate(R.layout.dialog_options, null, false);
        RecyclerView rv = content.findViewById(R.id.rvOptions);
        rv.setLayoutManager(new LinearLayoutManager(ctx));
        OptionsAdapter adapter = new OptionsAdapter(items, option -> {
            // dismiss happens below via adapter-attached dialog
            cb.onSelected(option);
        });
        rv.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(content)
                .create();

        // Optional: transparent background so your card’s rounded corners show
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        adapter.attachDialog(dialog);
        dialog.show();
        return dialog;
    }

    // Internal adapter
    private static class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.VH> {
        interface Click { void onClick(OptionItem item); }

        private final List<OptionItem> data;
        private final Click click;
        private AlertDialog dialog;

        OptionsAdapter(List<OptionItem> data, Click click) {
            this.data = data;
            this.click = click;
        }

        void attachDialog(AlertDialog dialog) { this.dialog = dialog; }

        static class VH extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView label;
            VH(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                label = itemView.findViewById(R.id.label);
            }
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dialog_item_option, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            OptionItem it = data.get(position);
            h.icon.setImageResource(it.iconRes);
            h.label.setText(it.label);
            h.itemView.setOnClickListener(v -> {
                if (dialog != null) dialog.dismiss();
                click.onClick(it);
            });
        }

        @Override public int getItemCount() { return data.size(); }
    }
}
