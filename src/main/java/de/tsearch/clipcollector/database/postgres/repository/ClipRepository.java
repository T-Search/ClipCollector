package de.tsearch.clipcollector.database.postgres.repository;

import de.tsearch.clipcollector.database.postgres.entity.Clip;
import org.springframework.data.repository.CrudRepository;

public interface ClipRepository extends CrudRepository<Clip, String> {
}
