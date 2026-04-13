package com.meteo.appMeteo.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.meteo.appMeteo.cache.CacheEntry;
import com.meteo.appMeteo.config.MeteoConfig;
import com.meteo.appMeteo.model.Meteo;
import com.meteo.formatter.Formatter;

/**
 * Service responsabile della logica applicativa per il recupero dei dati meteo.
 *
 * Questa classe gestisce:
 * - Validazione dell'input utente
 * - Chiamate alle API esterne (Geocoding + Meteo)
 * - Parsing delle risposte JSON
 * - Mapping verso il modello {@link com.meteo.appMeteo.model.Meteo}
 * - Gestione della cache in memoria (TTL: 1 ora)
 *
 * Flusso di esecuzione:
 * 1. Validazione città
 * 2. Controllo cache
 * 3. Geocoding → latitudine/longitudine
 * 4. Chiamata API meteo
 * 5. Costruzione oggetto Meteo
 * 6. Salvataggio in cache
 *
 * La cache utilizza {@link java.util.concurrent.ConcurrentHashMap}
 * per garantire thread-safety in ambienti multi-thread (es. applicazioni web).
 */
@Service
public class MeteoService {

    
    private final HttpClient client;

    public MeteoService(HttpClient client) {
        this.client = client;
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private static final String GEO_URL = MeteoConfig.get("geo.url");
    private static final String METEO_URL = MeteoConfig.get("meteo.url");

    /**
     * Recupera i dati meteo per una determinata città.
     *
     * @param citta Nome della città da cercare.
     *              Non può essere null, vuoto o contenere caratteri non validi.
     *
     * @return Un oggetto {@link Meteo} contenente:
     *         - nome città
     *         - temperatura
     *         - vento
     *         - descrizione
     *         - flag cache (true/false)
     *
     * @throws RuntimeException se:
     *         - input non valido
     *         - città non trovata
     *         - errore nelle API esterne
     */
    public Meteo getMeteoByCity(String citta) {

        try {
            validateInput(citta);

            String key = citta.trim().toLowerCase();

            CacheEntry entry = cache.get(key);

            if (entry != null) {
                if (entry.isValid()) {
                    Meteo cached = entry.getData();
                    cached.setCached(true);
                    return cached;
                } else {
                    cache.remove(key);
                }
            }

            double[] coords = getCoordinates(citta);

            JsonObject current = getMeteo(coords[0], coords[1]);

            Meteo result = buildResponse(citta, current);
            result.setCached(false);

            cache.put(key, new CacheEntry(result));

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Errore recupero meteo: " + e.getMessage());
        }
    }

    // =========================
    // VALIDAZIONE
    // =========================
    private void validateInput(String citta) {
        if (citta == null || citta.trim().isEmpty()) {
            throw new RuntimeException("Input città non valido");
        }

        if (!citta.matches("[a-zA-ZàèéìòùÀÈÉÌÒÙ\\s-]+")) {
            throw new RuntimeException("Formato città non valido");
        }
    }

    // =========================
    // GEOCODING
    // =========================
    private double[] getCoordinates(String citta) throws IOException, InterruptedException {

        String encodedCity = URLEncoder.encode(citta.trim(), StandardCharsets.UTF_8);
        String url = GEO_URL + "?name=" + encodedCity + "&count=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Errore API geocoding");
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray results = json.getAsJsonArray("results");

        if (results == null || results.isEmpty()) {
            throw new RuntimeException("Città non trovata");
        }

        JsonObject city = results.get(0).getAsJsonObject();

        return new double[]{
                city.get("latitude").getAsDouble(),
                city.get("longitude").getAsDouble()
        };
    }

    // =========================
    // METEO
    // =========================
    private JsonObject getMeteo(double lat, double lon) throws IOException, InterruptedException {

        String url = METEO_URL
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&current_weather=true";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Errore API meteo");
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        if (!json.has("current_weather")) {
            throw new RuntimeException("Dati meteo non disponibili");
        }

        return json.getAsJsonObject("current_weather");
    }

    // =========================
    // BUILD RESPONSE
    // =========================
    private Meteo buildResponse(String citta, JsonObject current) {

        double temperatura = current.get("temperature").getAsDouble();
        double vento = current.get("windspeed").getAsDouble();
        int code = current.get("weathercode").getAsInt();

        String descrizione = Formatter.weatherCodeToString(code);

        return new Meteo(citta, temperatura, vento, descrizione, false);
    }
}