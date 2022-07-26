public class GameEvent {
    public enum EventType {
        SHOW_MESSAGE,
        PLAY_SOUND
    }

    private final EventType type;
    private final String message;

    public GameEvent(EventType type, String message) {
        this.type = type;
        this.message = message;
    }

    public EventType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}