# Exam Schedule Generation by Searching (Local and Evolutionary)

Manually generating a Mid-term exam schedule for NUCES-FAST Lahore is an involved task as a diverse set of
constraints must be enforced while creating the schedule. In this repository, the famous natured inspired Genetic Algorithm combined with local search is implemented for solving the scheduling problem.

# Dataset

The dataset contains student registration information at FAST-NUCES Lahore. The files are stored in the "Dataset" folder. The description of the input files is as follows:

- A file named "registrationDdata.txt" that contains course registration status in the form of a 2D
  m x n array with space separated entries stored in row major order. Entry a[i][j] is 1 if students
  no j is registered in course i and 0 otherwise.
- A file named "roomCapacity.txt" containing a space separated list of room capacities available for
  scheduling.
- A file named "generalInfo.txt" containing a number specifying total exam days followed by a single
  number giving the exam slots per day for each room

# Methodology

These are discussed in detail in the report.

- An efficient representation of a chromosome (representation of a complete solution)
- Defining the crossover and mutation operator for representation of chromosome.
- Defining fitness function
- A generation of chromosome population
- Genetic Algorithm for solving the problem (i.e. repeatedly creating the next generation from existing generation
  until a termination criteria is met)
- Refining the solution using local search

# Best Parameters

After doing experiments using different population sizes, crossover rates, mutation rates and
different combination of selection, crossover and mutation operators, the best set of parameters
were found out to be:

| Parameter          | Value                         |
| ------------------ | ----------------------------- |
| Selection Operator | Elitism + Tournament          |
| Crossover Operator | Fixed-Point Uniform Crossover |
| Mutation Operator  | Uniform Mutation              |
| Crossover Rate     | 0.95                          |
| Mutation Rate      | 0.50                          |
| Elitism Rate       | 10 %                          |
| Tournament Size    | 5                             |
| Population Size    | 20                            |

# Directory Structure

<pre>
📦Exam-Schedule-Generation
┣ 📂Dataset
┃ ┣ 📜generalInfo.txt
┃ ┣ 📜registrationData.txt
┃ ┗ 📜roomCapacity.txt
┣ 📂Documents
┃ ┣ 📜17L-4316_Report.pdf
┃ ┗ 📜Third Programming Assignment.pdf
┣ 📂src
┃ ┗ 📂ExamSchedular
┃ ┃ ┣ 📜Course.java
┃ ┃ ┣ 📜Data.java
┃ ┃ ┣ 📜Gene.java
┃ ┃ ┣ 📜GeneticAlgorithm.java
┃ ┃ ┣ 📜Individual.java
┃ ┃ ┣ 📜Main.java
┃ ┃ ┣ 📜Population.java
┃ ┃ ┣ 📜Room.java
┃ ┃ ┗ 📜Student.java
┗ 📜README.md
</pre>

# Comments

- The program can be used for timetable generation of Final exams as well. Just need to change dataset for that.

# Future Work

- Improve GA as it takes a lot of time to find optimal solution.
