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
public class OnlineStreamerClipTask {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BroadcasterRepository broadcasterRepository;

    private final ClipTaskUtil clipTaskUtil;

    public OnlineStreamerClipTask(BroadcasterRepository broadcasterRepository, ClipTaskUtil clipTaskUtil) {
        this.broadcasterRepository = broadcasterRepository;
        this.clipTaskUtil = clipTaskUtil;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 5 * 1000)
    protected void getClipsFromOnlineStreamers() {
        logger.info("Get new clips for online broadcasters");
        ClipTaskUtil.Timespan timespan = clipTaskUtil.getTimespan(Calendar.MINUTE, 20);
        clipTaskUtil.getAndUpdateClips(broadcasterRepository.findAllByStatus(StreamStatus.ONLINE), timespan.getFrom(), timespan.getTo(), TimeWindow.MIN30);
        logger.info("Finished getting new clips for online broadcasters");
    }
}
