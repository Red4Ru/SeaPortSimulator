import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

class SortByStartingTime implements Comparator<ScheduleEvent> {
    @Override
    public int compare(ScheduleEvent o1, ScheduleEvent o2) {
        return o1.getStartingData().toMinutes() - o2.getStartingData().toMinutes();
    }
}

public class Schedule {
    public static final Data endOfSchedule = new Data(30, new Time(23, 59));
    private static final Time minEventDelay = new Time(10);
    private static final Time meanEventDelay = new Time(20);
    private static final Time maxEventDelay = new Time(60);
    private final int nEvents;
    private final ScheduleEvent[] schedule;
    private final List<String> shipNames;

    public Schedule(int[] nUnloaders) {
        final int tryesBeforeBreak = 10;
        this.shipNames = readNames();
        List<ScheduleEvent> list = new LinkedList<>();
        for (CargoType cargoType : CargoType.values()) {
            for (int i = 0; i < nUnloaders[cargoType.ordinal()]; i++) {
                Data soonestNewStart = new Data(0);
                int currentTryesLast = tryesBeforeBreak;
                while (currentTryesLast > 0) {
                    Ship ship = genRandomShip(cargoType);
                    ScheduleEvent event = new ScheduleEvent(ship, genRandomData(soonestNewStart));
                    if (event.getEndingData().getDay() <= endOfSchedule.getDay()) {
                        list.add(event);
                        soonestNewStart = new Data(event.getEndingData().toMinutes() + minEventDelay.toMinutes());
                    } else {
                        currentTryesLast--;
                    }
                }
            }
        }
        this.nEvents = list.size();
        this.schedule = list.toArray(new ScheduleEvent[this.nEvents]);
        Arrays.sort(this.schedule, new SortByStartingTime());
    }

    public Schedule(ScheduleEvent[] events) {
        this.shipNames = readNames();
        this.nEvents = events.length;
        this.schedule = new ScheduleEvent[this.nEvents];
        for (int i = 0; i < this.nEvents; i++) {
            if (events[i].getStartingData().getDay() > endOfSchedule.getDay() ||
                    events[i].getStartingData().getDay() < Data.FIRST_DAY) {
                System.err.printf("Wrong day: %d%n", i);
            }
            this.schedule[i] = new ScheduleEvent(events[i]);
        }
        Arrays.sort(this.schedule, new SortByStartingTime());
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("Schedule:\n\n");
        int i = 1;
        for (ScheduleEvent event : schedule) {
            string.append(String.format("%d) %s\n\n", i, event));
            i++;
        }
        return string.toString();
    }

    public int getEventsNumber() {
        return schedule.length;
    }

    public ScheduleEvent getNthEvent(int n) {
        return new ScheduleEvent(schedule[n]);
    }

    private static Data genRandomData(Data minData) {
        return new Data(Rand.genNormalInt(
                minData.toMinutes() + meanEventDelay.toMinutes(),
                minData.toMinutes(),
                Math.min(endOfSchedule.toMinutes(), minData.toMinutes() + maxEventDelay.toMinutes())));
    }

    private static List<String> readNames() {
        //from https://en.wikipedia.org/wiki/List_of_fictional_ships, length - 127
        List<String> names = new LinkedList<>();
        String[] namesLoaded = JSONService.loadShipNames();
        if (namesLoaded == null) {
            System.err.println("Can't load, nothing to use as names");
        } else {
            Collections.addAll(names, namesLoaded);
        }
        return names;
    }

    private Ship genRandomShip(CargoType cargoType) {
        final String digits = "1234567890";
        final String letters = "QWERTYUIOPASDFGHJKLZXCVBNM";
        final int chanceOfCode = 5;//in percents
        final int lettersInCode = 2, digitsInCode = 4;

        final int minLooseTons = 5000, maxLooseTons = 10000;
        final int minLiquidTons = 1000, maxLiquidTons = 10000;
        final int minContainerAmount = 33, maxContainerAmount = 333;

        String name;
        if ((shipNames.size() == 0) || (Rand.genInt(100) < chanceOfCode)) {
            StringBuilder string = new StringBuilder();
            for (int i = 0; i < lettersInCode; i++)
                string.append(letters.charAt(Rand.genInt(letters.length())));
            string.append('-');
            for (int i = 0; i < digitsInCode; i++) string.append(digits.charAt(Rand.genInt(digits.length())));
            name = string.toString();
        } else {
            int index = Rand.genInt(shipNames.size());
            name = shipNames.get(index);
            shipNames.remove(index);
        }
        int cargoAmount;
        if (cargoType == CargoType.LOOSE) cargoAmount = Rand.genInt(minLooseTons, maxLooseTons);
        else if (cargoType == CargoType.LIQUID) cargoAmount = Rand.genInt(minLiquidTons, maxLiquidTons);
        else if (cargoType == CargoType.CONTAINERS) cargoAmount = Rand.genInt(minContainerAmount, maxContainerAmount);
        else cargoAmount = 0;//error
        return new Ship(name, cargoType, cargoAmount);
    }

    public static void main(int[] nUnloaders) {
        Schedule schedule = new Schedule(nUnloaders);
        JSONService.saveSchedule(schedule);
//        System.out.println(schedule);
    }
}
