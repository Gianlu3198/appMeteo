package com.meteo.appMeteo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Meteo {

    private String citta;
    private double temperatura;
    private double vento;
    private String descrizione;
    private boolean cached;

}
