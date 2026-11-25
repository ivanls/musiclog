package com.ivan.musiclog;

import com.ivan.musiclog.dto.AlbumListenResponse;
import com.ivan.musiclog.entity.Album;
import com.ivan.musiclog.entity.AlbumListen;
import com.ivan.musiclog.entity.Artist;
import com.ivan.musiclog.entity.User;
import com.ivan.musiclog.repository.AlbumListenRepository;
import com.ivan.musiclog.repository.UserRepository;
import com.ivan.musiclog.service.AlbumListenService;
import com.ivan.musiclog.service.AlbumService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AlbumListenServiceTest {

    @Mock
    private AlbumListenRepository albumListenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AlbumService albumService;

    @InjectMocks
    private AlbumListenService albumListenService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setUpSecurityContext(){
        User user = new User();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );
    }


    @Test
    void setAsListened() {
        setUpSecurityContext();

        // Usuario autenticado
        User user = new User();
        user.setId(1L);
        user.setUsername("ivan");

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        // Artista y álbum
        Artist artist = new Artist();
        artist.setName("Pink Floyd");

        Album album = new Album();
        album.setSpotifyAlbumId("123");
        album.setName("The Dark Side of the Moon");
        album.setArtist(artist);

        when(albumService.findOrCreateAlbum("123"))
                .thenReturn(album);

        // No existe un registro previo
        when(albumListenRepository
                .findByUser_IdAndAlbum_SpotifyAlbumId(1L, "123"))
                .thenReturn(Optional.empty());

        // Configuramos el guardado
        when(albumListenRepository.save(any(AlbumListen.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutamos el método
        AlbumListenResponse response =
                albumListenService.setAsListened("123");

        // Comprobamos el resultado
        assertNotNull(response);
        assertEquals("123", response.getSpotifyAlbumId());
        assertTrue(response.isListened());

        // Comprobamos que se ha guardado
        verify(albumListenRepository, times(1))
                .save(any(AlbumListen.class));
    }


}
