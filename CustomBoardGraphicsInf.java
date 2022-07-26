import com.wwu.graphics.*;

// Extended BoardGraphicsInf with a callback for when the debug switch is toggled
public interface CustomBoardGraphicsInf extends BoardGraphicsInf {
    public void debugChanged();
}
