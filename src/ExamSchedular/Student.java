package ExamSchedular;

public class Student {

    private String id;
    private static int studentCounter = 0;

    public Student() {
        this.id = "L17 - " + studentCounter;
        studentCounter++;
    }

    public void print()
    {
        System.out.println("ID: " + this.id);
    }
}
