package com.sougata.sudoku.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.ButtonClickListener;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.HistoryItem;
import com.sougata.sudoku.R;
import com.sougata.sudoku.activities.GameActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ItemViewHolder> {

    public ArrayList<HistoryItem> items;
    private final Database db;
    Context context;
    GlobalStore globalStore = GlobalStore.getInstance();
    ButtonClickListener listener;

    public HistoryRecyclerViewAdapter(ArrayList<HistoryItem> items, Context context, ButtonClickListener listener) {
        this.items = items;
        this.context = context;
        this.listener = listener;
        db = new Database(context);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView date, time, difficulty, timer, hint, mistake;
        ImageView isCompleted;
        Button playAgain;
        CardView cardView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.tv_history_date);
            time = itemView.findViewById(R.id.tv_history_time);
            difficulty = itemView.findViewById(R.id.tv_history_difficulty);
            timer = itemView.findViewById(R.id.tv_history_timer);
            hint = itemView.findViewById(R.id.tv_history_hints);
            mistake = itemView.findViewById(R.id.tv_history_mistake);
            isCompleted = itemView.findViewById(R.id.iv_history_is_completed);
            playAgain = itemView.findViewById(R.id.btn_history_play_again);
            cardView = itemView.findViewById(R.id.cv_history_item);

            cardView.setBackgroundResource(R.drawable.history_card_bg);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        HistoryItem currentItem = items.get(position);
        holder.date.setText(currentItem.getDate());
        holder.time.setText(currentItem.getTime());
        holder.difficulty.setText(currentItem.getDifficulty());
        holder.timer.setText(currentItem.getTimer());
        holder.mistake.setText(String.valueOf(currentItem.getMistake()));
        holder.hint.setText(String.valueOf(currentItem.getHint()));

        if (currentItem.isCompleted()) {
            holder.isCompleted.setImageResource(R.drawable.ic_tick);
            holder.isCompleted.setBackgroundResource(R.drawable.history_indicator_bg);
            holder.isCompleted.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        } else {
            holder.isCompleted.setImageResource(R.drawable.ic_cross);
            holder.isCompleted.setBackgroundResource(R.drawable.history_indicator_bg_failed);
            holder.isCompleted.setColorFilter(ContextCompat.getColor(context, R.color.danger), PorterDuff.Mode.SRC_IN);

        }

        holder.playAgain.setOnClickListener(v -> {
            startGame(currentItem.getId());
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void startGame(long id) {
        Cursor cursor = db.getGameById(id);
        if (cursor.getCount() == 0) return;
        cursor.moveToFirst();
        globalStore.setId(id);
        globalStore.setCurrentLevel(cursor.getInt(1));
        globalStore.setDifficulty(cursor.getInt(2));
        globalStore.setDifficultyName(cursor.getString(3));
        globalStore.setTimer(0);
        globalStore.setCurrentBoardState(HelperFunctions.parseTwoDimArray(cursor.getString(6)));
        globalStore.setBoard(HelperFunctions.parseTwoDimArray(cursor.getString(6)));
        globalStore.setSolution(HelperFunctions.parseTwoDimArray(cursor.getString(7)));
        globalStore.setHints(0);
        globalStore.setMistakes(0);
        globalStore.setType(cursor.getString(10));
        globalStore.setPaused(false);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(cursor.getLong(12));
        globalStore.setDay(c.get(Calendar.DAY_OF_MONTH));
        globalStore.setMonth(c.get(Calendar.MONTH));
        globalStore.setYear(c.get(Calendar.YEAR));

        Intent intent = new Intent(this.context, GameActivity.class);
        if (globalStore.getType().equals(Constants.TYPES[1])) {
            intent.putExtra("date", globalStore.getDay());
        }
        context.startActivity(intent);
        listener.onItemButtonClick();
    }
}
