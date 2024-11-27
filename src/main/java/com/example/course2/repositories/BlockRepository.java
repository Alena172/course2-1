package com.example.course2.repositories;

import com.example.course2.models.Block;
import com.example.course2.models.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {
    List<Block> findByLesson(Lesson lesson);
}