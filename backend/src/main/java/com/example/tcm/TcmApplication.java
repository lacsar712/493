package com.example.tcm;

import com.example.tcm.entity.User;
import com.example.tcm.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootApplication
public class TcmApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcmApplication.class, args);
    }

    @Bean
    public CommandLineRunner updatePasswords(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                List<User> users = userRepository.findAll();
                for (User user : users) {
                    // Check if password is plain text (BCrypt hashes are usually 60 chars)
                    if (user.getPassword() != null && user.getPassword().length() < 50) {
                        String newPassword = passwordEncoder.encode("123456");
                        user.setPassword(newPassword);
                        userRepository.save(user);
                        System.out.println("Updated password for user: " + user.getUsername());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to update passwords: " + e.getMessage());
            }
        };
    }

}
