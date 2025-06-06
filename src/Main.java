import Model.StudentDAO;
import View.StudentView;
import Controller.StudentController;

public class Main {
    public static void main(String[] args) {
        StudentDAO studentDAO = new StudentDAO();
        StudentView studentView = new StudentView();
        StudentController controller = new StudentController(studentDAO, studentView);
        controller.run();
    }
}