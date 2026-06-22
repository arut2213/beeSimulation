import java.io.*;
import java.util.*;

public class Hive {
    private Queen queen;
    private ArrayList<Drone> drones = new ArrayList<Drone>();
    private ArrayList<Larva> larvae = new ArrayList<Larva>();
    private ArrayList<HoneyWorker> honeyWorkers = new ArrayList<HoneyWorker>();
    private ArrayList<CleanerWorker> cleanerWorkers = new ArrayList<CleanerWorker>();
    private ArrayList<Bee> deadCorpses = new ArrayList<Bee>();

    private double honeyStock;
    private int currentStep;
    private int nextId;

    private int deathsFromHungerLarvae;
    private int deathsFromHungerDrones;
    private int deathsFromHungerHoneyWorkers;
    private int deathsFromHungerCleanerWorkers;
    private double totalHoneyProduced;
    private double totalHoneyConsumed;
    private int idleCleanerCount;

    private String dataFile = "hive_data.txt";

    public Hive() {
        this.honeyStock = 1000;
        this.currentStep = 0;
        this.nextId = 1;
    }

    public void initializeHive() {
        queen = new Queen(nextId, 1.5, 0);
        nextId++;

        for (int i = 0; i < 5; i++) {
            drones.add(new Drone(nextId, 0.3, 1));
            nextId++;
        }
        for (int i = 0; i < 20; i++) {
            honeyWorkers.add(new HoneyWorker(nextId, 0.12, 1));
            nextId++;
        }
        for (int i = 0; i < 5; i++) {
            cleanerWorkers.add(new CleanerWorker(nextId, 0.15, 1));
            nextId++;
        }
    }

    public Queen getQueen() { return queen; }
    public ArrayList<Drone> getDrones() { return drones; }
    public ArrayList<Larva> getLarvae() { return larvae; }
    public ArrayList<HoneyWorker> getHoneyWorkers() { return honeyWorkers; }
    public ArrayList<CleanerWorker> getCleanerWorkers() { return cleanerWorkers; }
    public ArrayList<Bee> getDeadCorpses() { return deadCorpses; }
    public double getHoneyStock() { return honeyStock; }
    public int getCurrentStep() { return currentStep; }

    public void simulationStep() {
        currentStep++;
        produceEggs();
        collectHoney();
        cleanCorpses();
        consumeHoney();
        growLarvae();
        ageAndDie();
    }

    private void produceEggs() {
        if (queen == null || !queen.isAlive()) {
            return;
        }
        int eggsByQueen = queen.getProductivity(honeyStock, deadCorpses.size());

        int maxFertile = 0;
        for (int i = 0; i < drones.size(); i++) {
            if (drones.get(i).isAlive()) {
                maxFertile += drones.get(i).getFertileCount(honeyStock);
            }
        }

        int actualEggs = eggsByQueen;
        if (actualEggs > maxFertile) {
            actualEggs = maxFertile;
        }

        for (int i = 0; i < actualEggs; i++) {
            larvae.add(new Larva(nextId, 0.01, 0));
            nextId++;
        }

        if (actualEggs > 0 && drones.size() > 0) {
            int per = actualEggs / drones.size();
            int rest = actualEggs % drones.size();
            for (int i = 0; i < drones.size(); i++) {
                if (drones.get(i).isAlive()) {
                    drones.get(i).addFertilized(per);
                }
            }
            for (int i = 0; i < drones.size(); i++) {
                if (drones.get(i).isAlive()) {
                    drones.get(i).addFertilized(rest);
                    break;
                }
            }
        }
    }

    private void collectHoney() {
        for (int i = 0; i < honeyWorkers.size(); i++) {
            HoneyWorker hw = honeyWorkers.get(i);
            if (hw.isAlive()) {
                double brought = hw.bringHoney();
                honeyStock += brought;
                totalHoneyProduced += brought;
            }
        }
    }

    private void cleanCorpses() {
        idleCleanerCount = 0;
        for (int i = 0; i < cleanerWorkers.size(); i++) {
            CleanerWorker cw = cleanerWorkers.get(i);
            if (!cw.isAlive()) {
                continue;
            }
            boolean cleaned = false;
            for (int j = 0; j < deadCorpses.size(); j++) {
                if (deadCorpses.get(j).getWeight() < cw.getWeight()) {
                    deadCorpses.remove(j);
                    cw.incrementCorpsesCleaned();
                    cw.setIdle(false);
                    cleaned = true;
                    break;
                }
            }
            if (!cleaned) {
                if (deadCorpses.size() > 0) {
                    cw.setIdle(true);
                    idleCleanerCount++;
                } else {
                    cw.setIdle(false);
                }
            }
        }
    }

