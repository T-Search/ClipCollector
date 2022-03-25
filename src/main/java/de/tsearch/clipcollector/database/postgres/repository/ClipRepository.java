package de.tsearch.clipcollector.database.postgres.repository;

import de.tsearch.clipcollector.database.postgres.entity.Clip;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ClipRepository extends CrudRepository<Clip, String> {
    @Query("select p.id from #{#entityName} p")
    List<String> getAllIds();
}
