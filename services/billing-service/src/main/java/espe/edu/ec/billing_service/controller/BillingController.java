package espe.edu.ec.billing_service.controller;


import espe.edu.ec.billing_service.model.Billing;
import espe.edu.ec.billing_service.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/facturas")
    public ResponseEntity<Billing> crearFactura(@RequestBody Billing billing) {
        return ResponseEntity.ok(billingService.generarFactura(billing));
    }

    @GetMapping("/facturas")
    public ResponseEntity<List<Billing>> listarFacturas() {
        return ResponseEntity.ok(billingService.listarFacturas());
    }

    @GetMapping("/facturas/{id}")
    public ResponseEntity<Billing> buscarFactura(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.buscarPorId(id));
    }
}