    private void consumeHoney() {
        if (queen != null && queen.isAlive()) {
            double need = queen.getWeight() * 0.5;
            if (honeyStock >= need) {
                honeyStock -= need;
                totalHoneyConsumed += need;
            } else {
                killFromHunger(queen);
            }
        }

        for (int i = 0; i < drones.size(); i++) {
            Drone d = drones.get(i);
            if (d.isAlive()) {
                double need = d.getWeight() * 0.5;
                if (honeyStock >= need) {
                    honeyStock -= need;
                    totalHoneyConsumed += need;
                } else {
                    killFromHunger(d);
                    deathsFromHungerDrones++;
                }
            }
        }

        for (int i = 0; i < honeyWorkers.size(); i++) {
            HoneyWorker hw = honeyWorkers.get(i);
            if (hw.isAlive()) {
                double need = hw.getWeight() * 0.5;
                if (honeyStock >= need) {
                    honeyStock -= need;
                    totalHoneyConsumed += need;
                } else {
                    killFromHunger(hw);
                    deathsFromHungerHoneyWorkers++;
                }
            }
        }

        for (int i = 0; i < cleanerWorkers.size(); i++) {
            CleanerWorker cw = cleanerWorkers.get(i);
            if (cw.isAlive()) {
                double need = cw.getWeight() * 0.5;
                if (honeyStock >= need) {
                    honeyStock -= need;
                    totalHoneyConsumed += need;
                } else {
                    killFromHunger(cw);
                    deathsFromHungerCleanerWorkers++;
                }
            }
        }

        for (int i = 0; i < larvae.size(); i++) {
            Larva l = larvae.get(i);
            if (l.isAlive()) {
                double need = l.getWeight() * 0.5;
                if (need < 0.05) need = 0.05;
                if (honeyStock >= need) {
                    honeyStock -= need;
                    totalHoneyConsumed += need;
                    l.setWeight(l.getWeight() + 0.02);
                } else {
                    killFromHunger(l);
                    deathsFromHungerLarvae++;
                }
            }
        }
    }

    private void killFromHunger(Bee b) {
        b.setAlive(false);
        b.setDiedFromHunger(true);
        deadCorpses.add(b);
    }

    private void growLarvae() {
        ArrayList<Larva> toTransform = new ArrayList<Larva>();
        for (int i = 0; i < larvae.size(); i++) {
            Larva l = larvae.get(i);
            if (l.isAlive()) {
                l.incrementDaysAsLarva();
                if (l.isReadyToTransform()) {
                    toTransform.add(l);
                }
            }
        }

        for (int i = 0; i < toTransform.size(); i++) {
            Larva l = toTransform.get(i);
            larvae.remove(l);

            double r = Math.random();
            if (r < 0.3) {
                drones.add(new Drone(l.getId(), 0.3, 0));
            } else {
                double r2 = Math.random();
                if (r2 < 0.7) {
                    honeyWorkers.add(new HoneyWorker(l.getId(), 0.12, 0));
                } else {
                    cleanerWorkers.add(new CleanerWorker(l.getId(), 0.15, 0));
                }
            }
        }
    }

    private void ageAndDie() {
        if (queen != null && queen.isAlive()) {
            queen.growOlder();
            if (queen.getAge() > queen.getMaxLifeDays()) {
                queen.setAlive(false);
                deadCorpses.add(queen);
            }
        }

        for (int i = 0; i < drones.size(); i++) {
            Drone d = drones.get(i);
            if (d.isAlive()) {
                d.growOlder();
                if (d.getAge() > d.getMaxLifeDays()) {
                    d.setAlive(false);
                    deadCorpses.add(d);
                }
            }
        }

        for (int i = 0; i < honeyWorkers.size(); i++) {
            HoneyWorker hw = honeyWorkers.get(i);
            if (hw.isAlive()) {
                hw.growOlder();
                if (hw.getAge() > hw.getMaxLifeDays()) {
                    hw.setAlive(false);
                    deadCorpses.add(hw);
                }
            }
        }

        for (int i = 0; i < cleanerWorkers.size(); i++) {
            CleanerWorker cw = cleanerWorkers.get(i);
            if (cw.isAlive()) {
                cw.growOlder();
                if (cw.getAge() > cw.getMaxLifeDays()) {
                    cw.setAlive(false);
                    deadCorpses.add(cw);
                }
            }
        }

        for (int i = 0; i < larvae.size(); i++) {
            Larva l = larvae.get(i);
            if (l.isAlive()) {
                l.growOlder();
                if (l.getAge() > l.getMaxLifeDays()) {
                    l.setAlive(false);
                    deadCorpses.add(l);
                }
            }
        }

        removeDeadFromLists();
    }

