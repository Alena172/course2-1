package com.example.course2.services;

import com.example.course2.models.Block;
import com.example.course2.models.BlockType;
import com.example.course2.models.Course;
import com.example.course2.models.Lesson;
import com.example.course2.repositories.BlockRepository;
import com.example.course2.repositories.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private BlockService blockService;

    // Метод для поиска урока по ID с использованием Optional
    public Lesson findById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }

    // Метод для поиска всех уроков, относящихся к определенному курсу
    public List<Lesson> findLessonsByCourse(Course course) {
        return lessonRepository.findByCourse(course);
    }

    // Метод для сохранения урока
    public Lesson saveLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }

    // Метод для удаления урока по ID
    public void deleteById(Long id) {
        lessonRepository.deleteById(id);
    }

    public void updateBlocks(Lesson lesson, List<String> blockTitle, List<String> blockType, List<String> blockContent) {
        List<Block> updatedBlocks = new ArrayList<>();
        for (int i = 0; i < blockTitle.size(); i++) {
            Block block = new Block();
            block.setTitle(blockTitle.get(i));
            block.setType(BlockType.valueOf(blockType.get(i)));
            block.setContent(blockContent.get(i));
            block.setLesson(lesson);
            updatedBlocks.add(block);
        }
        blockService.deleteBlocksByLesson(lesson); // Удаляем старые блоки
        blockService.saveAll(updatedBlocks); // Сохраняем новые
    }

    public void delete(Lesson lesson) {
        lessonRepository.delete(lesson);
    }

    // Метод для сохранения урока
    public Lesson save(Lesson lesson) {
        return lessonRepository.save(lesson);
    }

}
