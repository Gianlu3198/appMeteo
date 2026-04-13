# Meteo App

## Descrizione del progetto

Meteo App è un'applicazione web composta da un backend sviluppato in Java con Spring Boot e un frontend semplice in HTML, CSS e JavaScript.  
L'app consente all'utente di inserire il nome di una città e ottenere in tempo reale le informazioni meteorologiche correnti.

Il sistema utilizza le API di Open-Meteo per:
- ottenere le coordinate geografiche della città (geocoding)
- recuperare i dati meteo aggiornati

## Funzionalità principali

- Ricerca meteo per città
- Visualizzazione temperatura
- Visualizzazione velocità del vento
- Descrizione testuale delle condizioni meteo
- Gestione errori (input non valido, città non trovata, errori API)
- Sistema di cache lato backend (durata 1 ora)

## Architettura

### Backend (Spring Boot)

Struttura principale:

- controller
  - MeteoController
- service
  - MeteoService
- model
  - Meteo
- config
  - MeteoConfig
  - HttpClientConfig
- cache
  - CacheEntry

#### MeteoController
Espone l'endpoint REST:

GET /meteo/{citta}

Riceve il nome della città e restituisce un oggetto Meteo in formato JSON.

#### MeteoService
Gestisce tutta la logica applicativa:
- validazione input
- chiamate HTTP alle API esterne
- parsing JSON
- gestione cache
- costruzione risposta finale

Flusso:
1. Validazione input
2. Controllo cache
3. Chiamata geocoding
4. Chiamata meteo
5. Creazione oggetto Meteo
6. Salvataggio in cache

#### Meteo (model)
Oggetto dati restituito al frontend:
- citta
- temperatura
- vento
- descrizione
- cached (boolean)

#### MeteoConfig
Carica le proprietà da application.properties:
- geo.url
- meteo.url

#### CacheEntry
Gestisce la cache in memoria:
- salva i dati meteo
- mantiene timestamp
- validità: 1 ora

#### HttpClientConfig
Configura un bean HttpClient utilizzato per le chiamate HTTP.

---

### Frontend

Frontend minimale sviluppato senza framework.

Tecnologie:
- HTML
- CSS
- JavaScript (fetch API)

Funzionamento:
- input per inserire la città
- bottone per avviare la ricerca
- chiamata HTTP al backend
- visualizzazione dei dati ricevuti

Esempio funzione JavaScript:

- invia richiesta a:
  http://localhost:8080/meteo/{citta}
- riceve JSON
- aggiorna il DOM con i dati

Gestione errori:
- controllo response.ok
- gestione campo errore restituito dal backend
- fallback con messaggio generico

---

## API utilizzate

Open-Meteo:
- Geocoding API
- Weather API

---

## Avvio del progetto

### Backend

1. Avviare l'applicazione Spring Boot
2. Server disponibile su:
   http://localhost:8080

### Frontend

1. Aprire il file index.html nel browser
2. Oppure usare Live Server su VS Code

---

## Esempio di utilizzo

Input:
Milano

Output:
- Temperatura: 22.5°C
- Vento: 10 km/h
- Descrizione: Cielo sereno

---

## Gestione errori

Il backend genera errori nei seguenti casi:
- input vuoto → "Input città non valido"
- formato errato → "Formato città non valido"
- città non trovata → "Città non trovata"
- errore API → "Errore API geocoding" / "Errore API meteo"

Il frontend intercetta:
- errori HTTP
- risposta non valida
- campi undefined

---

## Possibili miglioramenti

- UI più avanzata (CSS, icone meteo)
- storico ricerche
- supporto geolocalizzazione
- test automatizzati più completi
- gestione errori con codici HTTP più precisi
- integrazione con framework frontend (Angular, React)

---

## Autore

Gianluca Coccimiglio
