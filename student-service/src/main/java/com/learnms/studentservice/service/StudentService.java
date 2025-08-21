package com.learnms.studentservice.service;

import com.learnms.studentservice.entity.Student;
import com.learnms.studentservice.exceptions.ResourceNotFoundException;
import com.learnms.studentservice.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;

    public Student createStudent(Student student) {
        try {
            return studentRepository.save(student);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email must be unique");
        }
    }

    public Student getStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student updateStudent(Long id, Student updated) {
        return studentRepository.findById(id)
                .map(s -> {
                    s.setName(updated.getName());
                    s.setEmail(updated.getEmail());
                    s.setAge(updated.getAge());
                    return studentRepository.save(s);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with ID: " + id);
        }
        studentRepository.deleteById(id);
    }
}
