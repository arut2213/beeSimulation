public class Drone extends Bee {
    private int eggsFertilizedTotal;

    public Drone(int id, double weight, int age) {
        super(id, weight, age);
        this.eggsFertilizedTotal = 0;
    }

    public int getEggsFertilizedTotal() { return eggsFertilizedTotal; }

    public void addFertilized(int count) {
        this.eggsFertilizedTotal += count;
    }

    @Override
    public String getType() {
        return "трутень";
    }

    @Override
    public int getMaxLifeDays() {
        return 60;
    }

    public int getFertileCount(double honeyStock) {
        int base = 8;
        int variation = (int) (Math.random() * 5) - 2;
        int result = base + variation;

        if (honeyStock < 100) {
            result = result / 2;
        }
        if (result < 0) {
            result = 0;
        }
        return result;
    }
}
