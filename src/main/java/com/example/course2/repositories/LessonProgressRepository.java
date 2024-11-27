package com.example.course2.repositories;

import com.example.course2.models.Lesson;
import com.example.course2.models.LessonProgress;
import com.example.course2.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUserAndLesson(User user, Lesson lesson);
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
}