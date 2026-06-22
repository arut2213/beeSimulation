public class Queen extends Bee {

    public Queen(int id, double weight, int age) {
        super(id, weight, age);
    }

    @Override
    public String getType() {
        return "матка";
    }

    @Override
    public int getMaxLifeDays() {
        return 365;
    }

    public int getProductivity(double honeyStock, int deadNotCleaned) {
        int base = 5;

        if (honeyStock > 500) {
            base = 8;
        } else if (honeyStock > 200) {
            base = 5;
        } else if (honeyStock > 50) {
            base = 3;
        } else {
            base = 1;
        }

        if (deadNotCleaned > 20) {
            base = 0;
        } else if (deadNotCleaned > 10) {
            base = base / 2;
        }

        return base;
    }
}
