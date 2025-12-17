package espe.edu.ec.billing_service.repository;

import espe.edu.ec.billing_service.model.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingRepository extends JpaRepository<Billing,Long > {
    Optional<Billing> findById(long id);
}
