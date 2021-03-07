import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

class SortByStartingTime implements Comparator<ScheduleEvent> {
    @Override
    public int compare(ScheduleEvent o1, ScheduleEvent o2) {
        return o1.getStartingData().toMinutes() - o2.getStartingData().toMinutes();
    }
}

public class Schedule {
    public final Data endOfSchedule = new Data(30, new Time(0));
    private final int nEvents;
    private final ScheduleEvent[] schedule;
    private final Random random;

    public Schedule() {
        random = new Random();
        nEvents = genRandomInt(50, 100);
        schedule = new ScheduleEvent[nEvents];
        for (int i = 0; i < nEvents; i++) {
            schedule[i] = new ScheduleEvent(genRandomShip(), genRandomData());
        }
        Arrays.sort(schedule, new SortByStartingTime());
    }

    public Schedule(ScheduleEvent[] events) {
        random = new Random();
        nEvents = events.length;
        schedule = new ScheduleEvent[nEvents];
        for (int i = 0; i < nEvents; i++) {
            if (events[i].getStartingData().getDay() > endOfSchedule.getDay() ||
                    events[i].getStartingData().getDay() < 1) {
                System.err.printf("Wrong day: %d%n", i);
            }
            schedule[i] = events[i];
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
        return schedule[n];
    }

    private int genRandomInt(int start, int end) {
        return start + random.nextInt(end - start + 1);
    }

    private Data genRandomData() {
        return new Data(random.nextInt(endOfSchedule.getDay() * 24 * 60));
    }

    private Ship genRandomShip() {
        String[] names = {
                "Alexandria", "Argonaut", "Aurora", "Altair", "Arcadia", "Alice May", "Astrea", "Antaeus", "Alicorn",
                "Bebop", "Borneo Prince", "Barracuda", "Black Hawk", "Brandenburg", "Baalbek", "Brave Joffrey",
                "Chancellor", "Cithara", "Cassidy", "Chelsea", "Covenant", "Compass Rose", "Calypso", "Cantwell",
                "Decolore", "Defiant", "Dragonfish", "Dazzler", "Duncan",
                "Eagle's Shadow", "Elizabeth Dane", "Empress", "Endeavour", "Essess", "Erebus",
                "Fenton", "Fin of God", "Franklin",
                "Gran Tesoro", "Grossadler", "Genesis", "Ghost", "Goliath",
                "Hawksub", "Hahnchen Maru", "Hesperus",
                "Illustria", "Inferno", "Interceptor", "Indra", "Ilya Podogin", "Independence",
                "Jenny", "Jolly Roger",
                "Karaboudjan", "Korund",
                "Love Nest", "Liparus", "Laughing Sandbag", "Leviathan", "Liberian Star",
                "Mermaid", "Mirai", "Moby Dick", "Morning Star", "Montana", "Marie Celeste", "Milka", "Mortzestus",
                "Naked Sun", "Nautilus", "Neptune", "Nemesis", "Numestra del Oro",
                "Odessa", "Orca", "Okinawa", "Onward",
                "Pascal Magi", "Poseidon", "Princess Irene", "Proteus", "Penguin", "Pocahontas", "Pushkin", "Pandora",
                "Queen Anne's Revenge",
                "Reaper", "Red October", "Red Witch", "Roland", "Rocketing Spitfire",
                "Spiral", "St. Aphrodite", "Sirius", "Sherwood", "Saturn", "Skyline", "Sutherland", "Scorpion", "Siren",
                "Twelve Apostles", "Thunderer", "Titanic", "Tristram", "Thomas Jefferson", "Trident", "Tempest",
                "Unicorn", "Utah", "Undine", "Unnamed",
                "Vulkan", "Valhalla", "Vengeance", "Venus", "Vortex",
                "White Castle", "Wolfgang", "Warhammer", "Witch of Endor", "Wasp",
                "X-2",
                "Yashiromaru", "Yellow Submarine",
                "Zuko's Fire Nation ship", "Zelbess"
        };//from https://en.wikipedia.org/wiki/List_of_fictional_ships, length - 127
        String name;
        while (true) {
            name = names[random.nextInt(names.length)];
            boolean unique_name = true;
            for (ScheduleEvent event : schedule) {
                if (event == null) continue;
                if (event.getInvolvedShip().getName().equals(name)) {
                    unique_name = false;
                    break;
                }
            }
            if (unique_name) break;
        }
        CargoType cargoType = CargoType.values()[random.nextInt(CargoType.values().length)];
        int cargoAmount;
        if (cargoType != CargoType.CONTAINERS) cargoAmount = genRandomInt(100, 7000);
        else cargoAmount = genRandomInt(10, 300);
        return new Ship(name, cargoType, cargoAmount);
    }

    public static void main(String[] args) {
        Schedule schedule = new Schedule();
        System.out.println(schedule);
        ScheduleJSONService.saveSchedule(schedule);
        System.out.println("Saved");
        schedule = ScheduleJSONService.loadSchedule();
        System.out.println("Loaded");
        System.out.println(schedule);
    }
}
