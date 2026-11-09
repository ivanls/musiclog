
package com.ivan.musiclog;

import com.ivan.musiclog.dto.AlbumReviewResponse;
import com.ivan.musiclog.dto.UpdateAlbumReviewRequest;
import com.ivan.musiclog.entity.Album;
import com.ivan.musiclog.entity.AlbumListen;
import com.ivan.musiclog.entity.AlbumReview;
import com.ivan.musiclog.entity.User;
import com.ivan.musiclog.exception.AccessDeniedException;
import com.ivan.musiclog.exception.AlbumReviewNotFoundException;
import com.ivan.musiclog.repository.AlbumListenRepository;
import com.ivan.musiclog.repository.AlbumRepository;
import com.ivan.musiclog.repository.AlbumReviewRepository;
import com.ivan.musiclog.repository.ArtistRepository;
import com.ivan.musiclog.repository.UserRepository;
import com.ivan.musiclog.service.AlbumReviewService;
import com.ivan.musiclog.service.SpotifyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlbumReviewServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AlbumReviewRepository albumReviewRepository;

    @Mock
    private AlbumListenRepository albumListenRepository;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private SpotifyService spotifyService;

    @InjectMocks
    private AlbumReviewService albumReviewService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // Método auxiliar para crear una reseña asociada a AlbumListen.
    private AlbumReview createReview(User user, Long reviewId,
                                     Integer rating, String reviewText) {

        Album album = new Album();
        album.setSpotifyAlbumId("ABC");

        AlbumListen listen = new AlbumListen();
        listen.setUser(user);
        listen.setAlbum(album);
        listen.setListened(true);

        AlbumReview review = new AlbumReview();
        review.setId(reviewId);
        review.setAlbumListen(listen);
        review.setRating(rating);
        review.setReviewText(reviewText);

        return review;
    }

    // Verifica que se actualiza la puntuación y se conserva el texto original.
    @Test
    void updateAlbumReview_shouldUpdateRating() {

        User user = new User();
        user.setId(1L);
        user.setUsername("ivan");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        AlbumReview review = createReview(
                user, 10L, 6, "Mi reseña original"
        );

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        when(albumReviewRepository.save(any(AlbumReview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setRating(8);

        AlbumReviewResponse response =
                albumReviewService.updateAlbumReview(10L, request);

        assertEquals(8, response.getRating());
        assertEquals("Mi reseña original", response.getReviewText());

        verify(albumReviewRepository).save(any(AlbumReview.class));
    }

    // Verifica que un usuario no puede modificar una reseña ajena.
    @Test
    void updateAlbumReview_shouldThrowAccessDeniedException_whenUserIsNotOwner() {

        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user1));

        AlbumReview review = createReview(user2, 10L, 6, "Otra reseña");

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setRating(8);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> albumReviewService.updateAlbumReview(10L, request)
        );

        assertEquals("No puedes editar esta entrada", exception.getMessage());

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }

    // Verifica que se lanza una excepción cuando la reseña no existe.
    @Test
    void updateAlbumReview_shouldThrowAlbumReviewNotFoundException_whenReviewDoesNotExist() {

        User user = new User();
        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.empty());

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();

        AlbumReviewNotFoundException exception = assertThrows(
                AlbumReviewNotFoundException.class,
                () -> albumReviewService.updateAlbumReview(10L, request)
        );

        assertEquals("Review not found", exception.getMessage());

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }

    // Verifica que los campos omitidos conservan sus valores originales.
    @Test
    void updateAlbumReview_shouldPreserveFieldsNotIncludedInRequest() {

        User user = new User();
        user.setId(1L);
        user.setUsername("ivan");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        AlbumReview review = createReview(
                user, 10L, 7, "Buen disco"
        );

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        when(albumReviewRepository.save(any(AlbumReview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setRating(9);

        AlbumReviewResponse response =
                albumReviewService.updateAlbumReview(10L, request);

        assertEquals(9, response.getRating());
        assertEquals("Buen disco", response.getReviewText());
        assertTrue(response.isListened());
    }

    // Verifica que una puntuación superior a 10 provoca BAD_REQUEST.
    @Test
    void updateAlbumReview_shouldThrowBadRequest_whenRatingIsAboveMaximum() {

        User user = new User();
        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        AlbumReview review = createReview(user, 10L, 6, "Buen disco");

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setRating(11);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> albumReviewService.updateAlbumReview(10L, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "La puntuación debe estar entre 0 y 10",
                exception.getReason()
        );

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }

    // Verifica que una puntuación inferior a 0 provoca BAD_REQUEST.
    @Test
    void updateAlbumReview_shouldThrowBadRequest_whenRatingIsBelowMinimum() {

        User user = new User();
        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        AlbumReview review = createReview(user, 10L, 6, "Buen disco");

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setRating(-5);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> albumReviewService.updateAlbumReview(10L, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "La puntuación debe estar entre 0 y 10",
                exception.getReason()
        );

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }

    // Verifica que no se puede actualizar una reseña sin puntuación.
    @Test
    void updateAlbumReview_shouldThrowBadRequest_whenRatingIsMissing() {

        User user = new User();
        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        // La reseña original tampoco tiene puntuación.
        AlbumReview review = createReview(user, 10L, null, null);

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setReviewText("Buen disco");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> albumReviewService.updateAlbumReview(10L, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "No puedes publicar una reseña sin una puntuación",
                exception.getReason()
        );

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }
}
