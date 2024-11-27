package com.example.course2.services;

import com.example.course2.models.Block;
import com.example.course2.models.Lesson;
import com.example.course2.repositories.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class BlockService {

    @Autowired
    private BlockRepository blockRepository;

    public Block saveBlock(Block block) {
        return blockRepository.save(block);
    }

    public List<Block> findBlocksByLesson(Lesson lesson) {
        return blockRepository.findByLesson(lesson);
    }

    public List<Block> saveAll(List<Block> blocks) {
        return blockRepository.saveAll(blocks);
    }

    public Block findById(Long id) {
        return blockRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Block not found with id: " + id));
    }

    public void deleteBlock(Long id) {
        blockRepository.deleteById(id);
    }

    // New method to delete blocks by lesson
    public void deleteBlocksByLesson(Lesson lesson) {
        List<Block> blocks = blockRepository.findByLesson(lesson);
        if (!blocks.isEmpty()) {
            blockRepository.deleteAll(blocks);
        }
    }
}
