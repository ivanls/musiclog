package com.ivan.musiclog;

import com.ivan.musiclog.dto.RegisterRequest;
import com.ivan.musiclog.dto.RegisterResponse;
import com.ivan.musiclog.entity.User;
import com.ivan.musiclog.repository.UserRepository;
import com.ivan.musiclog.service.AuthService;
import com.ivan.musiclog.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldCreateUserWhenDataIsValid() {

        // Arrange: preparamos la petición
        RegisterRequest request = new RegisterRequest("ivan", "ivan123@gmail.com", "admin123");

        // Simulamos que el usuario y el email no existen
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // Simulamos la codificación de la contraseña
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword123");

        // Simulamos el usuario que devuelve userRepository.save()
        User savedUserMock = new User();
        savedUserMock.setId(1L);
        savedUserMock.setUsername("ivan");
        savedUserMock.setEmail("ivan123@gmail.com");
        savedUserMock.setPassword("encodedPassword123");

        when(userRepository.save(any(User.class))).thenReturn(savedUserMock);

        // Act: ejecutamos el registro
        RegisterResponse response = authService.register(request);

        // Assert: comprobamos que se devuelve una respuesta
        assertNotNull(response);

        // Verify: capturamos el usuario enviado al repositorio
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();

        // Comprobamos los datos del usuario guardado
        assertEquals("ivan", savedUser.getUsername());
        assertEquals("ivan123@gmail.com", savedUser.getEmail());
        assertEquals("encodedPassword123", savedUser.getPassword());

        // Comprobamos que la contraseña original no se guarda
        assertNotEquals("admin123", savedUser.getPassword());
    }

    void registerShouldThrowExceptionWhenUsernameAlreadyExists() {

    }

    void registerShouldThrowExceptionWhenEmailAlreadyExists() {

    }

    void loginShouldReturnTokenWhenAuthenticationSucceeds() {

    }

    void loginShouldPropagateExceptionWhenBadCredentials() {

    }
}

