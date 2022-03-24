package de.tsearch.clipcollector.task;

import de.tsearch.clipcollector.database.postgres.entity.StreamStatus;
import de.tsearch.clipcollector.database.postgres.repository.BroadcasterRepository;
import de.tsearch.tclient.http.respone.TimeWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Component
public class OfflineStreamerClipTask {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BroadcasterRepository broadcasterRepository;

    private final ClipTaskUtil clipTaskUtil;

    public OfflineStreamerClipTask(BroadcasterRepository broadcasterRepository, ClipTaskUtil clipTaskUtil) {
        this.broadcasterRepository = broadcasterRepository;
        this.clipTaskUtil = clipTaskUtil;
    }

    @Scheduled(fixedRate = 90 * 60 * 1000, initialDelay = 2 * 60 * 1000)
    protected void getClipsFromOfflineStreamers() {
        logger.info("Get new clips for offline broadcasters");
        ClipTaskUtil.Timespan timespan = clipTaskUtil.getTimespan(Calendar.HOUR, 2);
        clipTaskUtil.getAndUpdateClips(broadcasterRepository.findAllByStatus(StreamStatus.OFFLINE), timespan.getFrom(), timespan.getTo(), TimeWindow.HOURS4);
        logger.info("Finished getting new clips for offline broadcasters");
    }
}
