package com.logiflow.authservice_core.model.enums;

/**
 * Tipo de flota/vehículo para repartidores
 * Determina el tipo de entregas que puede realizar
 */
public enum FleetType {
    /**
     * Motocicleta - Para entregas urbanas rápidas (última milla)
     */
    MOTORIZADO("Motorizado", "Entregas urbanas rápidas"),

    /**
     * Vehículo liviano - Para entregas intermunicipales
     */
    VEHICULO_LIVIANO("Vehículo Liviano", "Entregas intermunicipales"),

    /**
     * Camión - Para entregas nacionales
     */
    CAMION("Camión", "Entregas nacionales"),

    /**
     * No aplica - Para roles que no son repartidores
     */
    NONE("N/A", "No aplica");

    private final String displayName;
    private final String description;

    FleetType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    /**
     * Verifica si el tipo de flota puede realizar entregas
     */
    public boolean canDeliver() {
        return this != NONE;
    }
}