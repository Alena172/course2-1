package com.example.course2.controllers;

import com.example.course2.models.Course;
import com.example.course2.services.CourseService;
import com.example.course2.services.UserService;
import com.example.course2.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CourseService courseService;
    @Autowired
    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/create-or-edit-user")
    public String createOrEditUser(@RequestParam(value = "id", required = false) Long id, Model model) {
        User user = id != null ? userService.findUserById(id) : new User();
        model.addAttribute("user", user);
        return "form-admin";
    }

    @PostMapping("/create-or-edit-user")
    public String saveUser(@ModelAttribute("user") User user) {
        if (user.getId() != null) {
            userService.save(user);
        } else {
            userService.addUser(user);
        }
        return "redirect:/admin/users";
    }


    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "user-list";
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/courses-admin")
    public String listCourses(Model model) {
        List<User> instructors = userService.findTeachers();
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("instructors", instructors);
        return "course-list";
    }

    @GetMapping("/course-form")
    public String showCourseForm(@RequestParam(required = false) Long id, Model model) {
        if (id != null) {
            model.addAttribute("course", courseService.findCourseById(id));
        } else {
            model.addAttribute("course", new Course());
        }
        model.addAttribute("instructors", courseService.findTeachers());
        return "course-form";
    }

    @PostMapping("/save-course")
    public String saveCourse(@ModelAttribute("course") Course course) {
        logger.info("Получен курс с ID: {}", course.getId());

        if (course.getId() != null && courseService.courseExists(course.getId())) {
            logger.info("Обновление курса с ID: {}", course.getId());
            courseService.save(course);
        } else {
            logger.info("Создание нового курса с названием: {}", course.getTitle());
            courseService.addCourse(course);
        }
        return "redirect:/admin/courses-admin";
    }

    @PostMapping("/delete-course")
    public String deleteCourse(@RequestParam Long id) {
        courseService.deleteCourse(id);
        return "redirect:/admin/courses-admin";
    }
}
