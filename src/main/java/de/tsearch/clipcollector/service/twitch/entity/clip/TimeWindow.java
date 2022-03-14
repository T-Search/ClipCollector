package de.tsearch.clipcollector.service.twitch.entity.clip;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

public enum TimeWindow {
    YEAR15((byte) 0, 15 * 365 * 86_400),
    YEAR10((byte) 1, 10 * 365 * 86_400),
    YEAR5((byte) 2, 5 * 365 * 86_400),
    YEAR((byte) 3, 365 * 86_400),
    MONTH9((byte) 4, 9 * 31 * 86_400),
    MONTH6((byte) 5, 6 * 31 * 86_400),
    MONTH3((byte) 6, 3 * 31 * 86_400),
    MONTH((byte) 7, 31 * 86_400),
    DAY15((byte) 8, 15 * 86_400),
    DAY((byte) 9, 86_400),
    HOURS12((byte) 10, 43_200),
    HOURS8((byte) 11, 28_800),
    HOURS4((byte) 12, 14_400),
    HOURS2((byte) 13, 7_200),
    HOUR((byte) 14, 3_600),
    MIN30((byte) 15, 1_800),
    MIN15((byte) 16, 900),
    MIN5((byte) 17, 300),
    MIN3((byte) 18, 180),
    MIN2((byte) 19, 120),
    MIN((byte) 20, 60);

    private final byte index;
    private final int timeSizeInSeconds;

    TimeWindow(byte index, int timeSizeInSeconds) {
        this.index = index;
        this.timeSizeInSeconds = timeSizeInSeconds;
    }

    static TimeWindow getTimeWindowByIndex(byte index) {
        for (TimeWindow window : TimeWindow.values()) {
            if (window.index == index) return window;
        }
        return null;
    }

    public Optional<TimeWindow> getBiggerWindow() {
        return Optional.ofNullable(TimeWindow.getTimeWindowByIndex((byte) (index - 1)));
    }

    public Optional<TimeWindow> getSmallerWindow() {
        return Optional.ofNullable(TimeWindow.getTimeWindowByIndex((byte) (index + 1)));
    }

    public Date getEndOfWindow(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, timeSizeInSeconds);
        return calendar.getTime();
    }
}
