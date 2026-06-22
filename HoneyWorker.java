public class HoneyWorker extends WorkerBee {
    private double honeyBroughtTotal;

    public HoneyWorker(int id, double weight, int age) {
        super(id, weight, age);
        this.honeyBroughtTotal = 0;
    }

    public double getHoneyBroughtTotal() { return honeyBroughtTotal; }

    public double bringHoney() {
        double amount = 1.5 + Math.random() * 1.5;
        honeyBroughtTotal += amount;
        return amount;
    }

    @Override
    public String getType() {
        return "сборщица мёда";
    }

    @Override
    public String getJob() {
        return "собирает мёд";
    }
}
