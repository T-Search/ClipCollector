package de.tsearch.clipcollector.database.redis.repository;


import de.tsearch.clipcollector.database.redis.entity.Broadcaster;
import org.springframework.data.repository.CrudRepository;

public interface BroadcasterRepository extends CrudRepository<Broadcaster, Long> {
}
