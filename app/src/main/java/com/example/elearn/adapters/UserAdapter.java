package com.example.elearn.adapters;

import android.graphics.drawable.GradientDrawable;
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
 * RecyclerView adapter for displaying user items in a card-based list layout.
 * Each item shows a circular avatar with first letter, name, email,
 * and a role badge colored pill (Admin=red, Student=blue, Teacher=green).
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final List<User> users;

    private static final int[] AVATAR_COLORS = {
            0xFF5B4FCF, 0xFF1976D2, 0xFF4CAF50, 0xFFFF9800,
            0xFFE91E63, 0xFF00BCD4, 0xFF9C27B0, 0xFF795548
    };

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

        // Avatar: first letter of name in colored circle
        String name = user.getName();
        String initial = (name != null && !name.isEmpty()) ? name.substring(0, 1).toUpperCase() : "?";
        holder.userAvatar.setText(initial);

        int avatarColor = AVATAR_COLORS[position % AVATAR_COLORS.length];
        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(avatarColor);
        holder.userAvatar.setBackground(avatarBg);

        // Role badge with colored pill background
        holder.userRoleBadge.setText(user.getRole());

        int badgeTextColor;
        int badgeBgRes;
        switch (user.getRole()) {
            case "Admin":
                badgeTextColor = 0xFFD32F2F;
                badgeBgRes = R.drawable.badge_red;
                break;
            case "Student":
                badgeTextColor = 0xFF1976D2;
                badgeBgRes = R.drawable.badge_blue;
                break;
            case "Teacher":
                badgeTextColor = 0xFF388E3C;
                badgeBgRes = R.drawable.badge_green;
                break;
            default:
                badgeTextColor = 0xFF757575;
                badgeBgRes = R.drawable.badge_blue;
                break;
        }
        holder.userRoleBadge.setTextColor(badgeTextColor);
        holder.userRoleBadge.setBackgroundResource(badgeBgRes);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    /**
     * ViewHolder for user list items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView userAvatar;
        final TextView userName;
        final TextView userEmail;
        final TextView userRoleBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userRoleBadge = itemView.findViewById(R.id.userRoleBadge);
        }
    }
}
