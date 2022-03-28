package de.tsearch.clipcollector.task;

import de.tsearch.clipcollector.database.postgres.entity.Broadcaster;
import de.tsearch.clipcollector.database.postgres.repository.ClipRepository;
import de.tsearch.tclient.ClipClient;
import de.tsearch.tclient.GameClient;
import de.tsearch.tclient.http.respone.Clip;
import de.tsearch.tclient.http.respone.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ClipTaskUtil {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClipClient clipClient;
    private final GameClient gameClient;

    private final ClipRepository clipRepository;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "cliptaskutil-thread-" + counter.getAndIncrement());
        }
    });

    public ClipTaskUtil(ClipClient clipClient, GameClient gameClient, ClipRepository clipRepository) {
        this.clipClient = clipClient;
        this.gameClient = gameClient;
        this.clipRepository = clipRepository;
    }

    protected void getAndUpdateClips(Iterable<Broadcaster> broadcasters, Instant from, Instant to) {
        List<Future<?>> futures = new ArrayList<>();
        for (Broadcaster broadcaster : broadcasters) {
            futures.add(executorService.submit(() -> getAndUpdateClipBroadcaster(broadcaster, from, to)));

        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException | InterruptedException ignored) {

            }
        }
    }

    private void getAndUpdateClipBroadcaster(Broadcaster broadcaster, Instant from, Instant to) {
        List<Clip> clips = clipClient.getAllClipsInWindowUncached(broadcaster.getId(), from, to);
        logger.debug("Found " + clips.size() + " clips for broadcaster " + broadcaster.getDisplayName() + " (" + broadcaster.getId() + ")");
        logger.debug("Saving clips to database");
        for (Clip tClip : clips) {
            createOrUpdateClip(tClip, broadcaster);
        }
        logger.info("Got clips for broadcaster " + broadcaster.getDisplayName() + " (" + broadcaster.getId() + ")");
    }

    protected void createOrUpdateClip(Clip tClip, Broadcaster broadcaster) {
        de.tsearch.clipcollector.database.postgres.entity.Clip clip = new de.tsearch.clipcollector.database.postgres.entity.Clip();
        clip.setId(tClip.getId());
        clip.setBroadcaster(broadcaster);
        clip.setCreatorId(tClip.getCreatorID());
        clip.setCreatorName(tClip.getCreatorName());
        clip.setVideoId(tClip.getVideoID());
        if (!tClip.getGameID().equals("")) {
            Optional<Game> gameOptional = gameClient.getGameById(Long.parseLong(tClip.getGameID()));
            gameOptional.ifPresent(game -> clip.setGame(game.getName()));
        }
        clip.setLanguage(tClip.getLanguage());
        clip.setTitle(tClip.getTitle());
        clip.setViewCount(tClip.getViewCount());
        clip.setCreatedAt(tClip.getCreatedAt());
        clip.setThumbnailUrl(tClip.getThumbnailURL());
        clip.setDuration(tClip.getDuration());
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
