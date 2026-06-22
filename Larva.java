public class Larva extends Bee {
    private int daysAsLarva;

    public Larva(int id, double weight, int age) {
        super(id, weight, age);
        this.daysAsLarva = 0;
    }

    public int getDaysAsLarva() { return daysAsLarva; }

    public void incrementDaysAsLarva() {
        this.daysAsLarva++;
    }

    @Override
    public String getType() {
        return "личинка";
    }

    @Override
    public int getMaxLifeDays() {
        return 10;
    }

    public boolean isReadyToTransform() {
        return daysAsLarva >= 5;
    }
}
