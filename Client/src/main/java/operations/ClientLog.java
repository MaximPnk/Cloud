package operations;

import graphics.Window;

public class ClientLog {
    public static void log(String str) {
        Window.getController().logArea.appendText(str + System.lineSeparator());
    }
}
