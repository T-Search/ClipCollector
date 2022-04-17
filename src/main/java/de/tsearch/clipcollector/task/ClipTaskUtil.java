package de.tsearch.clipcollector.task;

import de.tsearch.clipcollector.database.postgres.converter.TClipConverter;
import de.tsearch.clipcollector.database.postgres.entity.Broadcaster;
import de.tsearch.clipcollector.database.postgres.repository.ClipRepository;
import de.tsearch.tclient.ClipClient;
import de.tsearch.tclient.http.respone.Clip;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ClipTaskUtil {
    private final ClipClient clipClient;

    private final ClipRepository clipRepository;

    private final TClipConverter tClipConverter;

    private static final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "cliptaskutil-thread-" + counter.getAndIncrement());
        }
    });

    public ClipTaskUtil(ClipClient clipClient, ClipRepository clipRepository, TClipConverter tClipConverter) {
        this.clipClient = clipClient;
        this.clipRepository = clipRepository;
        this.tClipConverter = tClipConverter;
    }

    protected void getAndUpdateClips(Iterable<Broadcaster> broadcasters, Instant from, Instant to, Logger logger) {
        List<Future<?>> futures = new ArrayList<>();
        for (Broadcaster broadcaster : broadcasters) {
            futures.add(executorService.submit(() -> getAndUpdateClipBroadcaster(broadcaster, from, to, logger)));

        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException | InterruptedException ignored) {

            }
        }
    }

    private void getAndUpdateClipBroadcaster(Broadcaster broadcaster, Instant from, Instant to, Logger logger) {
        List<Clip> clips = clipClient.getAllClipsInWindowUncached(broadcaster.getId(), from, to);
        logger.debug("Found {} clips for broadcaster {}({})", clips.size(), broadcaster.getDisplayName(), broadcaster.getId());
        logger.debug("Saving clips to database");
        for (Clip tClip : clips) {
            createOrUpdateClip(tClip, broadcaster);
        }
        logger.debug("Got clips for broadcaster {}({})", broadcaster.getDisplayName(), broadcaster.getId());
    }

    protected void createOrUpdateClip(Clip tClip, Broadcaster broadcaster) {
        de.tsearch.clipcollector.database.postgres.entity.Clip clip = new de.tsearch.clipcollector.database.postgres.entity.Clip();
        clip.setBroadcaster(broadcaster);
        tClipConverter.updateDatabaseClipProperties(clip, tClip);
        clipRepository.save(clip);
    }

    protected Timespan getTimespan(TemporalUnit temporalUnit, int i2) {
        ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
        Instant from = now.minus(i2, temporalUnit).toInstant();
        Instant to = now.plus(5, ChronoUnit.MINUTES).toInstant();
        return new Timespan(from, to);
    }

    @Getter
    @AllArgsConstructor
    protected static class Timespan {
        private Instant from;
        private Instant to;
    }
}
