package Model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class StudentDAO {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/university";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "1234";
    private Connection connection;



    public StudentDAO() {
        try {
            Class.forName("org.postgresql.Driver");

            // Establish connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    public boolean addStudent(Model.Student student) throws SQLException {
        if (!isValidStudent(student)) {
            return false;
        }

        String sql = "INSERT INTO Student (name, age, email, grade, score) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, student.getName());
            stmt.setInt(2, student.getAge());
            stmt.setString(3, student.getEmail());
            stmt.setString(4, student.getGrade());
            stmt.setFloat(5, student.getScore());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    student.setId(generatedKeys.getInt(1));
                }
            }
            return true;
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM Student ORDER BY id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        return students;
    }

    public boolean updateStudent(Student student) throws SQLException {
        if (!isValidStudent(student)) {
            return false;
        }

        String sql = "UPDATE Student SET name = ?, age = ?, email = ?, grade = ?, score = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, student.getName());
            stmt.setInt(2, student.getAge());
            stmt.setString(3, student.getEmail());
            stmt.setString(4, student.getGrade());
            stmt.setFloat(5, student.getScore());
            stmt.setInt(6, student.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM Student WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Student> getStudentsByGrade(String grade) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM Student WHERE grade = ? ORDER BY score DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, grade);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        return students;
    }

    public List<Student> getTopPerformingStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM Student WHERE score >= 90 ORDER BY score DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        return students;
    }

    public float getAverageScore() throws SQLException {
        String sql = "SELECT AVG(score) FROM Student";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getFloat(1);
            }
            return 0;
        }
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("email"),
                rs.getString("grade"),
                rs.getFloat("score")
        );
    }

    private boolean isValidStudent(Student student) {
        if (student.getAge() <= 0) {
            System.err.println("Error: Age must be positive.");
            return false;
        }
        if (!isValidEmail(student.getEmail())) {
            System.err.println("Error: Invalid email format.");
            return false;
        }
        if (!student.getGrade().matches("[A-D]|F")) {
            System.err.println("Error: Grade must be A, B, C, D, or F.");
            return false;
        }
        if (student.getScore() < 0 || student.getScore() > 100) {
            System.err.println("Error: Score must be between 0 and 100.");
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.matches(emailRegex, email);
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}