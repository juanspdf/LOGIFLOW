package espe.edu.ec.billing_service.service;

import espe.edu.ec.billing_service.model.Billing;
import espe.edu.ec.billing_service.model.EstadoType;
import espe.edu.ec.billing_service.repository.BillingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class BillingService {

    private final BillingRepository billingRepository;
    private static final BigDecimal TASA_IMPUESTO = new BigDecimal("0.15");

    public BillingService(BillingRepository billingRepository) {
        this.billingRepository = billingRepository;
    }

    @Transactional
    public Billing generarFactura(Billing billing) {
        BigDecimal subtotal = billing.getSubtotal();
        BigDecimal impuestos = calcularImpuesto(subtotal);
        BigDecimal total = subtotal.add(impuestos);

        billing.setImpuestos(impuestos);
        billing.setTotal(total);
        billing.setEstado(EstadoType.BORRADOR);

        return billingRepository.save(billing);
    }

    @Transactional(readOnly = true)
    public List<Billing> listarFacturas() {
        return billingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Billing buscarPorId(Long id) {
        @SuppressWarnings("null")
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        return billing;
    }

    /**
     * Calcula el impuesto (IVA 15%) sobre el subtotal.
     * @param subtotal Monto base antes de impuestos
     * @return Monto del impuesto calculado con 2 decimales
     */
    public BigDecimal calcularImpuesto(BigDecimal subtotal) {
        return subtotal.multiply(TASA_IMPUESTO).setScale(2, RoundingMode.HALF_UP);
    }
}