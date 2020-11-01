package com.com.technoparkproject.view.adapters.main_list_records;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.com.technoparkproject.R;

public class SwipeCallback extends ItemTouchHelper.SimpleCallback {
    private final ColorDrawable backgroundInfo;
    private final Drawable iconInfo;
    private RecyclerView recyclerView;

    public SwipeCallback(Context context) {
        super(0, ItemTouchHelper.START);

        backgroundInfo = new ColorDrawable(ContextCompat.getColor(context,
                R.color.blue_info));
        iconInfo = ContextCompat.getDrawable(context,
                R.drawable.ic_info_outline_black_24dp);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                         int direction) {
        Log.i("onSwiped", "onSwiped");
        clearView(recyclerView, viewHolder);
    }


    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        this.recyclerView = recyclerView;
        View itemView = viewHolder.itemView;
        float dx = dX;
        if(itemView.getRight() <= -dX)
            dx =  dX + itemView.getRight();

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {

            if (dx < 0) {
                int BACKGROUND_OFFSET = 20;
                int backgroundInfoLeft = itemView.getRight() + ((int) dx) - BACKGROUND_OFFSET;

                backgroundInfo.setBounds(backgroundInfoLeft,
                        itemView.getTop(),
                        itemView.getRight(),
                        itemView.getBottom());

                int iconMargin = (itemView.getHeight() - iconInfo.getIntrinsicHeight()) / 3;
                int iconLeft = itemView.getRight() - iconMargin - iconInfo.getIntrinsicWidth();
                if (iconLeft > backgroundInfoLeft) {
                    int iconTop = itemView.getTop() + (itemView.getHeight() - iconInfo.getIntrinsicHeight()) / 2;
                    int iconBottom = iconTop + iconInfo.getIntrinsicHeight();
                    int iconRight = itemView.getRight() - iconMargin;

                    iconInfo.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                } else {
                    iconInfo.setBounds(0, 0, 0, 0);
                }
            } else {
                backgroundInfo.setBounds(0, 0, 0, 0);
            }

            Log.i("size", " dx: " + dx + " isActive: " + isCurrentlyActive
                    + " left: " + itemView.getLeft() + " right: " + itemView.getRight()
                    + " holder" + viewHolder.itemView.toString());

            this.recyclerView = recyclerView;
            backgroundInfo.draw(c);
            iconInfo.draw(c);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dx, dY, actionState, isCurrentlyActive);
    }

}
