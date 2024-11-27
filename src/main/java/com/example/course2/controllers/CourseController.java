package com.example.course2.controllers;

import com.example.course2.models.*;
import com.example.course2.repositories.UserRepository;
import com.example.course2.services.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class); // Логер для данного класса

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private LessonProgressService lessonProgressService;

    @Autowired
    private BlockService blockService;

    @GetMapping("/courses")
    public String getCourses(Model model,  Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        logger.info("User with ID {} is viewing course details.", user.getId());
        List<Course> courses = courseService.getAllCourses();
        if (courses.isEmpty()) {
            model.addAttribute("message", "Курсы не найдены.");
            logger.info("No courses found.");
        } else {
            logger.info("Fetched {} courses.", courses.size());
        }
        model.addAttribute("courses", courses);
        return "courses";
    }

    @GetMapping("/course-details/{id}")
    public String getCourseDetails(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseService.findCourseById(id);
            logger.info("Fetching details for course with ID: {}", id);
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        model.addAttribute("course", course);
        model.addAttribute("userId", user.getId());
            logger.info("User with ID {} is viewing course details.", user.getId());
        return "course-details";
    }


    @GetMapping("/myaccount/courses/{courseId}/lessons/{lessonId}")
    public String viewLessonDetails(@PathVariable Long courseId, @PathVariable Long lessonId, Principal principal, Model model) {
        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Lesson lesson = lessonService.findById(lessonId);
        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Lesson does not belong to the course");
        }
        LessonProgress progress = lessonProgressService.startOrUpdateLessonProgress(user, lesson);
        model.addAttribute("lesson", lesson);
        model.addAttribute("progress", progress);
        return "lesson-details";
    }


    @GetMapping("/myaccount/courses/{courseId}/lessons")
    public String viewCourseLessons(@PathVariable Long courseId, Principal principal, Model model) {
        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Course course = courseService.findCourseById(courseId);
        List<Lesson> lessons = lessonService.findLessonsByCourse(course);
        Map<Long, Boolean> lessonProgressMap = lessons.stream()
                .collect(Collectors.toMap(
                        lesson -> lesson.getId(),
                        lesson -> lessonProgressService.hasCompletedLesson(user, lesson)
                ));
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("lessonProgressMap", lessonProgressMap);
        return "course-lessons";
    }

    @PostMapping({"", "/{lessonId}"})
    public String saveLesson(
            @PathVariable Long courseId,
            @PathVariable(required = false) Long lessonId,
            @ModelAttribute Lesson lesson,
            @RequestParam(required = false) List<Block> blocks) {
        if (lessonId != null) {
            lesson.setId(lessonId);
            lessonService.save(lesson);
        } else {
            lessonService.save(lesson);
        }
        if (blocks != null && !blocks.isEmpty()) {
            blockService.saveAll(blocks);
        }
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }


    @GetMapping("/teacher/courses")
    public String getTeacherCourses(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.getRole() != Role.Teacher) {
            throw new RuntimeException("Access denied: User is not a teacher");
        }
        List<Course> courses = courseService.findByInstructor(user);
        model.addAttribute("courses", courses);
        return "teacher-courses";
    }

    @GetMapping("/teacher/courses/{courseId}/lessons")
    public String viewTeacherCourseLessons(@PathVariable Long courseId, Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.getRole() != Role.Teacher) {
            throw new RuntimeException("Доступ запрещен: Пользователь не является учителем");
        }
        Course course = courseService.findCourseById(courseId);
        if (!course.getInstructor().getId().equals(user.getId())) {
            throw new RuntimeException("Доступ запрещен: Курс не принадлежит этому учителю");
        }
        List<Lesson> lessons = lessonService.findLessonsByCourse(course);
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        return "teacher-course-lessons";
    }

    @GetMapping("/teacher/courses/{courseId}/lessons/new")
    public String showCreateLessonForm(@PathVariable Long courseId, Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Course course = courseService.findCourseById(courseId);
        if (!course.getInstructor().getId().equals(user.getId())) {
            throw new RuntimeException("Доступ запрещен: Курс не принадлежит этому учителю");
        }
        Lesson lesson = new Lesson();
        lesson.setBlocks(new ArrayList<>());

        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        return "create-lesson";
    }

    @GetMapping("/teacher/courses/{courseId}/lessons/{lessonId}/edit")
    public String showEditLessonForm(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            Principal principal,
            Model model) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Course course = courseService.findCourseById(courseId);
        if (course == null) {
            throw new EntityNotFoundException("Курс с ID " + courseId + " не найден");
        }
        Lesson lesson = lessonService.findById(lessonId);
        if (lesson == null || !lesson.getCourse().getId().equals(courseId)) {
            throw new EntityNotFoundException("Урок с ID " + lessonId + " не найден или не принадлежит указанному курсу");
        }
        if (!course.getInstructor().getId().equals(user.getId())) {
            throw new AccessDeniedException("Доступ запрещен");
        }
        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("blocks", lesson.getBlocks() != null ? lesson.getBlocks() : Collections.emptyList()); // Защита от null
        return "create-lesson";
    }


    @PostMapping("/teacher/courses/{courseId}/lessons/{lessonId}/delete")
    public String deleteLesson(@PathVariable Long courseId, @PathVariable Long lessonId, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Course course = courseService.findCourseById(courseId);
        Lesson lesson = lessonService.findById(lessonId);
        if (!course.getInstructor().getId().equals(user.getId()) || !lesson.getCourse().getId().equals(courseId)) {
            throw new RuntimeException("Доступ запрещен");
        }
        lessonService.delete(lesson);
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }


    @PostMapping("/teacher/courses/{courseId}/lessons/{lessonId}")
    public String updateLesson(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @ModelAttribute Lesson lesson,
            @RequestParam List<String> blockTitle,
            @RequestParam List<String> blockType,
            @RequestParam List<String> blockContent,
            Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Course course = courseService.findCourseById(courseId);
        if (!course.getInstructor().getId().equals(user.getId())) {
            throw new RuntimeException("Доступ запрещен");
        }

        Lesson existingLesson = lessonService.findById(lessonId);
        existingLesson.setTitle(lesson.getTitle());
        lessonService.updateBlocks(existingLesson, blockTitle, blockType, blockContent);

        lessonService.save(existingLesson);

        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }

    @PostMapping("/teacher/courses/{courseId}/lessons")
    public String createLesson(
            @PathVariable Long courseId,
            @ModelAttribute Lesson lesson,
            @RequestParam(required = false) List<String> blockTitle,
            @RequestParam(required = false) List<String> blockType,
            @RequestParam(required = false) List<String> blockContent,
            Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Course course = courseService.findCourseById(courseId);
        if (!course.getInstructor().getId().equals(user.getId())) {
            throw new RuntimeException("Доступ запрещен: Курс не принадлежит этому учителю");
        }
        lesson.setCourse(course);
        lesson = lessonService.saveLesson(lesson);
        if (blockType != null && !blockType.isEmpty()) {
            for (int i = 0; i < blockType.size(); i++) {
                Block block = new Block();
                block.setTitle(blockTitle != null ? blockTitle.get(i) : "");
                block.setType(BlockType.valueOf(blockType.get(i)));
                block.setContent(blockContent != null ? blockContent.get(i) : "");
                block.setLesson(lesson);
                blockService.saveBlock(block);
            }
        }
        return "redirect:/teacher/courses/" + courseId + "/lessons";
    }

    @PostMapping("/teacher/courses/{courseId}/lessons/{lessonId}/blocks/{blockId}")
    public String deleteBlock(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @PathVariable Long blockId) {
        Block block = blockService.findById(blockId);
        if (block == null) {
            throw new EntityNotFoundException("Block with ID " + blockId + " not found");
        }
        blockService.deleteBlock(blockId);
        return "redirect:/teacher/courses/" + courseId + "/lessons/" + lessonId + "/edit";
    }


    @DeleteMapping("/teacher/courses/{courseId}/lessons/{lessonId}/blocks/{blockId}")
    public String deleteBlock(@PathVariable Long courseId, @PathVariable Long lessonId, @PathVariable Long blockId, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Course course = courseService.findCourseById(courseId);
        if (!course.getInstructor().getId().equals(user.getId())) {
            throw new RuntimeException("Доступ запрещен: Курс не принадлежит этому учителю");
        }
        Block block = blockService.findById(blockId);
        if (block == null) {
            throw new RuntimeException("Блок не найден");
        }
        blockService.deleteBlock(blockId);
        return "redirect:/teacher/courses/" + courseId + "/lessons/" + lessonId;
    }

    @PostMapping("/myaccount/courses/{courseId}/lessons/{lessonId}/complete")
    public String markLessonAsCompleted(@PathVariable Long courseId, @PathVariable Long lessonId, Principal principal) {
        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Lesson lesson = lessonService.findById(lessonId);
        if (lesson == null || !lesson.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Invalid lesson or course");
        }
        LessonProgress lessonProgress = lessonProgressService.findByUserAndLesson(user, lesson)
                .orElseGet(() -> {
                    LessonProgress newLessonProgress = new LessonProgress();
                    newLessonProgress.setUser(user);
                    newLessonProgress.setLesson(lesson);
                    return newLessonProgress;
                });
        lessonProgressService.saveLessonProgress(lessonProgress);
        user.getCompletedLessons().add(lesson);
        userRepository.save(user);
        return "redirect:/myaccount/courses/{courseId}/lessons";
    }

}


