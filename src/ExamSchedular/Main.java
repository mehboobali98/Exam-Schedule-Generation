package ExamSchedular;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        long startTime, endTime, totalTime;
        GeneticAlgorithm ga = new GeneticAlgorithm(10,2000,0.95,0.50,10,5);
        startTime = System.nanoTime();
        ga.run();
        endTime = System.nanoTime();
        totalTime = endTime - startTime;
        System.out.println("Seconds: " + totalTime / (1000000000.0));
        System.out.println("Minutes: " + totalTime / (1000000000.0 * 60));

    }
}