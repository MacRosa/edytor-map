package pl.rosa.mapeditor.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.rosa.mapeditor.models.map.MapDetails;

/**
 * Created by Maciej on 2018-11-15 09:31
 */
@Repository
public interface MapDetailsRepository extends MongoRepository<MapDetails, String> {
}
