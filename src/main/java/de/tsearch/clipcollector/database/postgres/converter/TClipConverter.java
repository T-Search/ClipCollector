package de.tsearch.clipcollector.database.postgres.converter;

import de.tsearch.clipcollector.database.postgres.entity.Clip;
import de.tsearch.tclient.GameClient;
import de.tsearch.tclient.http.respone.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TClipConverter {

    @Autowired
    private GameClient gameClient;

    /**
     * Update given clip with information from tClip. This will not update the broadcaster!
     *
     * @param clip  database entity to update
     * @param tClip Twitch Clip entity with informations
     */
    public void updateDatabaseClipProperties(Clip clip, de.tsearch.tclient.http.respone.Clip tClip) {
        clip.setId(tClip.getId());
        clip.setCreatorId(tClip.getCreatorID());
        clip.setCreatorName(tClip.getCreatorName());
        clip.setVideoId(convertStringToLong(tClip.getVideoID()));
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
    }

    private Long convertStringToLong(String videoId) {
        if (videoId == null) return null;
        if (videoId.equals("")) return null;
        try {
            return Long.parseLong(videoId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
