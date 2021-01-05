package ExamSchedular;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class GeneticAlgorithm {

    private Population population;
    private Data data;
    private ArrayList<Course> courses;
    private ArrayList<Room> rooms;
    private ArrayList<Student> students;
    private int examDays;
    private int examSlots; //basically the number of slots
    private int populationSize;
    private int maxGenerationCount;
    private int tournamentSize;
    private double crossOverRate;
    private double mutationRate;
    private double elitismRate;
    private Random rn = new Random();

    //default constructor
    GeneticAlgorithm(int populationSize, int maxGenerationCount, double crossOverRate, double mutationRate, double elitismRate, int tournamentSize) throws IOException {
        this.courses = new ArrayList<>();
        this.rooms = new ArrayList<>();
        this.students = new ArrayList<>();
        this.populationSize = populationSize;
        this.maxGenerationCount = maxGenerationCount;
        this.crossOverRate = crossOverRate;
        this.mutationRate = mutationRate;
        this.elitismRate = elitismRate;
        this.tournamentSize = tournamentSize;
        this.data = new Data();
        this.readStudentData();
        this.readCourseData();
        this.readCapacityData();
        this.readGeneralInfo();
        this.setupData();
    }

    //main function
    public void run() {

        int generationCount = 0;
        int convergenceCheck = 100;
        int fittest, futureFittest;
        boolean found = false;
        population = new Population(populationSize, examDays, examSlots);

        //Initialize population
        population.initializePopulation(courses, rooms);

        population.calculateFitness();

        fittest = (int) population.getFittest().getFitness();

        System.out.println("Generation: " + generationCount);

        //While population gets an individual with maximum fitness
        while (population.getIndividual(0).getFitness() < 0) {
            ++generationCount;
            if (generationCount > maxGenerationCount) {
                break;
            }

            population.sortPopulation();

            if ((generationCount % convergenceCheck) == 0) {
                futureFittest = (int) population.getIndividual(0).getFitness();
                if (fittest == futureFittest) {
                    found = true;
                    population.updatePopulation();
                    //break;
                }
                fittest = futureFittest;
            }

            Population newGeneration = new Population(populationSize, examDays, examSlots);

            int s = (int) ((elitismRate * populationSize) / 100);
            for (int i = 0; i < s; i++) {
                newGeneration.addIndividual(population.getIndividual(i));
            }

            s = populationSize - s;
            //s = (90 * populationSize) / 100;
            //int rr = populationSize / 2;
            for (int i = 0; i < s; i++) {
                //int r = rn.nextInt(rr);
                //Individual parent1 = population.getIndividual(r);
                Individual parent1 = selectParent(population);
                //r = rn.nextInt(rr);
                //Individual parent2 = population.getIndividual(r);
                Individual parent2 = selectParent(population);

                // while( parent1 == parent2)
                //{
                //  parent1 = selectParent(population);
                //}

                //Individual offSpring = mate(parent1, parent2);
                if(crossOverRate > Math.random()) {
                    Individual offSpring = fixedUniformPointCrossover(parent1, parent2);
                    newGeneration.addIndividual(offSpring);
                } else {
                    newGeneration.addIndividual((parent1.getFitness() > parent2.getFitness()) ? (parent1) : (parent2));
                }
            }

            population = newGeneration;

            population = mutatePopulation();

            population = localOptimization();
            //Do selection
            //population = selection();
            //population = mutatePopulation();

            System.out.println("Generation: " + generationCount + " Fittest: " + population.getIndividual(0).getFitness());
        }
        if (!found) {
            System.out.println("\nSolution found in generation " + generationCount);
            System.out.println("");
            System.out.println(population.getIndividuals().get(0).getFitness());
            population.getIndividuals().get(0).printViolations();
        } else {
            System.out.println("\nMaximum fitness has not changed since last " + convergenceCheck + " generations. Stuck.");
        }
    }

    private Population selection() {

        Population p1 = new Population(populationSize, examDays, examSlots);

        for (int ii = 0; ii < populationSize; ii++) {
            Individual parent1 = population.getIndividual(ii);

            if (crossOverRate > Math.random() && ii > elitismRate) {

                Individual parent2 = selectParent(population);

                ArrayList<Individual> offSprings = crossover(parent1, parent2);
                offSprings.get(0).calcFitness();
                int f1 = (int) offSprings.get(0).getFitness();
                offSprings.get(1).calcFitness();
                int f2 = (int) offSprings.get(1).getFitness();
                p1.addIndividual((f1 > f2) ? (offSprings.get(0)) : (offSprings.get(1)));

            } else {
                p1.addIndividual(parent1);
            }
        }
        return p1;
    }

    //Uniform-Crossover
    private Individual uniformCrossover(Individual parent1, Individual parent2) {
        // chromosome for offspring
        Individual offSpring = new Individual(examDays, examSlots, rooms, courses, -1);
        int randSlot, totalSlots;
        totalSlots = examDays * examSlots;

        int len = courses.size();
        for (int i = 0; i < len; i++) {
            // random probability
            float p = rn.nextInt(100) / 100;

            if (p < (crossOverRate / 2)) {
                offSpring.getGenes().add(i, parent1.getGenes().get(i));

            } else if (p < (crossOverRate)) {
                offSpring.getGenes().add(i, parent2.getGenes().get(i));

            } else {
                randSlot = rn.nextInt(totalSlots);
                offSpring.getGenes().add(i, randSlot);
            }
        }

        offSpring.convertToTable();
        offSpring.calcFitness();
        return offSpring;
    }

    //One-point Crossover
    private Individual onePointCrossover(Individual parent1, Individual parent2) {

        Individual offSpring = new Individual(examDays, examSlots, rooms, courses, -1);

        int totalCourses = courses.size();
        int crossoverPoint = rn.nextInt(totalCourses);

        for (int i = 0; i < crossoverPoint; i++) {
            offSpring.getGenes().add(parent1.getGenes().get(i));
        }

        for (int i = crossoverPoint; i < totalCourses; i++) {
            offSpring.getGenes().add(parent2.getGenes().get(i));
        }

        offSpring.convertToTable();
        offSpring.calcFitness();

        return offSpring;
    }

    //Fixed Point Uniform Crossover.
    private Individual fixedUniformPointCrossover(Individual parent1, Individual paren2) {
        Individual offSpring = new Individual(examDays, examSlots, rooms, courses, -1);
        offSpring.fillOffspring();
        int length = courses.size() / 2;
        int totalCourses = courses.size();
        int randCourse;
        Set<Integer> hashSet = new HashSet<>();

        while (hashSet.size() < length) {
            randCourse = rn.nextInt(totalCourses);
            hashSet.add(randCourse);
        }

        for (int i = 0; i < totalCourses; i++) {
            if (hashSet.contains(i)) {
                offSpring.getGenes().set(i, parent1.getGenes().get(i));
            }
            offSpring.getGenes().set(i, paren2.getGenes().get(i));
        }

        offSpring.convertToTable();
        offSpring.calcFitness();
        return offSpring;
    }

    private ArrayList<Individual> crossover(Individual parent1, Individual parent2) {

        ArrayList<Individual> offSprings = new ArrayList<>();
        Individual offSpring1 = onePointCrossover(parent1, parent2);
        offSprings.add(offSpring1);
        Individual offSpring2 = onePointCrossover(parent2, parent1);
        offSprings.add(offSpring2);
        return offSprings;
    }

    private Population mutatePopulation() {
        // Initialize new population
        Population newPopulation = new Population(populationSize, examDays, examSlots);
        int prevFitness;

        int elites = (int) ((elitismRate * populationSize) / 100);
        for (int i = 0; i < elites; i++) {
            newPopulation.addIndividual(population.getIndividual(i));
        }

        elites = populationSize - elites;

        for (int populationIndex = 0; populationIndex < elites; populationIndex++) {

            Individual individual = population.getIndividual(populationIndex);
            prevFitness = (int) individual.getFitness();

            if (mutationRate > Math.random()) {
                individual = uniformMutation((individual));//interchanging
                individual.calcFitness();
                if (individual.getFitness() > prevFitness) {
                    newPopulation.addIndividual(individual);
                } else {
                    newPopulation.addIndividual(population.getIndividual(populationIndex));
                }
            } else {
                newPopulation.addIndividual(population.getIndividual(populationIndex));
            }
        }
        return newPopulation;
    }

    //Randomly picks course and assigns it a new random slot.
    private Individual uniformMutation(Individual individual) {

        int randCourse, totalCourse, randSlot, totalSlots;
        Individual newIndividual = new Individual(individual);

        totalCourse = courses.size();
        totalSlots = examDays * examSlots;
        randCourse = rn.nextInt(totalCourse);
        randSlot = rn.nextInt(totalSlots);

        newIndividual.getGenes().set(randCourse, randSlot);
        newIndividual.resetFitness();
        newIndividual.convertToTable();

        return newIndividual;
    }

    //Selects two courses and swaps their slots.
    private Individual interchangingMutation(Individual individual) {
        int randCourse1, randCourse2, totalCourse, swap;
        Random rn = new Random();
        Individual newIndividual = new Individual(individual);
        totalCourse = courses.size();

        randCourse1 = rn.nextInt(totalCourse);
        randCourse2 = rn.nextInt(totalCourse);

        while (randCourse1 == randCourse2) {
            randCourse1 = rn.nextInt(totalCourse);
        }

        swap = newIndividual.getGenes().get(randCourse1); //get slot of first course.
        newIndividual.getGenes().set(randCourse1, newIndividual.getGenes().get(randCourse2));
        newIndividual.getGenes().set(newIndividual.getGenes().get(randCourse2), swap);

        newIndividual.resetFitness();
        newIndividual.convertToTable();

        return newIndividual;
    }

    //Selects a random course (point), then reverses the array to the right of it.
    private Individual reverseMutation(Individual individual) {
        int randCourse, totalCourse, index;
        Random rn = new Random();
        Individual newIndividual = new Individual(individual);
        totalCourse = courses.size();
        index = totalCourse - 1;
        randCourse = rn.nextInt(totalCourse);

        for (int i = 0; i < randCourse; i++) {
            newIndividual.getGenes().set(i, individual.getGenes().get(i));
        }

        for (int i = randCourse; i < totalCourse; i++) {
            newIndividual.getGenes().set(i, individual.getGenes().get(index));
            index--;
        }

        newIndividual.resetFitness();
        newIndividual.convertToTable();

        return newIndividual;
    }

    private Population localOptimization() {
        Population newPopulation = new Population(populationSize, examDays, examSlots);
        int prevFitness;

        for (int populationIndex = 0; populationIndex < populationSize; populationIndex++) {

            Individual individual = population.getIndividual(populationIndex);
            prevFitness = (int) individual.getFitness();

            if (mutationRate > Math.random()) {
                while (true) {
                    individual = uniformMutation(individual);
                    individual.calcFitness();
                    if (individual.getFitness() >= prevFitness) {
                        newPopulation.addIndividual(individual);
                        break;
                    } else {
                        individual = population.getIndividual(populationIndex);
                    }
                }
            } else { // don't mutate elite individuals
                newPopulation.addIndividual(population.getIndividual(populationIndex));
            }
        }
        return newPopulation;
    }

    //Select second parent for crossover through tournament selection.
    private Individual selectParent(Population population) {
        // Create tournament
        Population tournament = new Population(tournamentSize, examDays, examSlots);

        population.shuffle();

        for (int i = 0; i < tournamentSize; i++) {
            Individual tournamentIndividual = population.getIndividual(i);
            tournament.addIndividual(tournamentIndividual);
        }
        tournament.sortPopulation();
        // Return the best individual in the tournament.
        return tournament.getIndividual(0);
    }

    //setting up rooms,courses
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

    //read general info from file
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

    //read capacity data from file
    private void readCapacityData() throws IOException {
        ArrayList<Room> roomsList = new ArrayList<>();
        File file = new File(String.valueOf(Path.of("roomCapacity.txt")));
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        while ((st = br.readLine()) != null) {
            String[] capacities = st.trim().split("\\s+");
            for (int i = 0; i < capacities.length; i++) {
                Room r = new Room(Integer.parseInt(capacities[i]));
                roomsList.add(r);
            }
        }
        this.rooms = roomsList;
    }

    //read registration data from file
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

    //find number of students.
    private int getStudentCount() throws FileNotFoundException {
        int studentCount = 0;
        File inFile = new File("registrationData.txt");
        Scanner in = new Scanner(inFile);

        String[] currentLine = in.nextLine().trim().split("\t");//data is tab separated.
        studentCount = currentLine.length;
        in.close();
        return studentCount;
    }

    //find number of courses
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

    //read Student Data from file
    private void readStudentData() throws FileNotFoundException {

        int stdCount = getStudentCount();
        ArrayList<Student> std = new ArrayList<>();
        for (int i = 0; i < stdCount; i++) {
            Student s = new Student();
            std.add(s);
        }
        this.students = std;
    }

    //read course data from file
    private void readCourseData() throws IOException {

        ArrayList<Course> courseList = new ArrayList<>();
        int courseCount = getCoursesCount();
        for (int i = 0; i < courseCount; i++) {
            Course c = new Course();
            courseList.add(c);
        }
        this.courses = courseList;
    }

    //removing courses with no students
    private void removeCourses() {
        ArrayList<Course> courseList = new ArrayList<>();
        int count = 0;

        for (Course c : this.courses) {
            if (c.getStudentCount() > 0) {
                c.setCourseId(count);
                courseList.add(c);
                count++;
            }
        }
        this.courses = courseList;
    }
}

