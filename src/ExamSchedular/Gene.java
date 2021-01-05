package ExamSchedular;

import java.util.ArrayList;

public class Gene {

    public ArrayList<Course> courses;

    //constructor
    public Gene()
    {
        this.courses = new ArrayList<>();
    }

    public void setCourses(ArrayList<Course> courses)
    {
        this.courses = new ArrayList<>(courses);
    }

    public void addCourse(Course c)
    {
        this.courses.add(c);
    }

    //Computing total students in a gene. It is basically a time slot.
    public int totalStudents()
    {
        int totalStudents=0;
        for(Course c: courses)
        {
            totalStudents+=c.getStudentCount();
        }
        return totalStudents;
    }

    public void printGene()
    {
        System.out.println("Number of Courses: " + courses.size());
        for(Course s: courses)
        {
            System.out.print(s.getId() + " ");
        }
        System.out.println();
    }

    public int totalCourses()
    {
        return this.courses.size();
    }

}
