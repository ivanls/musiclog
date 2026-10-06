package com.ivan.musiclog.service;

import com.ivan.musiclog.dto.SpotifyAlbumResponse;
import com.ivan.musiclog.dto.SpotifyArtistResponse;
import com.ivan.musiclog.dto.SpotifySearchResponse;
import com.ivan.musiclog.dto.SpotifyTrackResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

        try {
            JsonNode json = objectMapper.readTree(response);
            return json.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener el access token de Spotify", e);
        }
    }

    // Searches Spotify and maps the results to application-specific DTOs.
    public SpotifySearchResponse search(String query) {

        String token = getAccessToken();

        // Request search results from Spotify.
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

        List<SpotifyArtistResponse> artistResponses = new ArrayList<>();
        List<SpotifyAlbumResponse> albumResponses = new ArrayList<>();
        List<SpotifyTrackResponse> trackResponses = new ArrayList<>();

        try {
            JsonNode json = objectMapper.readTree(response);

            // Map artist results to application DTOs.
            JsonNode artists = json.get("artists").get("items");

            for (JsonNode artist : artists) {
                String id = artist.get("id").asText();
                String name = artist.get("name").asText();
                String imageUrl = getImageUrl(artist.get("images"));
                String spotifyUrl = getSpotifyUrl(artist);

                SpotifyArtistResponse artistResponse =
                        new SpotifyArtistResponse(id, name, imageUrl, spotifyUrl);

                artistResponses.add(artistResponse);
            }

            // Map album results to application DTOs.
            JsonNode albums = json.get("albums").get("items");

            for (JsonNode album : albums) {
                String id = album.get("id").asText();
                String name = album.get("name").asText();

                String artistName = album
                        .get("artists")
                        .path(0)
                        .path("name")
                        .asText();

                String imageUrl = getImageUrl(album.get("images"));
                String spotifyUrl = getSpotifyUrl(album);

                SpotifyAlbumResponse albumResponse =
                        new SpotifyAlbumResponse(
                                id,
                                name,
                                artistName,
                                imageUrl,
                                spotifyUrl
                        );

                albumResponses.add(albumResponse);
            }

            // Map track results to application DTOs.
            JsonNode tracks = json.get("tracks").get("items");

            for (JsonNode track : tracks) {
                String id = track.get("id").asText();
                String name = track.get("name").asText();
                Integer durationMs = track.get("duration_ms").asInt();

                String artistName = track
                        .get("artists")
                        .path(0)
                        .path("name")
                        .asText();

                String albumName = track
                        .path("album")
                        .path("name")
                        .asText();

                String imageUrl = getImageUrl(
                        track.path("album").path("images")
                );

                String spotifyUrl = getSpotifyUrl(track);

                SpotifyTrackResponse trackResponse =
                        new SpotifyTrackResponse(
                                id,
                                name,
                                artistName,
                                albumName,
                                durationMs,
                                imageUrl,
                                spotifyUrl
                        );

                trackResponses.add(trackResponse);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener los datos", e);
        }

        return new SpotifySearchResponse(
                albumResponses,
                artistResponses,
                trackResponses
        );
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
}