package de.tsearch.clipcollector.task;

import de.tsearch.clipcollector.database.postgres.entity.Broadcaster;
import de.tsearch.clipcollector.database.postgres.repository.ClipRepository;
import de.tsearch.tclient.ClipClient;
import de.tsearch.tclient.GameClient;
import de.tsearch.tclient.http.respone.Clip;
import de.tsearch.tclient.http.respone.Game;
import de.tsearch.tclient.http.respone.TimeWindow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ClipTaskUtil {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClipClient clipClient;
    private final GameClient gameClient;

    private final ClipRepository clipRepository;

    public ClipTaskUtil(ClipClient clipClient, GameClient gameClient, ClipRepository clipRepository) {
        this.clipClient = clipClient;
        this.gameClient = gameClient;
        this.clipRepository = clipRepository;
    }

    protected void getAndUpdateClips(Iterable<Broadcaster> broadcasters, Date from, Date to, TimeWindow timeWindow) {
        for (Broadcaster broadcaster : broadcasters) {
            logger.info("Get clips for " + broadcaster.getDisplayName() + " (" + broadcaster.getId() + ")");
            List<Clip> clips = clipClient.getAllClipsInWindowUncached(broadcaster.getId(), from, to, timeWindow);
            logger.debug("Found " + clips.size() + " clips");

            for (Clip tClip : clips) {
                createOrUpdateClip(tClip, broadcaster);
            }
        }
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

    protected Timespan getTimespan(int i1, int i2) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(i1, i2 * (-1));
        Date from = calendar.getTime();
        calendar.add(i1, i2);
        calendar.add(Calendar.MINUTE, 5);
        Date to = calendar.getTime();
        return new Timespan(from, to);
    }

    @Getter
    @AllArgsConstructor
    protected static class Timespan {
        private Date from;
        private Date to;
    }
}
