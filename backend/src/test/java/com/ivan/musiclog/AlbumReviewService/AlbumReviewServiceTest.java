package com.ivan.musiclog.AlbumReviewService;

import com.ivan.musiclog.dto.AlbumReviewResponse;
import com.ivan.musiclog.dto.UpdateAlbumReviewRequest;
import com.ivan.musiclog.entity.Album;
import com.ivan.musiclog.entity.AlbumReview;
import com.ivan.musiclog.entity.User;
import com.ivan.musiclog.exception.AccessDeniedException;
import com.ivan.musiclog.exception.AlbumReviewNotFoundException;
import com.ivan.musiclog.repository.AlbumReviewRepository;
import com.ivan.musiclog.repository.UserRepository;
import com.ivan.musiclog.service.AlbumReviewService;
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

    @InjectMocks
    private AlbumReviewService albumReviewService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // Verifica que se actualiza la puntuación de una reseña y se conserva
    // el texto original cuando no se incluye en la petición.
    @Test
    void updateAlbumReview_shouldUpdateRating() {

        // 1. PREPARAR EL USUARIO
        // Creamos un usuario ficticio, sin consultar la base de datos.
        User user = new User();
        user.setId(1L);

        // Simulamos que el usuario autenticado se llama "ivan".
        // El servicio obtendrá este nombre mediante SecurityContextHolder.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        // Cuando el servicio busque a "ivan", el repositorio simulado
        // devolverá nuestro usuario ficticio.
        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));


        // 2. PREPARAR LA REVIEW Y SU ÁLBUM
        // Creamos un álbum ficticio con el identificador que necesita el DTO.
        Album album = new Album();
        album.setSpotifyAlbumId("abc");

        // Creamos una review que pertenece al usuario anterior.
        AlbumReview review = new AlbumReview();
        review.setId(10L);
        review.setUser(user);
        review.setAlbum(album);

        // Establecemos los datos originales de la review.
        // La puntuación inicial es 6 y el texto ya está guardado.
        review.setRating(6);
        review.setReviewText("Mi reseña original");


        // 3. CONFIGURAR EL REPOSITORIO DE REVIEWS
        // Cuando el servicio busque la review con ID 10,
        // el repositorio simulado devolverá nuestra review.
        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        // Simulamos el guardado devolviendo el mismo objeto recibido.
        // Sin esta configuración, el mock devolvería null por defecto.
        when(albumReviewRepository.save(any(AlbumReview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        // 4. PREPARAR LA PETICIÓN PATCH
        // Solo enviamos una nueva puntuación.
        // Los campos omitidos permanecen en null y no deben modificarse.
        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setRating(8);


        // 5. EJECUTAR EL MÉTODO QUE QUEREMOS PROBAR
        AlbumReviewResponse response =
                albumReviewService.updateAlbumReview(10L, request);


        // 6. COMPROBAR EL RESULTADO
        // La puntuación debe haber cambiado de 6 a 8.
        assertEquals(8, response.getRating());

        // El texto original debe conservarse porque no lo enviamos en el PATCH.
        assertEquals("Mi reseña original", response.getReviewText());

        // Comprobamos que el servicio llamó al repositorio para guardar la review.
        verify(albumReviewRepository).save(any(AlbumReview.class));
    }

    // Verifica que un usuario no puede modificar una reseña que pertenece
    // a otro usuario y que no se guarda ningún cambio.
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

        AlbumReview albumReview = new AlbumReview();
        albumReview.setId(10L);
        albumReview.setUser(user2);

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(albumReview));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setRating(8);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            albumReviewService.updateAlbumReview(albumReview.getId(), request);
        });

        assertEquals("No puedes editar esta entrada", exception.getMessage());

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }

    // Verifica que se lanza una excepción cuando la reseña no existe
    // y que no se intenta guardar ninguna modificación.
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

        AlbumReviewNotFoundException exception = assertThrows(AlbumReviewNotFoundException.class, () -> {
            albumReviewService.updateAlbumReview(10L, request);
        });

        assertEquals("Review not found", exception.getMessage());

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }

    // Verifica que el campo listened puede actualizarse de true a false.
    @Test
    void updateAlbumReview_shouldUpdateListenedToFalse() {
        User user = new User();
        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        Album album = new Album();
        album.setSpotifyAlbumId("ABC");

        AlbumReview albumReview = new AlbumReview();
        albumReview.setUser(user);
        albumReview.setAlbum(album);
        albumReview.setListened(true);
        albumReview.setId(10L);

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(albumReview));

        when(albumReviewRepository.save(any(AlbumReview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setListened(false);

        AlbumReviewResponse response = albumReviewService.updateAlbumReview(10L, request);

        assertFalse(response.isListened());
    }

    // Verifica que los campos omitidos en la petición conservan sus valores
    // originales durante una actualización parcial.
    @Test
    void updateAlbumReview_shouldPreserveFieldsNotIncludedInRequest() {

        User user = new User();
        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        Album album = new Album();
        album.setSpotifyAlbumId("ABC");

        AlbumReview review = new AlbumReview();
        review.setId(10L);
        review.setAlbum(album);
        review.setUser(user);
        review.setListened(true);
        review.setRating(7);
        review.setReviewText("Buen disco");

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        when(albumReviewRepository.save(any(AlbumReview.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setRating(9);

        AlbumReviewResponse response = albumReviewService.updateAlbumReview(10L, request);

        assertEquals(9, response.getRating());
        assertEquals("Buen disco", response.getReviewText());
        assertTrue(response.isListened());
    }

    // Verifica que una puntuación superior a 10 provoca un error BAD_REQUEST
    // y que la reseña no se guarda.
    @Test
    void updateAlbumReview_shouldThrowBadRequest_whenRatingIsAboveMaximum() {
        User user = new User();
        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        Album album = new Album();
        album.setSpotifyAlbumId("ABC");

        AlbumReview review = new AlbumReview();
        review.setId(10L);
        review.setUser(user);
        review.setAlbum(album);

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setRating(11);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> albumReviewService.updateAlbumReview(10L, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("La puntuación debe estar entre 0 y 10", exception.getReason());

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }

    // Verifica que una puntuación inferior a 0 provoca un error BAD_REQUEST
    // y que la reseña no se guarda.
    @Test
    void updateAlbumReview_shouldThrowBadRequest_whenRatingIsBelowMinimum() {
        User user = new User();
        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        Album album = new Album();
        album.setSpotifyAlbumId("ABC");

        AlbumReview review = new AlbumReview();
        review.setId(10L);
        review.setUser(user);
        review.setAlbum(album);

        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setRating(-5);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> albumReviewService.updateAlbumReview(10L, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("La puntuación debe estar entre 0 y 10", exception.getReason());

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }

    // Verifica que enviar texto de reseña sin puntuación provoca un error
    // BAD_REQUEST y que la reseña no se guarda.
    @Test
    void updateAlbumReview_shouldThrowBadRequest_whenReviewTextProvidedWithoutRating() {
        User user = new User();
        user.setId(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ivan", "password")
        );

        when(userRepository.findByUsername("ivan"))
                .thenReturn(Optional.of(user));

        Album album = new Album();
        album.setSpotifyAlbumId("ABC");

        AlbumReview review = new AlbumReview();
        review.setId(10L);
        review.setUser(user);
        review.setAlbum(album);


        when(albumReviewRepository.findById(10L))
                .thenReturn(Optional.of(review));

        UpdateAlbumReviewRequest request = new UpdateAlbumReviewRequest();
        request.setReviewText("Buen disco");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> albumReviewService.updateAlbumReview(10L, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("No puedes publicar una reseña sin una puntuación", exception.getReason());

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }
}

