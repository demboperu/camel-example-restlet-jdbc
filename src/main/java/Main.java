import java.io.StringWriter;
import java.util.HashMap;

import org.json.simple.JSONValue;


public class Main {
	public static void main(String[] args) throws Exception {
		HashMap<String,String> map = new HashMap<String,String>();
        map.put("hola","hola");
        StringWriter out = new StringWriter();
        JSONValue.writeJSONString(map, out);
        String jsonText = out.toString();
        System.err.println(jsonText);
	}
}
