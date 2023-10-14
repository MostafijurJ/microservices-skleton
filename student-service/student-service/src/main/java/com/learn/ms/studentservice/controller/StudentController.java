package com.learn.ms.studentservice.controller;

import com.learn.ms.studentservice.dto.StudentDto;
import com.learn.ms.studentservice.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

	private final StudentService studentService;

	// Read - Retrieve a student by ID
	@GetMapping("/{studentId}")
	public StudentDto getStudent(@PathVariable Long studentId) {
		return studentService.getStudentById(studentId);
	}

	// Create - Add a new student
	@PostMapping
	public StudentDto createStudent(@RequestBody StudentDto studentDto) {
		return studentService.createStudent(studentDto);
	}

	// Update - Update an existing student by ID
	@PutMapping("/{studentId}")
	public StudentDto updateStudent(@PathVariable Long studentId, @RequestBody StudentDto studentDto) {
		return studentService.updateStudent(studentId, studentDto);
	}

	// Delete - Delete a student by ID
	@DeleteMapping("/{studentId}")
	public void deleteStudent(@PathVariable Long studentId) {
		studentService.deleteStudent(studentId);
	}
}
