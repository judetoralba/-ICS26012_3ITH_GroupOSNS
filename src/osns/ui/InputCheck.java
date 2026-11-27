package osns.ui;

/**
 * Input validation used by every screen. Turns text into a whole number or
 * throws an exception with a clear message (shown to the user, never a crash).
 * Group OSNS - shared UI class.
 */
public class InputCheck {

    public static int parse(Object value, String field, int min, int max) {
        String text = value == null ? "" : value.toString().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(field + " is empty. Please enter a whole number.");
        }
        int number;
        try {
            number = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a whole number (you entered \"" + text + "\").");
        }
        if (number < 0 && min >= 0) {
            throw new IllegalArgumentException(field + " cannot be negative.");
        }
        if (number < min || number > max) {
            throw new IllegalArgumentException(field + " must be from " + min + " to " + max + ".");
        }
        return number;
    }
}
