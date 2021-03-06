package pl.rosa.mapeditor.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.rosa.mapeditor.models.Map;

import java.util.List;

/**
 * Created by Maciej on 2018-11-03 10:20
 */
@Repository
public interface MapRepository extends JpaRepository<Map, Long> {
    List<Map> findByOwnerIdAndVisibility(Long ownerId, String visibility);
}
