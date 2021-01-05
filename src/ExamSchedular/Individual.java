package ExamSchedular;

import java.util.*;

//chromosome
public class Individual {

    private Gene[][] slot_genes;
    private ArrayList<Integer> genes;
    private ArrayList<Room> roomsList;
    private ArrayList<Course> coursesList;
    public boolean fitnessViolations[];

    private double fitness;
    private int examDays;
    private int examSlots;
    private static Random rn = new Random();

    public Individual(int examDays, int examsSlots, ArrayList<Room> r, ArrayList<Course> c, int x) {
        this.examDays = examDays;
        this.examSlots = examsSlots;
        this.fitness = 0;
        this.coursesList = c;
        this.roomsList = r;
        this.slot_genes = new Gene[examDays][examsSlots];
        this.genes = new ArrayList<>();
    }

    public Individual(int examDays, int examSlots, ArrayList<Course> c, ArrayList<Room> r) {
        this.examDays = examDays;
        this.examSlots = examSlots;
        this.roomsList = r;
        this.coursesList = c;
        this.slot_genes = new Gene[examDays][examSlots];
        this.genes = new ArrayList<>();
        this.createGene();
    }

    //copy constructor
    public Individual(Individual individual) {
        this.examSlots = individual.examSlots;
        this.examDays = individual.examDays;
        this.fitness = individual.fitness;
        this.roomsList = (ArrayList<Room>) individual.roomsList.clone();
        this.coursesList = (ArrayList<Course>) individual.coursesList.clone();
        this.slot_genes = new Gene[examDays][examSlots];
        this.genes = new ArrayList<>();

        for (int i = 0; i < coursesList.size(); i++) {
            this.genes.add(individual.genes.get(i));
        }
    }

    //randomly assigning slot numbers to courses.
    private void createGene() {

        int totalSlots = examDays * examSlots;
        int randomSlot;
        int totalCourses = coursesList.size();

        for (int i = 0; i < totalCourses; i++) {
            randomSlot = rn.nextInt(totalSlots);
            genes.add(randomSlot);
        }
    }

    //convert course list with slots number to slot table. It is required for computing fitness.
    public void convertToTable() {
        int totalCourses = coursesList.size();
        int day, slot, value, courseId;
        this.initializeIndividual();

        for (int i = 0; i < totalCourses; i++) {
            courseId = i;
            value = genes.get(i);
            day = value / examSlots;
            slot = value % examSlots;
            slot_genes[day][slot].addCourse(coursesList.get(courseId));
        }
    }

    //Calculate fitness. It is calculated based on the soft and hard constraints.
    public void calcFitness() {

        fitnessViolations = new boolean[6];

        if (checkStudentCountInSlot()) {
            fitness += -1000;
            fitnessViolations[0] = true;
        }

        if (checkThreeExams()) {
            fitness += -100;
            fitnessViolations[1] = true;
        }

        if (checkConsecutiveExams()) {
            fitness += -100;
            fitnessViolations[2] = true;
        }

        if (checkExamsAtSameTimeSlot()) {
            fitness += -1000;
            fitnessViolations[3] = true;
        }

        int count = countStudentsWithClashes();
        fitness -= count;
        if (count > 0) {
            fitnessViolations[4] = true;
        }

        count = studentsWithConsecutiveExams();
        fitness -= count;
        if (count > 0) {
            fitnessViolations[5] = true;
        }
    }

    //Hard-Constraint#1: total student count in a slot must be less than total room capacity.
    public boolean checkStudentCountInSlot() {
        int totalCapacity = Data.totalCapacity;
        int studentCount, totalViolations = 0;
        boolean found = false;

        for (int i = 0; i < examDays; i++) {
            for (int j = 0; j < examSlots; j++) {
                if (slot_genes[i][j] != null) {
                    studentCount = slot_genes[i][j].totalStudents(); // total students for one slot
                    if (studentCount > totalCapacity) {
                        totalViolations++;
                        found = true;
                        break;
                    } else {
                        studentCount = 0;
                    }
                }
            }
            if (found) {
                break;
            }
        }
        return found;
    }

    //Hard Constraint#2: a student cannot have more than 3 exams in the same day
    public boolean checkThreeExams() {
        boolean found = false;
        int count, courseCount;

        for (int i = 0; i < examDays; i++) {
            for (int j = 0; j < examSlots; j++) {
                if (slot_genes[i][j] != null) {
                    courseCount = slot_genes[i][j].totalCourses();
                    for (int k = 0; k < courseCount; k++) {
                        count = examInSameSlot(slot_genes[i][j].courses, k);
                        if (examSlots - j > 1) { //Index out of bound exception.
                            if (slot_genes[i][j + 1] != null) {
                                found = examInDifferentSlot(slot_genes[i][j + 1].courses, slot_genes[i][j].courses.get(k), count);
                            }
                        }
                        if (found == true) {
                            break;
                        }
                    }
                    if (found == true) {
                        break;
                    }
                }
            }
            if (found == true) {
                break;
            }
        }
        return found;
    }

