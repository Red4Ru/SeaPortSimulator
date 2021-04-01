public class Data {
    private final int day;
    private final Time time;

    public Data(int day, Time time) {
        this((day - 1) * 24 * 60 + time.toMinutes());
    }

    public Data(int minutes) {
        this.day = minutes / 60 / 24 + 1;
        this.time = new Time(minutes % (60 * 24));
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
        return (day - 1) * 24 * 60 + time.toMinutes();
    }
}
