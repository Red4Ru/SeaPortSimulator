import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class JSONService {
    private static final String scheduleFileName = System.getProperty("user.dir") + "/schedule.json";
    private static final String shipNamesFileName = System.getProperty("user.dir") + "/ship_names.json";

    public static void addScheduleEvents(ScheduleEvent[] events) {
        Schedule schedule = loadSchedule();
        int nEvents = schedule.getEventsNumber();
        int nEventsNew = nEvents + events.length;
        ScheduleEvent[] scheduleEventsNew = new ScheduleEvent[nEventsNew];
        for (int i = 0; i < nEventsNew; i++) {
            if (i < nEvents) {
                scheduleEventsNew[i] = schedule.getNthEvent(i);
            } else {
                scheduleEventsNew[i] = events[i - nEvents];
            }
        }
        schedule = new Schedule(scheduleEventsNew);
        saveSchedule(schedule);
    }

    public static Schedule loadSchedule() {
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(scheduleFileName));
            return JSONConverter.scheduleFromJSON(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Schedule();
    }

    public static void saveSchedule(Schedule schedule) {
        try {
            FileWriter file = new FileWriter(scheduleFileName);
            file.write(JSONConverter.toJSON(schedule).toJSONString());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] loadShipNames() {
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(shipNamesFileName));
            return JSONConverter.shipNamesFromJSON(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{};
    }
}
