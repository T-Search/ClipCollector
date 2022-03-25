package de.tsearch.clipcollector.database.postgres.repository;

import de.tsearch.clipcollector.database.postgres.entity.Broadcaster;
import de.tsearch.clipcollector.database.postgres.entity.StreamStatus;
import org.springframework.data.repository.CrudRepository;

public interface BroadcasterRepository extends CrudRepository<Broadcaster, Long> {
    Iterable<Broadcaster> findAllByStatus(StreamStatus status);
}
