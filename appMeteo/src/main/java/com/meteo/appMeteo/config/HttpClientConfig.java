package com.meteo.appMeteo.config;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione Spring per la creazione del client HTTP.
 *
 * Questa classe definisce i bean necessari all'applicazione
 * per effettuare chiamate HTTP verso API esterne.
 *
 * In particolare, espone un {@link java.net.http.HttpClient}
 * come bean gestito da Spring, così da poter essere iniettato
 * nei vari service tramite dependency injection.
 *
 * Vantaggi di questa configurazione:
 * - Riutilizzo della stessa istanza di HttpClient
 * - Migliore gestione delle risorse
 * - Integrazione con il container Spring
 *
 * Esempio di utilizzo nei service:
 * {@code
 * @Service
 * public class MeteoService {
 *
 *     private final HttpClient client;
 *
 *     public MeteoService(HttpClient client) {
 *         this.client = client;
 *     }
 * }
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder().build();
    }

}
