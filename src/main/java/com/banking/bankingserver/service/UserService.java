package com.banking.bankingserver.service;

import com.banking.bankingserver.dto.UserDTO;
import com.banking.bankingserver.entity.User;
import com.banking.bankingserver.event.EventType;
import com.banking.bankingserver.event.NotificationEvent;
import com.banking.bankingserver.kafka.EventProducer;
import com.banking.bankingserver.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventProducer eventProducer;

    public UserDTO registerUser(String username, String email, String password,
                               String firstName, String lastName, String phoneNumber) {
        if (userRepository.findByUsernameOrEmail(username, email).isPresent()) {
            throw new IllegalArgumentException("Username or email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .status("ACTIVE")
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: {}", username);

        sendRegistrationEvent(saved);
        return convertToDTO(saved);
    }

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public UserDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO updateUser(Long id, UserDTO updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (updates.getEmail() != null) user.setEmail(updates.getEmail());
        if (updates.getPhoneNumber() != null) user.setPhoneNumber(updates.getPhoneNumber());
        if (updates.getFirstName() != null) user.setFirstName(updates.getFirstName());
        if (updates.getLastName() != null) user.setLastName(updates.getLastName());

        User saved = userRepository.save(user);
        log.info("User updated: {}", id);

        sendUpdateEvent(saved);
        return convertToDTO(saved);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }

    private void sendUpdateEvent(User user) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(EventType.USER_UPDATED.name())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .timestamp(LocalDateTime.now())
                .build();

        eventProducer.sendNotificationEvent(event);
    }

    private void sendRegistrationEvent(User user) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType(EventType.USER_REGISTERED.name())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .timestamp(LocalDateTime.now())
                .build();

        eventProducer.sendNotificationEvent(event);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt().toString())
                .updatedAt(user.getUpdatedAt().toString())
                .build();
    }
}
