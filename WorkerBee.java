public abstract class WorkerBee extends Bee {

    public WorkerBee(int id, double weight, int age) {
        super(id, weight, age);
    }

    @Override
    public int getMaxLifeDays() {
        return 30;
    }

    public abstract String getJob();
}
