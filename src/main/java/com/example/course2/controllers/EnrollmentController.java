package com.example.course2.controllers;

import com.example.course2.models.*;
import com.example.course2.repositories.CertificateRepository;
import com.example.course2.repositories.CourseRepository;
import com.example.course2.repositories.UserRepository;
import com.example.course2.repositories.EnrollmentRepository;
import com.example.course2.services.CourseService;
import com.example.course2.services.EnrollmentService;
import com.example.course2.services.UserService;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.springframework.web.bind.annotation.RequestParam;


import com. itextpdf. kernel. colors. Color;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.LineSeparator;

import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private CertificateRepository certificateRepository;

    @PostMapping("/enroll/{courseId}")
    public String enrollInCourse(@PathVariable Long courseId, Principal principal) {
        Course course = courseService.findCourseById(courseId);
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (enrollmentService.findEnrollment(user, course).isEmpty()) {
            enrollmentService.enrollUserInCourse(user, course);
        }

        return "redirect:/myaccount";
    }


    @GetMapping("/myaccount")
    public String showMyAccount(Principal principal, Model model) {
        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Enrollment> enrollments = enrollmentService.findEnrollmentsByStudent(user);
        enrollments.forEach(enrollment -> {
            double completionPercentage = enrollmentService.calculateCourseCompletion(user, enrollment.getCourse());
            enrollment.setProgress(Math.round(completionPercentage));
        });
        model.addAttribute("user", user);
        model.addAttribute("enrollments", enrollments);


        return "myaccount";
    }

    private String generateCertificateCode() {
        return UUID.randomUUID().toString();
    }

    @GetMapping("/certificate/{courseId}/{userId}")
    public String issueCertificate(@PathVariable Long courseId, @PathVariable Long userId, HttpServletResponse response) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
        Certificate existingCertificate = certificateRepository.findByCourseAndUser(course, user).orElse(null);
        if (existingCertificate == null) {
            String certificateCode = generateCertificateCode();
            Certificate certificate = new Certificate();
            certificate.setCourse(course);
            certificate.setUser(user);
            certificate.setIssueDate(LocalDateTime.now());
            certificate.setCertificateCode(certificateCode);
            certificateRepository.save(certificate);
        }
        generateCertificatePdf(course, user, response);
        return "redirect:/myaccount";
    }

    public void generateCertificatePdf(Course course, User user, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=certificate.pdf");
        try (PdfWriter writer = new PdfWriter(response.getOutputStream())) {
            PdfDocument pdfDoc = new PdfDocument(writer);
            try (Document document = new Document(pdfDoc)) {
                PdfFont font = PdfFontFactory.createFont("fonts/arialuni.ttf", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                Paragraph title = new Paragraph("СЕРТИФИКАТ")
                        .setFont(font)
                        .setFontSize(36)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(30);
                document.add(title);

                Paragraph recipientText = new Paragraph()
                        .add("Этот сертификат подтверждает, что\n")
                        .add(new Text(user.getFullName()).setBold())
                        .add("\nуспешно завершил курс")
                        .setFont(font)
                        .setFontSize(20)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(30);
                document.add(recipientText);

                Paragraph courseName = new Paragraph(course.getTitle())
                        .setFont(font)
                        .setFontSize(24)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setMarginBottom(30);
                document.add(courseName);

                long durationInMonths = course.getDuration();
                Paragraph courseDuration = new Paragraph("Длительность курса: " + durationInMonths + " месяцев")
                        .setFont(font)
                        .setFontSize(14)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(30);
                document.add(courseDuration);

                LineSeparator separator = new LineSeparator(new SolidLine());
                document.add(separator);

                Paragraph footerText = new Paragraph("Сертификат действителен при предъявлении.")
                        .setFont(font)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(20);
                document.add(footerText);

                Paragraph secondaryText = new Paragraph("Этот сертификат подтверждает, что вы завершили обучение на нашем курсе и продемонстрировали необходимые навыки. Мы гордимся вашими достижениями!")
                        .setFont(font)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(30);
                document.add(secondaryText);

                Paragraph courseValueText = new Paragraph("Ваши знания и навыки будут ценны в вашей дальнейшей профессиональной карьере. Благодарим за участие!")
                        .setFont(font)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(10);
                document.add(courseValueText);

                float pageHeight = pdfDoc.getPage(1).getPageSize().getHeight();
                float pageWidth = pdfDoc.getPage(1).getPageSize().getWidth();

                PdfCanvas canvas = new PdfCanvas(pdfDoc.getFirstPage());
                canvas.setLineWidth(2)
                        .setStrokeColor(ColorConstants.BLACK)
                        .rectangle(10, 10, pageWidth-20, pageHeight-20)
                        .stroke();

                Paragraph signature = new Paragraph("Подпись организатора: ________________________")
                        .setFont(font)
                        .setFontSize(14)
                        .setTextAlignment(TextAlignment.LEFT);

                document.showTextAligned(signature, 50, 20, TextAlignment.LEFT);

            }
        }
        System.out.println("PDF файл успешно создан и отправлен в браузер!");
    }

}



