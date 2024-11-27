package com.example.course2.services;

import com.example.course2.models.Role;
import com.example.course2.models.User;
import com.example.course2.repositories.UserRepository;
import com.example.course2.repositories.CourseRepository;  // Импортируем репозиторий курсов
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;  // Добавляем репозиторий курсов

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void addUser(User user) {
        if (user.getRole() == null) {
            user.setRole(Role.Student);
        }

        if (!userRepository.existsByEmail(user.getEmail())) {  // Проверка на существование email
            user.setCreatedAt(LocalDateTime.now());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
        }
    }

    public void save(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Обновляем поля пользователя, кроме createdAt
        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());

        // Обновляем роль пользователя, если нужно
        existingUser.setRole(user.getRole());

        // Не обновляем поле createdAt
        // Сохраняем пользователя в базу данных
        userRepository.save(existingUser);
    }

    public List<User> findTeachers() {
        return userRepository.findByRole(Role.Teacher); // Получаем пользователей с ролью "Teacher"
    }

    public List<User> findAll() {
        Iterable<User> allUsers = userRepository.findAll();
        List<User> users = new ArrayList<>();
        for (User user : allUsers) {
            users.add(user);
        }
        return users;
    }

    public User findUserById(Long id) {
        return userRepository.findUserById(id);
    }

    public void changePassword(User user, String newPassword) {
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    public User findByName(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    // Удаление пользователя по ID
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Если это учитель, удаляем его из всех курсов
        if (user.getRole() == Role.Teacher) {
            // Сбрасываем все курсы, связанные с этим учителем
            courseRepository.updateInstructorToNull(user);  // Метод для сброса инструктора у курсов
        }

        // Удаляем пользователя
        userRepository.deleteById(id);
    }
}
