package com.example.course2.repositories;

import com.example.course2.models.Course;
import com.example.course2.models.Enrollment;
import com.example.course2.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findAllByStudent(User student);
    Optional<Enrollment> findByStudentAndCourse(User student, Course course);
    // Получить запись студента на курс
    Optional<Enrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);

    // Получить все записи для курса
    List<Enrollment> findByCourse(Course course);

    // Проверить, есть ли запись на курс для студента
    boolean existsByCourseAndStudent(Course course, User student);

    // Проверить, есть ли запись на курс для студента по id
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}
