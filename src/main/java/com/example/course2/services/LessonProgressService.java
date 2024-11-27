package com.example.course2.services;

import com.example.course2.models.Lesson;
import com.example.course2.models.LessonProgress;
import com.example.course2.models.User;
import com.example.course2.repositories.LessonProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LessonProgressService {

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    public Optional<LessonProgress> findByUserAndLesson(User user, Lesson lesson) {
        return lessonProgressRepository.findByUserAndLesson(user, lesson);
    }

    public void saveLessonProgress(LessonProgress lessonProgress) {
        lessonProgressRepository.save(lessonProgress);
    }

    public LessonProgress startOrUpdateLessonProgress(User user, Lesson lesson) {
        // Логика для создания или обновления прогресса
        return lessonProgressRepository.findByUserAndLesson(user, lesson)
                .orElseGet(() -> {
                    LessonProgress lessonProgress = new LessonProgress();
                    lessonProgress.setUser(user);    // Устанавливаем пользователя через сеттер
                    lessonProgress.setLesson(lesson); // Устанавливаем урок через сеттер
                    return lessonProgress;
                });
    }

    public boolean hasCompletedLesson(User user, Lesson lesson) {
        return lessonProgressRepository.findByUserAndLesson(user, lesson)
                .map(LessonProgress::isCompleted)
                .orElse(false);
    }
}