    //Hard Constraint #3: Not a single student can have more than 2 exams in consecutive slots.
    public boolean checkConsecutiveExams() {
        boolean found = false;
        int count, courseCount;

        for (int i = 0; i < examDays; i++) {
            for (int j = 0; j < examSlots; j++) {
                if (slot_genes[i][j] != null) {
                    courseCount = slot_genes[i][j].totalCourses();
                    for (int k = 0; k < courseCount; k++) {
                        count = examInSameSlot(slot_genes[i][j].courses, k);
                        if (examSlots - j > 1) { //Index out of bound exception.
                            if (slot_genes[i][j + 1] != null) {
                                found = examInDifferentSlot_t(slot_genes[i][j + 1].courses, slot_genes[i][j].courses.get(k), count);
                            }
                        }
                        if (found == true) {
                            break;
                        }
                    }
                    if (found == true) {
                        break;
                    }
                }
            }
            if (found == true) {
                break;
            }
        }
        return found;
    }

    //Hard Constraint #4: checking for clashes,i.e. more than two exams in same slot.
    public boolean checkExamsAtSameTimeSlot() {
        boolean found = false;
        int count, courseCount;

        for (int i = 0; i < examDays; i++) {
            for (int j = 0; j < examSlots; j++) {
                if (slot_genes[i][j] != null) {
                    courseCount = slot_genes[i][j].totalCourses();
                    for (int k = 0; k < courseCount; k++) {
                        count = wrapperForClashExams(slot_genes[i][j].courses, k);
                        if (count >= 1) {
                            found = true;
                            break;
                        }
                    }
                }
                if (found == true) {
                    break;
                }
            }
            if (found == true) {
                break;
            }
        }
        return found;
    }