    private void removeDeadFromLists() {
        for (int i = drones.size() - 1; i >= 0; i--) {
            if (!drones.get(i).isAlive()) drones.remove(i);
        }
        for (int i = honeyWorkers.size() - 1; i >= 0; i--) {
            if (!honeyWorkers.get(i).isAlive()) honeyWorkers.remove(i);
        }
        for (int i = cleanerWorkers.size() - 1; i >= 0; i--) {
            if (!cleanerWorkers.get(i).isAlive()) cleanerWorkers.remove(i);
        }
        for (int i = larvae.size() - 1; i >= 0; i--) {
            if (!larvae.get(i).isAlive()) larvae.remove(i);
        }
    }

    public void showStatistics() {
        System.out.println("\n===== СТАТИСТИКА УЛЬЯ =====");
        System.out.println("Шаг симуляции: " + currentStep);
        System.out.println("Запас мёда: " + roundTo2(honeyStock));

        int queenAlive = 0;
        if (queen != null && queen.isAlive()) queenAlive = 1;

        System.out.println("\n--- Количество пчёл ---");
        System.out.println("Матка: " + queenAlive);
        System.out.println("Трутни: " + drones.size());
        System.out.println("Личинки: " + larvae.size());
        System.out.println("Сборщицы мёда: " + honeyWorkers.size());
        System.out.println("Уборщицы: " + cleanerWorkers.size());
        System.out.println("Невыметенных трупов: " + deadCorpses.size());

        System.out.println("\n--- Баланс мёда ---");
        System.out.println("Всего собрано: " + roundTo2(totalHoneyProduced));
        System.out.println("Всего потреблено: " + roundTo2(totalHoneyConsumed));
        double diff = totalHoneyProduced - totalHoneyConsumed;
        System.out.println("Разница: " + roundTo2(diff));

        System.out.println("\n--- Эффективность трутней ---");
        int totalFertilized = 0;
        for (int i = 0; i < drones.size(); i++) {
            totalFertilized += drones.get(i).getEggsFertilizedTotal();
        }
        System.out.println("Трутней живых: " + drones.size());
        System.out.println("Всего оплодотворено яйцеклеток: " + totalFertilized);
        if (drones.size() > 0 && larvae.size() > 50) {
            System.out.println("=> Трутней недостаточно (много личинок ждёт)");
        } else if (drones.size() > 15 && larvae.size() < 5) {
            System.out.println("=> Трутней избыток (простаивают)");
        } else {
            System.out.println("=> Количество трутней в норме");
        }

        int totalHunger = deathsFromHungerLarvae + deathsFromHungerDrones
                + deathsFromHungerHoneyWorkers + deathsFromHungerCleanerWorkers;
        int totalAliveNow = drones.size() + larvae.size() + honeyWorkers.size() + cleanerWorkers.size() + queenAlive;
        int totalEver = totalAliveNow + totalHunger;
        System.out.println("\n--- Смерти от голода ---");
        System.out.println("Личинки: " + deathsFromHungerLarvae);
        System.out.println("Трутни: " + deathsFromHungerDrones);
        System.out.println("Сборщицы мёда: " + deathsFromHungerHoneyWorkers);
        System.out.println("Уборщицы: " + deathsFromHungerCleanerWorkers);
        System.out.println("Итого умерло от голода: " + totalHunger);
        if (totalEver > 0) {
            double pct = (totalHunger * 100.0) / totalEver;
            System.out.println("Доля смертей от голода: " + roundTo2(pct) + "%");
        }

        System.out.println("\n--- Простаивающие уборщицы ---");
        System.out.println("Уборщиц простаивает (последний шаг): " + idleCleanerCount);

        System.out.println("===========================");
    }

