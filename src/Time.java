public class Time {
    private final int hours;
    private final int minutes;

    public Time(int hours, int minutes) {
        this(hours * 60 + minutes);
    }

    public Time(int minutes) {
        this.hours = minutes / 60;
        this.minutes = minutes % 60;
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
        return hours * 60 + minutes;
    }
}
