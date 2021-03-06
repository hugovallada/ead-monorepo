package com.ead.course.controllers;

import com.ead.course.dtos.CourseDto;
import com.ead.course.models.CourseModel;
import com.ead.course.services.CourseService;
import com.ead.course.specifications.SpecificationTemplate;
import com.ead.course.validation.CourseValidator;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "*", maxAge = 3600)
@AllArgsConstructor
public class CourseController {

    private final CourseService courseService;

    private final CourseValidator courseValidator;

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<Object> saveCourse(@RequestBody CourseDto courseDto, Errors errors) {
        courseValidator.validate(courseDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors.getAllErrors());
        }
        var courseModel = new CourseModel();
        BeanUtils.copyProperties(courseDto, courseModel);
        courseService.save(courseModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(courseModel);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Object> deleteCourse(@PathVariable("courseId") UUID courseId) {
        var course = courseService.findById(courseId);

        if (course.isEmpty()) return ResponseEntity.notFound().build();

        courseService.delete(course.get());
        return ResponseEntity.ok("Deleted");
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("/{courseId}")
    public ResponseEntity<Object> updateCourse(@PathVariable("courseId") UUID courseId, @RequestBody @Valid CourseDto courseDto) {
        var course = courseService.findById(courseId);

        if (course.isEmpty()) return ResponseEntity.notFound().build();

        var courseModel = course.get();

        BeanUtils.copyProperties(courseDto, courseModel);
        courseService.save(courseModel);

        return ResponseEntity.ok(courseModel);
    }

    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping
    public ResponseEntity<Page<CourseModel>> getAll(
            SpecificationTemplate.CourseSpec spec,
            @PageableDefault Pageable pageable,
            @RequestParam(value = "userId", required = false) UUID userId
    ) {
        if (userId != null) {
            return ResponseEntity.ok(courseService.findAll(pageable,
                    SpecificationTemplate.courseUserId(userId).and(spec)));
        }

        return ResponseEntity.ok(courseService.findAll(pageable, spec));
    }

    @PreAuthorize("hasAnyRole('STUDENT')")
    @GetMapping("/{courseId}")
    public ResponseEntity<Object> getOne(@PathVariable UUID courseId) {
        var course = courseService.findById(courseId);

        if (course.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(course.get());
    }

}
