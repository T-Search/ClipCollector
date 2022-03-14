package de.tsearch.clipcollector.service.twitch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tsearch.clipcollector.service.twitch.entity.clip.Clip;
import de.tsearch.clipcollector.service.twitch.entity.clip.ClipsWrapper;
import de.tsearch.clipcollector.service.twitch.entity.clip.TimeWindow;
import kong.unirest.HttpRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ClipService extends GenericTwitchClient<Clip> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClipService.class);

    public ClipService(@Value("${twitch.clientid}") String clientId,
                       @Value("${twitch.apptoken}") String token,
                       ObjectMapper objectMapper) {
        super(clientId, token, objectMapper, Clip.class);
    }

    public List<Clip> getAllActiveClips(List<String> clipsIds) {
        ArrayList<Clip> clips = new ArrayList<>();
        final int batchSize = 100;
        for (int round = 0; round < Math.ceil(((float) clipsIds.size()) / batchSize); round++) {
            try {
                List<String> list = clipsIds.subList(round * batchSize, Math.min(clipsIds.size(), (round + 1) * batchSize));
                HttpResponse<String> response = Unirest
                        .get("https://api.twitch.tv/helix/clips")
                        .headers(standardHeader)
                        .queryString("first", batchSize)
                        .queryString("id", list)
                        .asString();
                if (response.getStatus() == 200) {
                    ClipsWrapper streamsWrapper = objectMapper.readValue(response.getBody(), ClipsWrapper.class);
                    clips.addAll(streamsWrapper.getData());
                }
            } catch (UnirestException | JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return clips;
    }

    public List<Clip> getAllClipsInWindow(long broadcasterId, Date from, Date to) {
        return this.getAllClipsInWindow(broadcasterId, from, to, TimeWindow.YEAR);
    }

    public List<Clip> getAllClipsInWindow(long broadcasterId, Date from, Date to, TimeWindow baseWindow) {
        LOGGER.debug("Start searching for clips with window " + baseWindow);
        Date currentFrom = from;
        List<Clip> clips = new ArrayList<>();

        while (currentFrom != to) {
            Date currentTo = getMinDate(baseWindow.getEndOfWindow(currentFrom), to);
            LOGGER.debug("Get clips in window " + rfcDate.format(currentFrom) + " - " + rfcDate.format(currentTo));
            List<Clip> currentClips = getAllClipsInWindowWithPaging(broadcasterId, currentFrom, currentTo);
            if (currentClips.size() >= 1000) {
                LOGGER.warn("To many clips in time window " + baseWindow + ". " + currentClips.size() + " clips founded");
                Optional<TimeWindow> smallerWindow = baseWindow.getSmallerWindow();
                if (smallerWindow.isPresent()) {
                    currentClips = getAllClipsInWindow(broadcasterId, currentFrom, currentTo, smallerWindow.get());
                } else {
                    LOGGER.error("Cannot search more accurately for clips for broadcaster id " + broadcasterId + " from " + rfcDate.format(currentFrom) + " to " + rfcDate.format(currentTo));
                }
            }
            clips.addAll(currentClips);
            currentFrom = currentTo;
        }
        return clips;
    }

    private List<Clip> getAllClipsInWindowWithPaging(long broadcasterId, Date from, Date to) {
        HttpRequest<?> httpRequest = Unirest.get("https://api.twitch.tv/helix/clips")
                .queryString("broadcaster_id", broadcasterId)
                .queryString("first", 100)
                .queryString("started_at", rfcDate.format(from))
                .queryString("ended_at", rfcDate.format(to));

        return requestWithCursorFollowing(httpRequest);
    }

    private Date getMinDate(Date d1, Date d2) {
        if (d1.before(d2)) {
            return d1;
        } else {
            return d2;
        }
    }
}
