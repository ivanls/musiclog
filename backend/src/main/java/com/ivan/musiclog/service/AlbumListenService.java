package com.ivan.musiclog.service;

import com.ivan.musiclog.dto.AlbumListenResponse;
import com.ivan.musiclog.entity.Album;
import com.ivan.musiclog.entity.AlbumListen;
import com.ivan.musiclog.entity.User;
import com.ivan.musiclog.exception.AlbumListenNotFoundException;
import com.ivan.musiclog.repository.AlbumListenRepository;
import com.ivan.musiclog.repository.AlbumRepository;
import com.ivan.musiclog.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlbumListenService {
    private final AlbumListenRepository albumListenRepository;
    private final UserRepository userRepository;
    private final AlbumService albumService;

    public AlbumListenService(AlbumListenRepository albumListenRepository, UserRepository userRepository, AlbumService albumService) {
        this.albumListenRepository = albumListenRepository;
        this.userRepository = userRepository;
        this.albumService = albumService;
    }

    @Transactional
    public AlbumListenResponse setAsListened(String spotifyAlbumId) {

        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        Album album = albumService.findOrCreateAlbum(spotifyAlbumId);
        Optional<AlbumListen> albumListen = albumListenRepository.findByUser_IdAndAlbum_SpotifyAlbumId(user.getId(), spotifyAlbumId);

        AlbumListen listen;
        if (albumListen.isPresent()) {
            listen = albumListen.get();
        } else {
            listen = new AlbumListen();
            listen.setUser(user);
            listen.setAlbum(album);
        }
        listen.setListened(true);
        listen.setListenedAt(LocalDateTime.now());

        AlbumListen savedListen = albumListenRepository.save(listen);

        return toResponse(savedListen);
    }

    public AlbumListenResponse unlisten(String spotifyAlbumId) {

        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        Optional<AlbumListen> albumListen = albumListenRepository.findByUser_IdAndAlbum_SpotifyAlbumId(user.getId(), spotifyAlbumId);

        AlbumListen listen;
        if (albumListen.isPresent()) {
            listen = albumListen.get();
            listen.setListened(false);
        } else {
            throw new AlbumListenNotFoundException("Album Listen not found");
        }

        AlbumListen savedListen = albumListenRepository.save(listen);

        return toResponse(savedListen);
    }


    public List<AlbumListenResponse> getMyListens() {
        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        List<AlbumListen> albumListenList = albumListenRepository.findByUser_IdAndListened(user.getId(), true);
        List<AlbumListenResponse> responseList = albumListenList.stream()
                .map(this::toResponse)
                .toList();

        return responseList;
    }


    public AlbumListenResponse getListenBySpotifyAlbumId(String spotifyAlbumId) {
        String contextName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(contextName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        AlbumListen albumListen = albumListenRepository.findByUser_IdAndAlbum_SpotifyAlbumId(user.getId(), spotifyAlbumId)
                .orElseThrow(() ->
                        new AlbumListenNotFoundException("Album Listen not found"));

        return toResponse(albumListen);
    }

    private AlbumListenResponse toResponse(AlbumListen albumListen) {
        AlbumListenResponse response = new AlbumListenResponse(
                albumListen.getId(),
                albumListen.getAlbum().getSpotifyAlbumId(),
                albumListen.getAlbum().getName(),
                albumListen.getAlbum().getArtist().getName(),
                albumListen.getAlbum().getImageUrl(),
                albumListen.isListened(),
                albumListen.getListenedAt()
        );

        return response;
    }

}
