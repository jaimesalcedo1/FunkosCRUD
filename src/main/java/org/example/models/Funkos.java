package org.example.models;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class Funkos {
    private UUID COD;
    private String NOMBRE;
    private Modelo MODELO;
    private double PRECIO;
    private LocalDate FECHA_LANZAMIENTO;

    public Funkos(UUID COD, String NOMBRE, Modelo MODELO, double PRECIO, LocalDate FECHA_LANZAMIENTO) {
        this.COD = COD;
        this.NOMBRE = NOMBRE;
        this.MODELO = MODELO;
        this.PRECIO = PRECIO;
        this.FECHA_LANZAMIENTO = FECHA_LANZAMIENTO;
    }

    @Override
    public String toString() {
        return "Funkos{" +
                "COD=" + COD +
                ", NOMBRE='" + NOMBRE + '\'' +
                ", MODELO=" + MODELO +
                ", PRECIO=" + PRECIO +
                ", FECHA_LANZAMIENTO=" + FECHA_LANZAMIENTO +
                '}';
    }
}


