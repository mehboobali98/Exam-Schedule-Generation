package ExamSchedular;

import java.util.ArrayList;

public class Course {

    private int id;
    private ArrayList<Student> std;

    //default constructor
    public Course()
    {
        this.std = new ArrayList<>();
    }

    public void setCourseId(int Id)
    {
        this.id = Id;
    }

    public void addStudent(Student s)
    {
        this.std.add(s);
    }

    public void print()
    {
        System.out.println("Course ID: " + this.id);
        System.out.println("Total Students: " + this.std.size());
        System.out.println("---------Student Data----------");
        for(int i=0;i<this.std.size();i++)
        {
            this.std.get(i).print();
        }
        System.out.println();
    }

    public ArrayList<Student> getStd() {
        return std;
    }

    public int getId() {
        return id;
    }

    public int getStudentCount()
    {
        return std.size();
    }
}

