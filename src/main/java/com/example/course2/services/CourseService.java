package com.example.course2.services;

import com.example.course2.controllers.AdminController;
import com.example.course2.models.Course;
import com.example.course2.models.Role;
import com.example.course2.models.User;
import com.example.course2.repositories.CourseRepository;
import com.example.course2.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    // Добавление нового курса
    public void addCourse(Course course) {
        // Устанавливаем преподавателя, если он указан в курсе
        if (course.getInstructor() != null) {
            User instructor = userRepository.findById(course.getInstructor().getId())
                    .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

            // Проверяем, является ли преподаватель учителем
            if (instructor.getRole() != Role.Teacher) {
                throw new RuntimeException("Пользователь не является преподавателем");
            }
            course.setInstructor(instructor);
        }

        courseRepository.save(course);
    }


    // Сохранение или обновление курса
    public void save(Course course) {
        // Проверяем, существует ли курс с данным id
        Course existingCourse = courseRepository.findById(course.getId())
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        // Обновляем поля существующего курса
        existingCourse.setTitle(course.getTitle());
        existingCourse.setDescription(course.getDescription());
        existingCourse.setPrice(course.getPrice());
        existingCourse.setCategory(course.getCategory());
        existingCourse.setDuration(course.getDuration());
        existingCourse.setStatus(course.getStatus());
        existingCourse.setImage(course.getImage());

        // Обновление инструктора, если изменился
        if (course.getInstructor() != null) {
            User instructor = userRepository.findById(course.getInstructor().getId())
                    .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));
            if (instructor.getRole() != Role.Teacher) {
                throw new RuntimeException("Пользователь не является преподавателем");
            }
            existingCourse.setInstructor(instructor);
        } else {
            existingCourse.setInstructor(null);  // Сбрасываем инструктора, если его нет в новом объекте
        }

        // Сохраняем обновленный курс
        courseRepository.save(existingCourse);
    }

    public boolean courseExists(Long id) {
        boolean exists = courseRepository.existsById(id);
                logger.info("Проверка существования курса с ID: {}: {}", id, exists);
        return exists;
        }
//        return courseRepository.existsById(id);




    public List<Course> getAllCourses() {
        return courseRepository.findAll();  // Используем метод findAll() для получения всех курсов
    }

    // Получение списка всех курсов
    public List<Course> findAll() {
        Iterable<Course> allCourses = courseRepository.findAll();
        List<Course> courses = new ArrayList<>();
        allCourses.forEach(courses::add);
        return courses;
    }

    // Поиск курса по ID
    public Course findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));
    }

    // Удаление курса
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        // Удаляем курс
        courseRepository.delete(course);
    }

    // Получение списка преподавателей для назначения на курс
    public List<User> findTeachers() {
        return userRepository.findByRole(Role.Teacher);
    }

    public List<Course> findByInstructor(User instructor) {
        return courseRepository.findByInstructor(instructor);
    }
}
