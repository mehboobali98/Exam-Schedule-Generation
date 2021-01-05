package ExamSchedular;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class Population {

    private int populationSize;
    private int examDays;
    private int examSlots;
    private int fittest;
    private ArrayList<Individual> individuals;

    //constructor #1
    public Population(int populationSize, int examDays, int examSlots) {
        this.populationSize = populationSize;
        this.examDays = examDays;
        this.examSlots = examSlots;
        this.individuals = new ArrayList<>(populationSize);
    }

    //Initialize population
    public void initializePopulation(ArrayList<Course> courses, ArrayList<Room> rooms) {
        for (int i = 0; i < populationSize; i++) {
            Individual I = new Individual(examDays, examSlots, courses, rooms);
            I.convertToTable();
            this.individuals.add(I);
        }
    }

    //Calculate fitness of each individual in the population
    public void calculateFitness() {
        for (int i = 0; i < individuals.size(); i++) {
            individuals.get(i).calcFitness();
        }
        getFittest();
    }

    //Remove poor Individuals and add new ones.
    public void updatePopulation() {
        System.out.println("Updating Population..");
        this.sortPopulation();
        int half = populationSize / 2;
        for (int i = half; i < populationSize; i++) {
            individuals.remove(i);
        }
        ArrayList<Room> r = new ArrayList<>();
        for (int i = 0; i < half; i++) {
            Individual newIndividual = new Individual(examDays, examSlots, Data.courses, r);
            newIndividual.convertToTable();
            individuals.add(newIndividual);
        }
    }

    public void sortPopulation() {
        Collections.sort(individuals, new Comparator<Individual>() {
            public int compare(Individual o1, Individual o2) {
                return (int) (o2.getFitness() - o1.getFitness());
            }
        });
    }

    //Get the fittest individual
    public Individual getFittest() {
        int maxFit = Integer.MIN_VALUE;
        int maxFitIndex = 0;
        for (int i = 0; i < individuals.size(); i++) {
            if (maxFit <= individuals.get(i).getFitness()) {
                maxFit = (int) individuals.get(i).getFitness();
                maxFitIndex = i;
            }
        }
        fittest = (int) individuals.get(maxFitIndex).getFitness();
        return individuals.get(maxFitIndex);
    }

    //shuffling the population so that individuals are selected randomly in tournament.
    public void shuffle() {
        Random rnd = new Random();
        for (int i = populationSize - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            Individual a = individuals.get(index);
            individuals.set(index, individuals.get(i));
            individuals.set(i, a);
        }
    }

    //printing fitness of individuals in population.
    public void printFitness() {
        for (int i = 0; i < populationSize; i++) {
            System.out.print(individuals.get(i).getFitness() + " ");
        }
        System.out.println();
    }

    public void addIndividual(Individual I) {
        this.individuals.add(I);
    }

    public ArrayList<Individual> getIndividuals() {
        return individuals;
    }

    public Individual getIndividual(int i) {
        return this.individuals.get(i);
    }

    public int getPopulationSize() {
        return populationSize;
    }
}