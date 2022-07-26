import java.util.Set;
import java.util.EnumSet;

// Enum to represent additional images that are not included by the base game graphics
public enum ExtraGraphicImageTypes {
    HERO1_TREASURE,
    HERO2_TREASURE,
    HERO1_HERO2,
    HERO2_HERO1;

    private static final Set<ExtraGraphicImageTypes> COMPOSITE_GRAPHICS = EnumSet.of(HERO1_TREASURE, HERO2_TREASURE, HERO1_HERO2, HERO2_HERO1);

    // Returns true if the image is a composite image and will be generated from other images
    public boolean isComposite() {
        return COMPOSITE_GRAPHICS.contains(this);
    }
}
