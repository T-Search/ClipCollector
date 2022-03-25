package de.tsearch.clipcollector.batch;

import de.tsearch.clipcollector.database.postgres.converter.TClipConverter;
import de.tsearch.clipcollector.database.postgres.entity.Clip;
import de.tsearch.clipcollector.database.postgres.repository.ClipRepository;
import de.tsearch.tclient.ClipClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Collections;
import java.util.List;


public class ClipItemProcessor implements ItemProcessor<Clip, Clip> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClipRepository clipRepository;

    private final ClipClient clipClient;

    private final TClipConverter tClipConverter;

    public ClipItemProcessor(ClipRepository clipRepository, ClipClient clipClient, TClipConverter tClipConverter) {
        this.clipRepository = clipRepository;
        this.clipClient = clipClient;
        this.tClipConverter = tClipConverter;
    }

    @Override
    public Clip process(Clip clip) {
        List<de.tsearch.tclient.http.respone.Clip> activeClipsUncached = clipClient.getAllActiveClipsUncached(Collections.singletonList(clip.getId()));
        if (activeClipsUncached.isEmpty()) {
            clipRepository.delete(clip);
            return null;
        }

        tClipConverter.updateDatabaseClipProperties(clip, activeClipsUncached.get(0));
        return clip;
    }
}