    public void showAllBees() {
        System.out.println("\n--- Все пчёлы в улье ---");
        if (queen != null && queen.isAlive()) {
            queen.print();
        }
        for (int i = 0; i < drones.size(); i++) drones.get(i).print();
        for (int i = 0; i < larvae.size(); i++) larvae.get(i).print();
        for (int i = 0; i < honeyWorkers.size(); i++) honeyWorkers.get(i).print();
        for (int i = 0; i < cleanerWorkers.size(); i++) cleanerWorkers.get(i).print();
        System.out.println("--- Невыметенные трупы ---");
        for (int i = 0; i < deadCorpses.size(); i++) deadCorpses.get(i).print();
    }

    private double roundTo2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public void save() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile));
            bw.write(currentStep + ";" + honeyStock + ";" + nextId + "\n");
            if (queen != null && queen.isAlive()) {
                bw.write("queen;" + queen.getId() + ";" + queen.getWeight() + ";" + queen.getAge() + "\n");
            } else {
                bw.write("none\n");
            }
            bw.write(drones.size() + "\n");
            for (int i = 0; i < drones.size(); i++) {
                Drone d = drones.get(i);
                bw.write(d.getId() + ";" + d.getWeight() + ";" + d.getAge() + "\n");
            }
            bw.write(larvae.size() + "\n");
            for (int i = 0; i < larvae.size(); i++) {
                Larva l = larvae.get(i);
                bw.write(l.getId() + ";" + l.getWeight() + ";" + l.getAge() + ";" + l.getDaysAsLarva() + "\n");
            }
            bw.write(honeyWorkers.size() + "\n");
            for (int i = 0; i < honeyWorkers.size(); i++) {
                HoneyWorker hw = honeyWorkers.get(i);
                bw.write(hw.getId() + ";" + hw.getWeight() + ";" + hw.getAge() + "\n");
            }
            bw.write(cleanerWorkers.size() + "\n");
            for (int i = 0; i < cleanerWorkers.size(); i++) {
                CleanerWorker cw = cleanerWorkers.get(i);
                bw.write(cw.getId() + ";" + cw.getWeight() + ";" + cw.getAge() + "\n");
            }
            bw.close();
            System.out.println("Сохранено в файл: " + dataFile);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    public boolean load() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(dataFile));
            String line = br.readLine();
            String[] head = line.split(";");
            currentStep = Integer.parseInt(head[0]);
            honeyStock = Double.parseDouble(head[1]);
            nextId = Integer.parseInt(head[2]);

            line = br.readLine();
            if (line.equals("none")) {
                queen = null;
            } else {
                String[] p = line.split(";");
                queen = new Queen(Integer.parseInt(p[1]), Double.parseDouble(p[2]), Integer.parseInt(p[3]));
            }
            int n = Integer.parseInt(br.readLine());
            for (int i = 0; i < n; i++) {
                String[] p = br.readLine().split(";");
                drones.add(new Drone(Integer.parseInt(p[0]), Double.parseDouble(p[1]), Integer.parseInt(p[2])));
            }
            n = Integer.parseInt(br.readLine());
            for (int i = 0; i < n; i++) {
                String[] p = br.readLine().split(";");
                Larva l = new Larva(Integer.parseInt(p[0]), Double.parseDouble(p[1]), Integer.parseInt(p[2]));
                int days = Integer.parseInt(p[3]);
                for (int k = 0; k < days; k++) l.incrementDaysAsLarva();
                larvae.add(l);
            }
            n = Integer.parseInt(br.readLine());
            for (int i = 0; i < n; i++) {
                String[] p = br.readLine().split(";");
                honeyWorkers.add(new HoneyWorker(Integer.parseInt(p[0]), Double.parseDouble(p[1]), Integer.parseInt(p[2])));
            }
            n = Integer.parseInt(br.readLine());
            for (int i = 0; i < n; i++) {
                String[] p = br.readLine().split(";");
                cleanerWorkers.add(new CleanerWorker(Integer.parseInt(p[0]), Double.parseDouble(p[1]), Integer.parseInt(p[2])));
            }

            br.close();
            System.out.println("Загружено из файла: " + dataFile);
            return true;
        } catch (IOException e) {
            System.out.println("Файл сохранения не найден. Создаём новый улей.");
            return false;
        }
    }
}
