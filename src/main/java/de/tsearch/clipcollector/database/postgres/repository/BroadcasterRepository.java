package de.tsearch.clipcollector.database.postgres.repository;

import de.tsearch.clipcollector.database.postgres.entity.Broadcaster;
import org.springframework.data.repository.CrudRepository;

public interface BroadcasterRepository extends CrudRepository<Broadcaster, Long> {
}
