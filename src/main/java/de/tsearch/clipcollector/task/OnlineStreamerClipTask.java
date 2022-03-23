package de.tsearch.clipcollector.task;

import de.tsearch.clipcollector.database.postgres.entity.Broadcaster;
import de.tsearch.clipcollector.database.postgres.entity.StreamStatus;
import de.tsearch.clipcollector.database.postgres.repository.BroadcasterRepository;
import de.tsearch.clipcollector.database.postgres.repository.ClipRepository;
import de.tsearch.tclient.ClipClient;
import de.tsearch.tclient.GameClient;
import de.tsearch.tclient.http.respone.Clip;
import de.tsearch.tclient.http.respone.Game;
import de.tsearch.tclient.http.respone.TimeWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class OnlineStreamerClipTask {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BroadcasterRepository broadcasterRepository;
    private final ClipRepository clipRepository;
    private final ClipClient clipClient;
    private final GameClient gameClient;

    public OnlineStreamerClipTask(BroadcasterRepository broadcasterRepository, ClipRepository clipRepository, ClipClient clipClient, GameClient gameClient) {
        this.broadcasterRepository = broadcasterRepository;
        this.clipRepository = clipRepository;
        this.clipClient = clipClient;
        this.gameClient = gameClient;
    }

    @Scheduled(fixedRate = 10 * 60 * 1000, initialDelay = 5 * 1000)
    protected void checkBroadcasterStatus() {
        logger.info("Get new clips for online broadcasters");
        for (Broadcaster broadcaster : broadcasterRepository.findAll()) {
            if (!StreamStatus.ONLINE.equals(broadcaster.getStatus())) continue;
            logger.info("Get clips for " + broadcaster.getDisplayName() + " (" + broadcaster.getId() + ")");

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.MINUTE, -20);
            Date from = calendar.getTime();
            calendar.add(Calendar.MINUTE, 20);
            calendar.add(Calendar.MINUTE, 5);
            Date to = calendar.getTime();
            List<Clip> clips = clipClient.getAllClipsInWindowUncached(broadcaster.getId(), from, to, TimeWindow.MIN30);
            logger.debug("Found " + clips.size() + " clips in the last 30 minutes");

            for (Clip tClip : clips) {
                de.tsearch.clipcollector.database.postgres.entity.Clip clip = new de.tsearch.clipcollector.database.postgres.entity.Clip();
                clip.setId(tClip.getId());
                clip.setBroadcaster(broadcaster);
                clip.setCreatorID(tClip.getCreatorID());
                clip.setCreatorName(tClip.getCreatorName());
                clip.setVideoID(tClip.getVideoID());
                if (!tClip.getGameID().equals("")) {
                    Optional<Game> gameOptional = gameClient.getGameById(Long.parseLong(tClip.getGameID()));
                    gameOptional.ifPresent(game -> clip.setGame(game.getName()));
                }
                clip.setLanguage(tClip.getLanguage());
                clip.setTitle(tClip.getTitle());
                clip.setViewCount(tClip.getViewCount());
                clip.setCreatedAt(tClip.getCreatedAt());
                clip.setThumbnailURL(tClip.getThumbnailURL());
                clip.setDuration(tClip.getDuration());
                clipRepository.save(clip);
            }
        }
        logger.info("Finished getting new clips for online broadcasters");
    }
}
