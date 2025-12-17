package espe.edu.ec.fleet_service.repository;

import espe.edu.ec.fleet_service.model.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepartidorRepository extends JpaRepository<Repartidor, Long> {
    Optional<Repartidor> findByIdentificacion(String identificacion);

    boolean existsByIdentificacion(String identificacion);
}
