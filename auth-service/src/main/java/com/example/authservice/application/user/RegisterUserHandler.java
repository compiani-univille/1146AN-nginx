package com.example.authservice.application.user;

import com.example.authservice.application.port.PasswordHasher;
import com.example.authservice.domain.user.User;
import com.example.authservice.domain.user.UserRepository;
import com.example.authservice.domain.user.vo.Email;
import com.example.authservice.domain.user.vo.RoleType;
import com.example.authservice.interfaces.rest.dto.user.UserResponse;
import com.example.authservice.messaging.events.UserCreatedEvent;
import com.example.authservice.messaging.events.UserCreatedPublisher;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RegisterUserHandler {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final UserCreatedPublisher userCreatedPublisher;

    public UserResponse handle(String name, String emailRaw, String passwordRaw) {
        Email email = Email.of(emailRaw);

        if (userRepository.existsByEmail(email.getValue())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        String hashedPassword = passwordHasher.hash(passwordRaw);
        User user = new User(name, email, RoleType.CUSTOMER, hashedPassword);
        User savedUser = userRepository.save(user);

        userCreatedPublisher.publish(
            new UserCreatedEvent(
                savedUser.getId(),
                savedUser.getEmail().getValue(),
                savedUser.getName(),
                savedUser.getRole().getValue().name()
            )
        );

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail().getValue(),
                savedUser.getRole().getValue().name()
        );
    }
}
