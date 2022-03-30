package de.tsearch.clipcollector.task;

import de.tsearch.clipcollector.database.postgres.entity.StreamStatus;
import de.tsearch.clipcollector.database.postgres.repository.BroadcasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

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
        ClipTaskUtil.Timespan timespan = clipTaskUtil.getTimespan(ChronoUnit.MINUTES, 20);
        clipTaskUtil.getAndUpdateClips(broadcasterRepository.findAllByStatus(StreamStatus.ONLINE), timespan.getFrom(), timespan.getTo(), "OnlineClips");
        logger.info("Finished getting new clips for online broadcasters");
    }
}
