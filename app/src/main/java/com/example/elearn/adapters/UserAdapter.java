package com.example.elearn.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elearn.R;
import com.example.elearn.models.User;

import java.util.List;

/**
 * RecyclerView adapter for displaying user items in a list layout.
 * Each item shows the user's name, email, and a role badge colored by role.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final List<User> users;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        holder.userRoleBadge.setText(user.getRole());

        int badgeColor;
        switch (user.getRole()) {
            case "Admin":
                badgeColor = 0xFFD32F2F;
                break;
            case "Student":
                badgeColor = 0xFF1976D2;
                break;
            case "Teacher":
                badgeColor = 0xFF388E3C;
                break;
            default:
                badgeColor = 0xFF757575;
                break;
        }
        holder.userRoleBadge.setBackgroundColor(badgeColor);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    /**
     * ViewHolder for user list items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView userName;
        final TextView userEmail;
        final TextView userRoleBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userRoleBadge = itemView.findViewById(R.id.userRoleBadge);
        }
    }
}
