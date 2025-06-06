package Controller;

import java.sql.SQLException;
import java.util.List;
import Model.Student;
import Model.StudentDAO;
import View.StudentView;

public class StudentController {
    private StudentDAO studentDAO;
    private StudentView studentView;

    public StudentController(StudentDAO studentDAO, StudentView studentView) {
        this.studentDAO = studentDAO;
        this.studentView = studentView;
    }

    public void run() {
        boolean running = true;

        while (running) {
            studentView.displayMenu();
            int choice = studentView.getIntInput();

            try {
                switch (choice) {
                    case 1:
                        addStudent();
                        break;
                    case 2:
                        viewAllStudents();
                        break;
                    case 3:
                        updateStudent();
                        break;
                    case 4:
                        deleteStudent();
                        break;
                    case 5:
                        searchStudentsByGrade();
                        break;
                    case 6:
                        listTopPerformingStudents();
                        break;
                    case 7:
                        calculateAverageScore();
                        break;
                    case 8:
                        running = false;
                        studentDAO.close();
                        studentView.showMessage("Exiting...");
                        break;
                    default:
                        studentView.showError("Invalid choice. Please try again.");
                }
            } catch (SQLException e) {
                studentView.showError("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                studentView.showError("Invalid input format. Please enter numbers where required.");
            }
        }
        studentView.close();
    }

    private void addStudent() throws SQLException {
        Student student = studentView.getStudentDetails();
        if (studentDAO.addStudent(student)) {
            studentView.showMessage("Student added successfully!");
        } else {
            studentView.showError("Failed to add student.");
        }
    }

    private void viewAllStudents() throws SQLException {
        List<Student> students = studentDAO.getAllStudents();
        studentView.showAllStudents(students);
    }

    private void updateStudent() throws SQLException {
        int id = studentView.getStudentId("Update");
        Student existingStudent = getStudentById(id);
        if (existingStudent != null) {
            Student updatedStudent = studentView.getUpdatedStudentDetails(existingStudent);
            if (studentDAO.updateStudent(updatedStudent)) {
                studentView.showMessage("Student updated successfully!");
            } else {
                studentView.showError("Failed to update student.");
            }
        } else {
            studentView.showError("Student not found with ID: " + id);
        }
    }

    private void deleteStudent() throws SQLException {
        int id = studentView.getStudentId("Delete");
        if (studentDAO.deleteStudent(id)) {
            studentView.showMessage("Student deleted successfully!");
        } else {
            studentView.showError("Student not found with ID: " + id);
        }
    }

    private void searchStudentsByGrade() throws SQLException {
        String grade = studentView.getGradeToSearch();
        List<Student> students = studentDAO.getStudentsByGrade(grade);
        studentView.showAllStudents(students);
    }

    private void listTopPerformingStudents() throws SQLException {
        List<Student> students = studentDAO.getTopPerformingStudents();
        studentView.showMessage("\n--- Top-Performing Students (Score >= 90) ---");
        studentView.showAllStudents(students);
    }

    private void calculateAverageScore() throws SQLException {
        float average = studentDAO.getAverageScore();
        studentView.showAverageScore(average);
    }

    private Student getStudentById(int id) throws SQLException {
        List<Student> students = studentDAO.getAllStudents();
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }
}