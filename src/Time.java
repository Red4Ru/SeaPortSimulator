public class Time {
    public static final int MM_IN_HH = 60;
    private final int hours;
    private final int minutes;

    public Time(int hours, int minutes) {
        this(hours * MM_IN_HH + minutes);
    }

    public Time(int minutes) {
        this.hours = minutes / MM_IN_HH;
        this.minutes = minutes % MM_IN_HH;
    }

    public Time(Time time) {
        this(time.toMinutes());
    }

    @Override
    public String toString() {
        String hh = Integer.toString(hours);
        if (hh.length() < 2) hh = '0' + hh;
        String mm = Integer.toString(minutes);
        if (mm.length() < 2) mm = '0' + mm;
        return hh + ':' + mm;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int toMinutes() {
        return hours * MM_IN_HH + minutes;
    }
}
