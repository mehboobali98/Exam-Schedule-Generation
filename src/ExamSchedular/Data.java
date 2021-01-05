package ExamSchedular;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

public class Data {

    public static ArrayList<Course> courses;
    private ArrayList<Integer> capacities;
    public static ArrayList<Student> students;
    public static int examDays;
    public static int examSlots; //basically the number of slots
    public static int totalCapacity;

    //default constructor.
    public Data() throws IOException {
        this.courses = new ArrayList<>();
        this.capacities = new ArrayList<>();
        this.students = new ArrayList<>();
        this.readStudentData();
        this.readCourseData();
        this.readCapacityData();
        this.readGeneralInfo();
        this.setupData();
        totalCapacity = getTotalCapacity();
    }

    //Setting up courses.
    private void setupData() throws IOException {

        int stdCount = this.students.size();
        int courseCount = this.courses.size();
        //setting up the registration matrix
        int[][] registrationMatrix = readRegistrationData(stdCount, courseCount);
        for (int i = 0; i < courseCount; i++) {
            for (int j = 0; j < stdCount; j++) {
                if (registrationMatrix[i][j] == 0) // 0 means not registered
                {
                    continue;
                } else { // 1 means student is registered, hence add it to the student list in course object
                    this.courses.get(i).addStudent(this.students.get(j));
                }
            }
        }
        this.removeCourses();

    }

    //Read general info from file.
    private void readGeneralInfo() throws IOException {

        File file = new File(String.valueOf(Path.of("generalInfo.txt")));
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        while ((st = br.readLine()) != null) {
            String[] generalInfo = st.trim().split("\\s+");
            this.examDays = Integer.parseInt(generalInfo[0]);
            this.examSlots = Integer.parseInt(generalInfo[1]);
        }

    }

    //Read capacity data from file.
    private void readCapacityData() throws IOException {

        File file = new File(String.valueOf(Path.of("roomCapacity.txt")));
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        while ((st = br.readLine()) != null) {
            String[] capacityS = st.trim().split("\\s+");
            for (int i = 0; i < capacityS.length; i++) {
                capacities.add(Integer.parseInt(capacityS[i]));
            }
        }

    }

    //Read registration data from file.
    public int[][] readRegistrationData(int studentCount, int courseCount) throws FileNotFoundException {

        int[][] registrationMatrix = new int[courseCount][studentCount];
        File inFile = new File("registrationData.txt");
        Scanner in = new Scanner(inFile);
        int lineCount = 0;
        while (in.hasNextLine()) {
            String[] currentLine = in.nextLine().trim().split("\t");
            for (int i = 0; i < currentLine.length; i++) {
                registrationMatrix[lineCount][i] = Integer.parseInt(currentLine[i]);
            }
            lineCount++;
        }
        in.close();
        return registrationMatrix;

    }

    //Find number of students.
    private int getStudentCount() throws FileNotFoundException {

        int studentCount = 0;
        File inFile = new File("registrationData.txt");
        Scanner in = new Scanner(inFile);

        String[] currentLine = in.nextLine().trim().split("\t");//data is tab separated.
        studentCount = currentLine.length;
        in.close();
        return studentCount;

    }

    //Find number of courses.
    private int getCoursesCount() throws FileNotFoundException {

        File inFile = new File("registrationData.txt");
        Scanner in = new Scanner(inFile);
        int lineCount = 0;
        while (in.hasNextLine()) {
            in.nextLine();
            lineCount++;
        }
        in.close();
        return lineCount;

    }

    //Read Student Data from file
    private void readStudentData() throws FileNotFoundException {

        int stdCount = getStudentCount();
        ArrayList<Student> std = new ArrayList<>();
        for (int i = 0; i < stdCount; i++) {
            Student s = new Student();
            std.add(s);
        }
        this.students = std;

    }

    //Read course data from file.
    private void readCourseData() throws IOException {

        int courseCount = getCoursesCount();
        for (int i = 0; i < courseCount; i++) {
            Course c = new Course();
            this.courses.add(c);
        }

    }

    //Removing courses with no students as they won't be scheduled.
    private void removeCourses() {

        int count = 0;
        ArrayList<Course> courseList = new ArrayList<>();
        for (Course c : this.courses) {
            if (c.getStudentCount() > 0) {
                c.setCourseId(count);
                courseList.add(c);
                count++;
            }
        }
        this.courses = courseList;
    }

    //Function to get total capacity for a slot.
    public int getTotalCapacity() {
        int capacity = 0;
        for (Integer i : capacities) {
            capacity += i;
        }
        return capacity;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public int getExamDays() {
        return examDays;
    }

    public int getExamSlots() {
        return examSlots;
    }
}
