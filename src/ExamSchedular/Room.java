package ExamSchedular;

public class Room {

    private int id;
    private int capacity;
    private static int roomCounter = 0;

    public Room(int capacity)
    {
        this.capacity = capacity;
        this.id = roomCounter;
        roomCounter++; // incrementing counter for next room
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void print()
    {
        System.out.println("ID: " + this.id);
        System.out.println("Capacity: " + this.capacity);
        System.out.println();
    }
}
