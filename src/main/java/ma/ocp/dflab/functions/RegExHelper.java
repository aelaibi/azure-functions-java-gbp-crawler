package ma.ocp.dflab.functions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aelaibi on 13/06/2017.
 */
public class RegExHelper {

    public static String findFirst(String regex, String input, boolean ignoreCase, int groupIndex) {
        Matcher matcher = Pattern.compile(regex,ignoreCase? Pattern.CASE_INSENSITIVE:0)
                .matcher(input);
        if (matcher.find()){//find first one
            return matcher.group(groupIndex);
        }
        return "";
    }
}
