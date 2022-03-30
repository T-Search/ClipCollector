package de.tsearch.clipcollector.task;

import de.tsearch.clipcollector.database.postgres.repository.BroadcasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class GetAllClipsTask {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BroadcasterRepository broadcasterRepository;

    private final ClipTaskUtil clipTaskUtil;

    public GetAllClipsTask(BroadcasterRepository broadcasterRepository, ClipTaskUtil clipTaskUtil) {
        this.broadcasterRepository = broadcasterRepository;
        this.clipTaskUtil = clipTaskUtil;
    }

    //Once a week
    @Scheduled(fixedRate = 7 * 24 * 60 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    protected void getAllClips() {
        logger.info("Get all clips");
        ClipTaskUtil.Timespan timespan = clipTaskUtil.getTimespan(ChronoUnit.YEARS, 10);
        clipTaskUtil.getAndUpdateClips(broadcasterRepository.findAll(), timespan.getFrom(), timespan.getTo(), "AllClips");
        logger.info("Finished getting all clips");
    }
}
