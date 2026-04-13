package com.meteo.appMeteo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meteo.appMeteo.model.Meteo;
import com.meteo.appMeteo.service.MeteoService;

/**
 * Controller REST per la gestione delle richieste meteo.
 *
 * Espone un endpoint HTTP che consente di ottenere le informazioni
 * meteorologiche correnti per una determinata città.
 *
 * L'endpoint delega la logica al {@link com.meteo.appMeteo.service.MeteoService}
 * che si occupa di:
 *   - Validare l'input-
 *   - Recuperare le coordinate tramite API di geocoding
 *   - Recuperare i dati meteo tramite API Open-Meteo
 *   - Gestire la cache dei risultati
 *
 * Endpoint disponibile:
 * GET /meteo/{citta}
 *
 * @author Gianluca Coccimiglio
 */
@RestController
@RequestMapping("/meteo")
@CrossOrigin(origins = "*")
public class MeteoController {

    @Autowired
    private MeteoService meteoService;

    
    @GetMapping("/{citta}")
    public Meteo getMeteo(@PathVariable String citta) {
        return meteoService.getMeteoByCity(citta);
    }
}
