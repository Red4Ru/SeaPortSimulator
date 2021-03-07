import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONConverter {

    public static JSONObject toJSON(Schedule schedule) {
        JSONObject out = new JSONObject();
        JSONArray scheduleEvents = new JSONArray();
        for (int i = 0; i < schedule.getEventsNumber(); i++) {
            scheduleEvents.add(toJSON(schedule.getNthEvent(i)));
        }
        out.put("schedule", scheduleEvents);
        return out;
    }

    public static Schedule scheduleFromJSON(JSONObject jsonObject) {
        JSONArray scheduleEvents = (JSONArray) jsonObject.get("schedule");
        int nEvents = scheduleEvents.size();
        ScheduleEvent[] events = new ScheduleEvent[nEvents];
        for (int i = 0; i < nEvents; i++) {
            events[i] = scheduleEventFromJSONArray(scheduleEvents, i);
        }
        return new Schedule(events);
    }

    private static JSONObject toJSON(ScheduleEvent event) {
        JSONObject out = new JSONObject();
        out.put("ship", toJSON(event.getInvolvedShip()));
        out.put("startingData", toJSON(event.getStartingData()));
        out.put("unloadingTime", toJSON(event.getUnloadingTime()));
        return out;
    }

    private static ScheduleEvent scheduleEventFromJSONArray(JSONArray array, int i) {
        JSONObject jsonObject = (JSONObject) array.get(i);
        return new ScheduleEvent(shipFromJSON(jsonObject), dataFromJSON(jsonObject, "startingData"),
                timeFromJSON(jsonObject, "unloadingTime")
        );
    }

    private static JSONObject toJSON(Ship ship) {
        JSONObject out = new JSONObject();
        out.put("name", ship.getName());
        out.put("cargoType", ship.getCargoType().name());
        out.put("cargoAmount", ship.getCargoAmount());
        return out;
    }

    private static Ship shipFromJSON(JSONObject jsonObject) {
        jsonObject = (JSONObject) jsonObject.get("ship");
        return new Ship(stringFromJSON(jsonObject, "name"),
                cargoTypeFromJSON(jsonObject),
                intFromJSON(jsonObject, "cargoAmount"));
    }

    private static String stringFromJSON(JSONObject jsonObject, String key) {
        return (String) jsonObject.get(key);
    }

    private static CargoType cargoTypeFromJSON(JSONObject jsonObject) {
        String typeName = stringFromJSON(jsonObject, "cargoType");
        for (CargoType cargoType : CargoType.values()) {
            if (cargoType.name().equals(typeName)) {
                return cargoType;
            }
        }
        System.err.println("Inappropriate CargoType in JSON");
        return CargoType.LIQUID;
    }

    private static JSONObject toJSON(Data data) {
        JSONObject out = new JSONObject();
        out.put("day", data.getDay());
        out.put("time", toJSON(data.getTime()));
        return out;
    }

    private static Data dataFromJSON(JSONObject jsonObject, String key) {
        jsonObject = (JSONObject) jsonObject.get(key);
        return new Data(intFromJSON(jsonObject, "day"),
                timeFromJSON(jsonObject, "time"));
    }

    private static int intFromJSON(JSONObject jsonObject, String key) {
        return ((Long) jsonObject.get(key)).intValue();
    }

    private static JSONObject toJSON(Time time) {
        JSONObject out = new JSONObject();
        out.put("hours", time.getHours());
        out.put("minutes", time.getMinutes());
        return out;
    }

    private static Time timeFromJSON(JSONObject jsonObject, String key) {
        jsonObject = (JSONObject) jsonObject.get(key);
        return new Time(intFromJSON(jsonObject, "hours"),
                intFromJSON(jsonObject, "minutes"));
    }
}
