public class CleanerWorker extends WorkerBee {
    private int corpsesCleaned;
    private boolean idle;

    public CleanerWorker(int id, double weight, int age) {
        super(id, weight, age);
        this.corpsesCleaned = 0;
        this.idle = false;
    }

    public int getCorpsesCleaned() { return corpsesCleaned; }
    public boolean isIdle() { return idle; }
    public void setIdle(boolean idle) { this.idle = idle; }

    public void incrementCorpsesCleaned() {
        this.corpsesCleaned++;
    }

    @Override
    public String getType() {
        return "уборщица";
    }

    @Override
    public String getJob() {
        return "убирает улей";
    }
}
