public class SeaPortSimulator {
    private final Time absArrivalDeviation = new Time(24 * 7, 0);
    private final Time unloadingDeviation = new Time(0, 1440);
    private Schedule originalSchedule;
    private Schedule actualSchedule;

    public SeaPortSimulator(Schedule schedule) {
        setSchedule(schedule);
    }

    private void setSchedule(Schedule schedule) {
        originalSchedule = schedule;
        int nEvents = originalSchedule.getEventsNumber();
        ScheduleEvent[] actualEvents = new ScheduleEvent[nEvents];
        for (int i = 0; i < nEvents; i++) {
            actualEvents[i] = toActualEvent(originalSchedule.getNthEvent(i));
        }
        actualSchedule = new Schedule(actualEvents);
    }

    private ScheduleEvent toActualEvent(ScheduleEvent event) {
        Data start = event.getStartingData();
        Data start_min = new Data(Math.max(0, start.toMinutes() - absArrivalDeviation.toMinutes()));
        Data start_max = new Data(Math.min(originalSchedule.endOfSchedule.getDay() * 24 * 60 - 1,
                start.toMinutes() + absArrivalDeviation.toMinutes()));
        Data actualStart = new Data(Rand.genNormalInt(event.getStartingData().toMinutes(),
                start_min.toMinutes(), start_max.toMinutes()));
        Time actualUnloadingTime = new Time(Math.abs(Rand.genNormalInt(event.getUnloadingTime().toMinutes(),
                event.getUnloadingTime().toMinutes(), unloadingDeviation.toMinutes())));
        return new ScheduleEvent(event.getInvolvedShip(), actualStart, actualUnloadingTime);
    }

    public void printActualSchedule() {
        System.out.println(actualSchedule);
    }

    public void printOriginalSchedule() {
        System.out.println(originalSchedule);
    }
}
