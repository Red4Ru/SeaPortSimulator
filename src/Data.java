public class Data {
    public static final int MM_IN_HH = Time.MM_IN_HH, HH_IN_DAY = 24, FIRST_DAY = 1;
    private final int day;
    private final Time time;

    public Data(int day, Time time) {
        this((day - FIRST_DAY) * HH_IN_DAY * MM_IN_HH + time.toMinutes());
    }

    public Data(int minutes) {
        this.day = minutes / MM_IN_HH / HH_IN_DAY + FIRST_DAY;
        this.time = new Time(minutes % (MM_IN_HH * HH_IN_DAY));
    }

    public Data(Data data) {
        this(data.toMinutes());
    }

    @Override
    public String toString() {
        return String.format("Day %d, ", day) + time;
    }

    public int getDay() {
        return day;
    }

    public Time getTime() {
        return new Time(time);
    }

    public int toMinutes() {
        return (day - FIRST_DAY) * HH_IN_DAY * MM_IN_HH + time.toMinutes();
    }
}
