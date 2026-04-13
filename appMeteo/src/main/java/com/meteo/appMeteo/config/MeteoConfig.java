package com.meteo.appMeteo.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Classe di configurazione per la gestione delle proprietà applicative.
 *
 * Questa classe si occupa di:
 * - Caricare il file application.properties dal classpath
 * - Rendere disponibili i valori di configurazione tramite chiave
 *
 * Le proprietà possono essere utilizzate per configurare:
 * - URL delle API esterne
 * - Parametri di configurazione
 * - Eventuali chiavi o variabili di ambiente
 *
 * Esempio di utilizzo:
 * {@code
 * String geoUrl = MeteoConfig.get("geo.url");
 * String meteoUrl = MeteoConfig.get("meteo.url");
 * }
 *
 * @throws RuntimeException se:
 *         - il file application.properties non viene trovato
 *         - si verifica un errore durante il caricamento
 */
public class MeteoConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = MeteoConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new RuntimeException("File application.properties non trovato");
            }

            props.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Errore caricamento configurazione", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
