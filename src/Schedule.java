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
    private List<String> shipNames;

    public Schedule(int[] nUnloaders) {
        shipNames = readNames();
        List<ScheduleEvent> list = new LinkedList<>();
        for(CargoType cargoType:CargoType.values()) {
            for(int i=0;i<nUnloaders[cargoType.ordinal()];i++) {
                Data soonestNewStart = new Data(0);
                int tryesBeforeBreak = 10;
                while (tryesBeforeBreak > 0) {
                    Ship ship = genRandomShip(cargoType);
                    ScheduleEvent event = new ScheduleEvent(ship, genRandomData(soonestNewStart));
                    if (event.getEndingData().getDay() <= endOfSchedule.getDay()) {
                        list.add(event);
                        soonestNewStart = new Data(event.getEndingData().toMinutes() + minEventDelay.toMinutes());
                    } else {
                        tryesBeforeBreak--;
                    }
                }
            }
        }
        nEvents = list.size();
        schedule = list.toArray(new ScheduleEvent[nEvents]);
        Arrays.sort(schedule, new SortByStartingTime());
    }

    public Schedule(ScheduleEvent[] events) {
        shipNames = readNames();
        nEvents = events.length;
        schedule = new ScheduleEvent[nEvents];
        for (int i = 0; i < nEvents; i++) {
            if (events[i].getStartingData().getDay() > endOfSchedule.getDay() ||
                    events[i].getStartingData().getDay() < 1) {
                System.err.printf("Wrong day: %d%n", i);
            }
            schedule[i] = new ScheduleEvent(events[i]);
        }
        Arrays.sort(schedule, new SortByStartingTime());
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
        Collections.addAll(names, JSONService.loadShipNames());
        return names;
    }

    private Ship genRandomShip(CargoType cargoType) {
        final String codeMembers = "1234567890QWERTYUIOPASDFGHJKLZXCVBNM";
        final int chanceOfCode = 5;
        String name;
        if ((shipNames.size() == 0) || (Rand.genInt(100) < chanceOfCode)) {
            StringBuilder string = new StringBuilder();
            for (int i = 0; i < 2; i++)
                string.append(codeMembers.charAt(Rand.genInt(10, codeMembers.length() - 1)));
            string.append('-');
            for (int i = 0; i < 4; i++) string.append(codeMembers.charAt(Rand.genInt(10)));
            name = string.toString();
        } else {
            int index = Rand.genInt(shipNames.size());
            name = shipNames.get(index);
            shipNames.remove(index);
        }
        int cargoAmount;
        if (cargoType != CargoType.CONTAINERS) cargoAmount = Rand.genInt(100, 7000);
        else cargoAmount = Rand.genInt(10, 300);
        return new Ship(name, cargoType, cargoAmount);
    }

    public static void main(int[] nUnloaders) {
        Schedule schedule = new Schedule(nUnloaders);
        JSONService.saveSchedule(schedule);
//        System.out.println(schedule);
    }
}
