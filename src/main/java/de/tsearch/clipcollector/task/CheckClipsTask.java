package de.tsearch.clipcollector.task;

import de.tsearch.clipcollector.database.postgres.converter.TClipConverter;
import de.tsearch.clipcollector.database.postgres.repository.ClipRepository;
import de.tsearch.tclient.ClipClient;
import de.tsearch.tclient.http.respone.Clip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CheckClipsTask {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClipRepository clipRepository;

    private final ClipClient clipClient;

    private final TClipConverter tClipConverter;

    public CheckClipsTask(ClipRepository clipRepository, ClipClient clipClient, TClipConverter tClipConverter) {
        this.clipRepository = clipRepository;
        this.clipClient = clipClient;
        this.tClipConverter = tClipConverter;
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000, initialDelay = 3 * 60 * 60 * 1000)
    protected void checkAllClips() {
        logger.info("Start check all existing clips job");
        List<String> ids = clipRepository.getAllIds();

        final int batchSize = 1_000;
        for (int round = 0; round < Math.ceil(((float) ids.size()) / batchSize); round++) {
            logger.debug("Check clips starting index {} from {}", round * batchSize, ids.size());
            List<String> list = ids.subList(round * batchSize, Math.min(ids.size(), (round + 1) * batchSize));
            checkClipTask(list);
        }
        logger.info("Finishing check all existing clips job");
    }

    private void checkClipTask(List<String> ids) {
        List<Clip> activeClips = clipClient.getAllActiveClipsUncached(ids);
        for (Clip activeClip : activeClips) {
            Optional<de.tsearch.clipcollector.database.postgres.entity.Clip> clipOptional = clipRepository.findById(activeClip.getId());
            if (clipOptional.isEmpty()) continue;
            tClipConverter.updateDatabaseClipProperties(clipOptional.get(), activeClip);
        }

        //Delete old clips
        List<String> toDeleteIds = ids.stream().filter(id -> activeClips.stream().noneMatch(clip -> id.equals(clip.getId()))).toList();
        clipRepository.deleteAllById(toDeleteIds);
        logger.debug("To delete clip ids: {}", toDeleteIds);
    }
}