    //Soft Constraint: #1. Determines number of students with consecutive exams.
    private int studentsWithConsecutiveExams() {

        int count = 0, courseCount;

        for (int i = 0; i < examDays; i++) {
            for (int j = 0; j < examSlots; j++) {
                if (slot_genes[i][j] != null) {
                    courseCount = slot_genes[i][j].totalCourses();
                    for (int k = 0; k < courseCount; k++) {
                        if (examSlots - j > 1) { //Index out of bound exception.
                            if (slot_genes[i][j + 1] != null) { //checking in the next slot.
                                count += getConsecutiveStudents(slot_genes[i][j + 1].courses, slot_genes[i][j].courses.get(k));
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    //Soft Constraint: #2. Determines number of students with 2 exams in same time Slot.
    public int countStudentsWithClashes() {
        int count = 0, courseCount;

        for (int i = 0; i < examDays; i++) {
            for (int j = 0; j < examSlots; j++) {
                if (slot_genes[i][j] != null) {
                    courseCount = slot_genes[i][j].totalCourses();
                    for (int k = 0; k < courseCount; k++) {
                        count += getClashStudents(slot_genes[i][j].courses, k);
                    }
                }
            }
        }
        return count;
    }

    private int getClashStudents(ArrayList<Course> courses, int x) //this is for same slot.
    {
        int count = 0;
        int size = courses.size();
        int firstCourse, secondCourse;

        for (int i = x; i < size; i++) {
            firstCourse = courses.get(i).getId();
            for (int j = i + 1; j < size; j++) {
                secondCourse = courses.get(j).getId();
                count += CompareCourse(coursesList.get(firstCourse), coursesList.get(secondCourse));
            }
        }
        return count;
    }

    private int wrapperForClashExams(ArrayList<Course> courses, int x) //this is for same slot.
    {
        int count1 = 0, count2 = 0;
        int size = courses.size();
        int firstCourse, secondCourse;
        boolean found = false;

        for (int i = x; i < size; i++) {
            firstCourse = courses.get(i).getId();
            for (int j = i + 1; j < size; j++) {
                secondCourse = courses.get(j).getId();
                if (compareCourse(coursesList.get(firstCourse), coursesList.get(secondCourse))) {
                    count1++;
                }
                if (count1 == 2) {
                    count2++;
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        return count2;
    }

    private int examInSameSlot(ArrayList<Course> courses, int x) //this is for same slot.
    {
        int count = 0, c = 0;
        int size = courses.size();
        int firstCourse, secondCourse;
        boolean found = false;

        for (int i = x; i < size; i++) {
            firstCourse = courses.get(i).getId();
            for (int j = i + 1; j < size; j++) {
                secondCourse = courses.get(j).getId();
                if (compareCourse(coursesList.get(firstCourse), coursesList.get(secondCourse))) {
                    count++;
                }
                if (count == 1) {
                    c++;
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        return c;
    }

    private boolean examInDifferentSlot(ArrayList<Course> courses, Course course,
                                        int prevCount) //this is for different slot.
    {
        int count = 0, firstCourse, secondCourse;
        boolean found = false;

        firstCourse = course.getId();
        for (Course s : courses) {
            secondCourse = s.getId();
            if (compareCourse(coursesList.get(firstCourse), coursesList.get(secondCourse))) {
                count++;
            }
            if (prevCount + count >= 3) {
                found = true;
                break;
            }
        }
        return found;
    }

    private boolean examInDifferentSlot_t(ArrayList<Course> courses, Course course,
                                          int prevCount) //this is for different slot.
    {
        int count = 0, firstCourse, secondCourse;
        boolean found = false;

        firstCourse = course.getId();
        for (Course s : courses) {
            secondCourse = s.getId();
            if (compareCourse(coursesList.get(firstCourse), coursesList.get(secondCourse))) {
                count++;
            }
            if (prevCount + count >= 2) {
                found = true;
                break;
            }
        }
        return found;
    }

    private int getConsecutiveStudents(ArrayList<Course> courses, Course course) //this is for different slot.
    {
        int count = 0, firstCourse, secondCourse;

        firstCourse = course.getId();
        for (Course s : courses) {
            secondCourse = s.getId();
            count += CompareCourse(coursesList.get(firstCourse), coursesList.get(secondCourse));
        }
        return count;
    }

    //function to determine if two courses have common students
    private boolean compareCourse(Course c1, Course c2) {
        Student[] std = c1.getStd().toArray(new Student[0]);
        Set hashSet = new HashSet(Arrays.asList(std));
        Set commonElements = new HashSet();
        for (int i = 0; i < c2.getStd().size(); i++) {
            if (hashSet.contains(c2.getStd().get(i))) {
                commonElements.add(c2.getStd().get(i));
                break;
            }
        }
        return (commonElements.size() > 0) ? (true) : (false);// true means student is enrolled in both courses
    }

    //function to determine count of common students in two courses
    private int CompareCourse(Course c1, Course c2) {
        Student[] std = c1.getStd().toArray(new Student[0]);
        Set hashSet = new HashSet(Arrays.asList(std));
        Set commonElements = new HashSet();
        for (int i = 0; i < c2.getStd().size(); i++) {
            if (hashSet.contains(c2.getStd().get(i))) {
                commonElements.add(c2.getStd().get(i));
            }
        }
        return commonElements.size();// true means student is enrolled in both courses
    }

    public void updateGeneSlot(int courseOne, int courseTwo, int slotOne, int slotTwo) {
        int dayOne, dayTwo;
        dayOne = slotOne / examSlots; //day of course number 1.
        dayTwo = slotTwo / examSlots;
        slotOne = slotOne % examSlots;
        slotTwo = slotTwo % examSlots;
        ArrayList<Course> temp = new ArrayList<>(slot_genes[dayOne][slotOne].courses);
        slot_genes[dayOne][slotOne].setCourses(slot_genes[dayTwo][slotTwo].courses);
        slot_genes[dayTwo][slotTwo].setCourses(temp);
        temp = null;
    }

    //function to check if the schedule created satisfies the hard constraints. Short-circuiting has been used to improve time efficiency
    public boolean checkHardConstraints() {
        return ((!checkExamsAtSameTimeSlot()) && (!checkThreeExams()) && (!checkConsecutiveExams()) && (!checkStudentCountInSlot()));
    }

    //finding course with a particular Id.
    private Course findCourse(int courseId) {
        for (Course c : coursesList) {
            if (c.getId() == courseId) {
                return c;
            }
        }
        return null;
    }

    public void printIndividual() {

        int totalCourses = coursesList.size();

        for (int i = 0; i < totalCourses; i++) {
            System.out.print(genes.get(i) + " ");
        }
    }

    public void initializeIndividual() {
        for (int i = 0; i < examDays; i++) {
            for (int j = 0; j < examSlots; j++) {
                this.slot_genes[i][j] = new Gene();
            }
        }
    }

    public void resetFitness() {
        this.fitness = 0.0;
    }

    public double getFitness() {
        return fitness;
    }

    public ArrayList<Integer> getGenes() {
        return genes;
    }

    public void fillOffspring() {
        int totalCourses = coursesList.size();
        for (int i = 0; i < totalCourses; i++) {
            genes.add(-1);
        }
    }

    public void printViolations() {
        for (int i = 0; i < 6; i++) {
            System.out.println(fitnessViolations[i]);
        }
    }

}