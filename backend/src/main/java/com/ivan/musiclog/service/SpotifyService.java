package com.ivan.musiclog.service;

import com.ivan.musiclog.dto.SpotifyAlbumResponse;
import com.ivan.musiclog.dto.SpotifyArtistResponse;
import com.ivan.musiclog.dto.SpotifySearchResponse;
import com.ivan.musiclog.dto.SpotifyTrackResponse;
import com.ivan.musiclog.exception.SpotifyNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyService {

    private final WebClient spotifyWebClient;
    private final ObjectMapper objectMapper;

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    public SpotifyService(WebClient spotifyWebClient, ObjectMapper objectMapper) {
        this.spotifyWebClient = spotifyWebClient;
        this.objectMapper = objectMapper;
    }

    // Retrieves an access token using Spotify's Client Credentials flow.
    public String getAccessToken() {

        String response = spotifyWebClient
                .post()
                .uri("https://accounts.spotify.com/api/token")
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response == null || response.isBlank()){
            throw new RuntimeException("La respuesta de Spotify para obtener el token está vacía");
        }

        JsonNode json;
        try {
            json = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear la respuesta de Spotify", e);
        }

        if (!json.hasNonNull("access_token") || json.path("access_token").asText("").isBlank()){
            throw new RuntimeException("No se ha recibido un access_token válido de Spotify");
        }

        return json.path("access_token").asText();
    }

    // Searches Spotify and maps the results to application-specific DTOs.
    // Searches Spotify and maps the results to application-specific DTOs.
    public SpotifySearchResponse search(String query) {

        if (query == null || query.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Debes indicar un término de búsqueda"
            );
        }

        String token = getAccessToken();

        String response = spotifyWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("q", query)
                        .queryParam("type", "artist,album,track")
                        .queryParam("market", "ES")
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response == null || response.isBlank()) {
            throw new RuntimeException("Respuesta vacía al buscar en Spotify");
        }

        JsonNode json = parseJson(response);

        List<SpotifyArtistResponse> artistResponses = mapArtists(json);
        List<SpotifyAlbumResponse> albumResponses = mapAlbums(json);
        List<SpotifyTrackResponse> trackResponses = mapTracks(json);

        return new SpotifySearchResponse(albumResponses, artistResponses, trackResponses);
    }

    // Extracts the first available image URL from a Spotify images array.
    private String getImageUrl(JsonNode images) {
        if (images != null && !images.isEmpty()) {
            return images.get(0).path("url").asText(null);
        }

        return null;
    }

    // Extracts the Spotify URL from a Spotify resource.
    private String getSpotifyUrl(JsonNode node) {
        return node
                .path("external_urls")
                .path("spotify")
                .asText(null);
    }

    // Get Artist by ID
    public SpotifyArtistResponse getArtistById(String id) {

        String token = getAccessToken();

        String response = spotifyWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/artists/{id}")
                        .build(id))
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        clientResponse -> Mono.error(
                                new SpotifyNotFoundException("Artista no encontrado en Spotify")
                        )
                )
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode json = objectMapper.readTree(response);

            String artistId = json.get("id").asText();
            String name = json.get("name").asText();
            String imageUrl = getImageUrl(json.get("images"));
            String spotifyUrl = getSpotifyUrl(json);

            return new SpotifyArtistResponse(
                    artistId,
                    name,
                    imageUrl,
                    spotifyUrl
            );

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener los datos del artista", e);
        }
    }

    // Get Album by ID
    public SpotifyAlbumResponse getAlbumById(String id) {

        String token = getAccessToken();

        String response = spotifyWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/albums/{id}")
                        .build(id))
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        clientResponse -> Mono.error(
                                new SpotifyNotFoundException("Álbum no encontrado en Spotify")
                        )
                )
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode json = objectMapper.readTree(response);

            String albumId = json.get("id").asText();
            String name = json.get("name").asText();

            JsonNode artistsArray = json.path("artists");
            String artistName = "";
            String artistId = "";

            if (artistsArray.isArray() && !artistsArray.isEmpty()) {
                JsonNode firstArtist = artistsArray.path(0);
                artistName = firstArtist.path("name").asText("");
                artistId = firstArtist.path("id").asText("");
            }

            String imageUrl = getImageUrl(json.get("images"));
            String spotifyUrl = getSpotifyUrl(json);

            return new SpotifyAlbumResponse(
                    albumId,
                    name,
                    artistName,
                    imageUrl,
                    spotifyUrl,
                    artistId
            );

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener los datos del álbum", e);
        }
    }

    // Get Track by ID
    public SpotifyTrackResponse getTrackById(String id) {

        String token = getAccessToken();

        String response = spotifyWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/tracks/{id}")
                        .build(id))
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        clientResponse -> Mono.error(
                                new SpotifyNotFoundException("Pista no encontrada en Spotify")
                        )
                )
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode json = objectMapper.readTree(response);

            String trackId = json.get("id").asText();
            String name = json.get("name").asText();

            JsonNode artistsArray = json.path("artists");
            String artistName = "";
            if (artistsArray.isArray() && !artistsArray.isEmpty()) {
                artistName = artistsArray.path(0).path("name").asText("");
            }

            String albumName = json.path("album").path("name").asText("");
            Integer durationMs = json.path("duration_ms").asInt(0);
            String imageUrl = getImageUrl(json.path("album").path("images"));
            String spotifyUrl = getSpotifyUrl(json);

            return new SpotifyTrackResponse(
                    trackId,
                    name,
                    artistName,
                    albumName,
                    durationMs,
                    imageUrl,
                    spotifyUrl
            );

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener los datos de la canción", e);
        }
    }

    private JsonNode parseJson(String response) {
        try {
            return objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear la respuesta de Spotify", e);
        }
    }

    private List<SpotifyArtistResponse> mapArtists(JsonNode json) {
        List<SpotifyArtistResponse> artistResponses = new ArrayList<>();

        JsonNode artistsNode = json.path("artists");
        JsonNode items = artistsNode.path("items");

        if (items == null || !items.isArray() || items.isEmpty()) {
            return artistResponses;
        }

        for (JsonNode artist : items) {
            String id = artist.path("id").asText();
            String name = artist.path("name").asText();
            String imageUrl = getImageUrl(artist.path("images"));
            String spotifyUrl = getSpotifyUrl(artist);

            artistResponses.add(new SpotifyArtistResponse(id, name, imageUrl, spotifyUrl));
        }

        return artistResponses;
    }

    private List<SpotifyAlbumResponse> mapAlbums(JsonNode json) {
        List<SpotifyAlbumResponse> albumResponses = new ArrayList<>();

        JsonNode albumsNode = json.path("albums");
        JsonNode items = albumsNode.path("items");

        if (items == null || !items.isArray() || items.isEmpty()) {
            return albumResponses;
        }

        for (JsonNode album : items) {
            String id = album.path("id").asText();
            String name = album.path("name").asText();

            JsonNode artistsArray = album.path("artists");
            String artistName = "";
            String artistId = "";

            if (artistsArray.isArray() && !artistsArray.isEmpty()) {
                JsonNode firstArtist = artistsArray.path(0);
                artistName = firstArtist.path("name").asText("");
                artistId = firstArtist.path("id").asText("");
            }

            String imageUrl = getImageUrl(album.path("images"));
            String spotifyUrl = getSpotifyUrl(album);

            albumResponses.add(new SpotifyAlbumResponse(id, name, artistName, imageUrl, spotifyUrl, artistId));
        }

        return albumResponses;
    }

    private List<SpotifyTrackResponse> mapTracks(JsonNode json) {
        List<SpotifyTrackResponse> trackResponses = new ArrayList<>();

        JsonNode tracksNode = json.path("tracks");
        JsonNode items = tracksNode.path("items");

        if (items == null || !items.isArray() || items.isEmpty()) {
            return trackResponses;
        }

        for (JsonNode track : items) {
            String id = track.path("id").asText();
            String name = track.path("name").asText();
            Integer durationMs = track.path("duration_ms").asInt(0);

            JsonNode artistsArray = track.path("artists");
            String artistName = "";
            if (artistsArray.isArray() && !artistsArray.isEmpty()) {
                artistName = artistsArray.path(0).path("name").asText("");
            }

            String albumName = track.path("album").path("name").asText("");
            String imageUrl = getImageUrl(track.path("album").path("images"));
            String spotifyUrl = getSpotifyUrl(track);

            trackResponses.add(new SpotifyTrackResponse(id, name, artistName, albumName, durationMs, imageUrl, spotifyUrl));
        }

        return trackResponses;
    }
}