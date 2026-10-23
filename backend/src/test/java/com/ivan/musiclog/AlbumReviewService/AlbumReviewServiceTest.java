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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void updateAlbumReview_shouldThrowAccessDeniedException_whenUserIsNotOwner(){
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

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, ()->{
            albumReviewService.updateAlbumReview(albumReview.getId(), request);
        });

        assertEquals("No puedes editar esta entrada", exception.getMessage());

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }

    @Test
    void updateAlbumReview_shouldThrowAlbumReviewNotFoundException_whenReviewDoesNotExist(){

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

        AlbumReviewNotFoundException exception = assertThrows(AlbumReviewNotFoundException.class, ()->{
            albumReviewService.updateAlbumReview(10L, request);
        });

        assertEquals("Review not found", exception.getMessage());

        verify(albumReviewRepository, never()).save(any(AlbumReview.class));
    }

}

