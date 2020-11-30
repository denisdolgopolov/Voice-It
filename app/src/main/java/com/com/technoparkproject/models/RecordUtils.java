package com.com.technoparkproject.models;

public class RecordUtils {

    public static String durationFormatted(Long duration) {
        return durationFormatted(duration.toString());
    }
    public static String durationFormatted(Integer duration) {
        return durationFormatted(duration.toString());
    }

    public static String durationFormatted(String duration) {
        String durationString;
        int seconds = (int) Long.parseLong(duration) / 1000;
        int minutes = (int) seconds / 60;
        seconds = seconds - minutes * 60;
        if (seconds >= 10) {
            durationString = minutes + ":" + seconds;
        } else {
            durationString = minutes + ":0" + seconds;
        }
        return durationString;
    }
}
