import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class JSONService {
    private static String getScheduleFileName(int[] nUnloaders) {
        StringBuilder string = new StringBuilder(System.getProperty("user.dir") + "/schedule");
        for (int n : nUnloaders) {
            string.append("_").append(n);
        }
        string.append(".json");
        return string.toString();
    }

    public static void addScheduleEvents(ScheduleEvent[] events, int[] nUnloaders) {
        Schedule schedule = loadSchedule(nUnloaders);
        if (schedule == null) {
            System.err.println("Can't download, nothing added");
            return;
        }
        int nEvents = schedule.getEventsNumber();
        int nEventsNew = nEvents + events.length;
        ScheduleEvent[] scheduleEventsNew = new ScheduleEvent[nEventsNew];
        for (int i = 0; i < nEventsNew; i++) {
            if (i < nEvents) {
                scheduleEventsNew[i] = schedule.getNthEvent(i);
            } else {
                scheduleEventsNew[i] = new ScheduleEvent(events[i - nEvents]);
            }
        }
        schedule = new Schedule(scheduleEventsNew);
        saveSchedule(schedule, nUnloaders);
    }

    public static Schedule loadSchedule(int[] nUnloaders) {
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(getScheduleFileName(nUnloaders)));
            return loadSchedule(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Schedule loadSchedule(String raw) {
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(raw);
            return loadSchedule(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Schedule loadSchedule(JSONObject jsonObject) {
        return JSONConverter.scheduleFromJSON(jsonObject);
    }

    public static void saveSchedule(Schedule schedule, int[] nUnloaders) {
        try {
            FileWriter file = new FileWriter(getScheduleFileName(nUnloaders));
            file.write(JSONConverter.toJSON(schedule).toJSONString());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] loadShipNames() {
        final String shipNamesFileName = System.getProperty("user.dir") + "/ship_names.json";
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(shipNamesFileName));
            return JSONConverter.shipNamesFromJSON(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
