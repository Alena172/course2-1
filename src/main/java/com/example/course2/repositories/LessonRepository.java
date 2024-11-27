package com.example.course2.repositories;

import com.example.course2.models.Course;
import com.example.course2.models.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourse(Course course);
}