package com.technopark.room.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.technopark.room.entities.Record;
import com.technopark.room.entities.Topic;
import com.technopark.room.entities.TopicTypes;

@Database(entities = {Record.class, Topic.class}, version = 1, exportSchema = false)
@TypeConverters({TopicTypes.class})
public abstract class AppRoomDatabase extends RoomDatabase {

    public abstract AppDao appDao();
    private volatile static AppRoomDatabase INSTANCE;

    public static AppRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppRoomDatabase.class, "app_database")
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
