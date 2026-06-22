public abstract class Bee {
    private int id;
    private double weight;
    private int age;
    private boolean alive;
    private boolean diedFromHunger;

    public Bee(int id, double weight, int age) {
        this.id = id;
        this.weight = weight;
        this.age = age;
        this.alive = true;
        this.diedFromHunger = false;
    }

    public int getId() { return id; }
    public double getWeight() { return weight; }
    public int getAge() { return age; }
    public boolean isAlive() { return alive; }
    public boolean isDiedFromHunger() { return diedFromHunger; }

    public void setWeight(double weight) { this.weight = weight; }
    public void setAge(int age) { this.age = age; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public void setDiedFromHunger(boolean diedFromHunger) { this.diedFromHunger = diedFromHunger; }

    public void growOlder() {
        age++;
    }

    public abstract String getType();
    public abstract int getMaxLifeDays();

    public void print() {
        String statusText;
        if (alive) {
            statusText = "жива";
        } else {
            statusText = "мертва";
        }
        System.out.println("Пчела #" + id +
                " | Тип: " + getType() +
                " | Вес: " + weight +
                " | Возраст: " + age +
                " | Статус: " + statusText);
    }
}
