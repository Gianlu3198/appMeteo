package com.meteo.appMeteo.cache;

import com.meteo.appMeteo.model.Meteo;

/**
 * Classe che rappresenta una singola entry della cache.
 *
 * Contiene:
 * - Il dato meteo ({@link com.meteo.appMeteo.model.Meteo})
 * - Il timestamp di creazione della cache
 *
 * Viene utilizzata dal service per:
 * - Ridurre le chiamate alle API esterne
 * - Migliorare le performance
 *
 * Ogni entry ha una durata limitata (TTL - Time To Live)
 * impostata a 1 ora.
 */
public class CacheEntry {
    private final Meteo data;
    private final long timestamp;

    public CacheEntry(Meteo data) {
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public Meteo getData() {
        return data;
    }

    public boolean isValid() {
        long oneHour = 60 * 60 * 1000;
        return System.currentTimeMillis() - timestamp < oneHour;
    }
}
