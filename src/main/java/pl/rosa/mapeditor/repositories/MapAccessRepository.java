package pl.rosa.mapeditor.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.rosa.mapeditor.models.MapAccess;

/**
 * Created by Maciej on 2018-12-17 15:58
 */
@Repository
public interface MapAccessRepository extends JpaRepository<MapAccess, Long> {
}
