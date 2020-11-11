package operations;

import java.io.File;

public class GetFiles {

    public static String list() {
        String[] files = new File(ChangeDirectory.getRootPath()).list();
        assert files != null;
        return ChangeDirectory.getRootPath() + " " + String.join(" ", files);
    }
}
