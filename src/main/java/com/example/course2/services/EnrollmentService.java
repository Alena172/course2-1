package com.example.course2.services;

import com.example.course2.models.*;
import com.example.course2.repositories.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private LessonProgressService lessonProgressService;

    public Optional<Enrollment> findEnrollment(User student, Course course) {
        return enrollmentRepository.findByStudentAndCourse(student, course);
    }

    public Enrollment enrollUserInCourse(User student, Course course) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(Status.ACTIVE);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setProgress(0);
        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> findEnrollmentsByStudent(User student) {
        return enrollmentRepository.findAllByStudent(student);
    }

    public double calculateCourseCompletion(User user, Course course) {
        // Получаем все уроки курса
        List<Lesson> lessons = lessonService.findLessonsByCourse(course);

        // Считаем количество завершённых уроков для данного пользователя
        long completedLessonsCount = lessons.stream()
                .filter(lesson -> lessonProgressService.hasCompletedLesson(user, lesson))
                .count();

        // Вычисляем процент завершённых уроков
        double completionPercentage = 0;
        if (lessons.size() > 0) {
            completionPercentage = (double) completedLessonsCount / lessons.size() * 100;
        }

        return completionPercentage;
    }
}
