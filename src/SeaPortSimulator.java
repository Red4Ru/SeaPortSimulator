import java.util.Random;

public class SeaPortSimulator {
    private final Time absArrivalDeviation = new Time(24 * 7, 0);
    private final Time unloadingDeviation = new Time(0, 1440);
    private Schedule originalSchedule;
    private Schedule actualSchedule;
    private int nEvents;

    public SeaPortSimulator() {
        originalSchedule = ScheduleJSONService.loadSchedule();
        nEvents = originalSchedule.getEventsNumber();
        setActualSchedule();
    }

    private void setActualSchedule() {
        ScheduleEvent[] actualEvents = new ScheduleEvent[nEvents];
        for (int i = 0; i < nEvents; i++) {
            actualEvents[i] = toActualEvent(originalSchedule.getNthEvent(i));
        }
        actualSchedule = new Schedule(actualEvents);
    }

    private ScheduleEvent toActualEvent(ScheduleEvent event) {
        Random random = new Random();
        Data start = event.getStartingData();
        Data start_min = new Data(start.toMinutes() - absArrivalDeviation.toMinutes());
        if (start_min.toMinutes() < 0) {
            start_min = new Data(0);
        }
        Data start_max = new Data(start.toMinutes() + absArrivalDeviation.toMinutes());
        if (start_max.toMinutes() >= originalSchedule.endOfSchedule.getDay() * 24 * 60) {
            start_max = new Data(originalSchedule.endOfSchedule.getDay() * 24 * 60 - 1);
        }
        //start_min.toMinutes() + random.nextInt(start_max.toMinutes()- start_min.toMinutes() + 1);
        return new ScheduleEvent(new Ship("",CargoType.LIQUID,0),new Data(0));//TODO
    }
}
