package arekkuusu.enderskills.api.configuration;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class DSLFactory {

    public static DSL create(String defaultFileName) {
        String location = defaultFileName.replace("enderskills", "defaults");
        List<String> stringList = Lists.newLinkedList();

        InputStream resourceAsStream = DSLFactory.class.getClassLoader().getResourceAsStream(location + ".cfg");

        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringList.add(line);
            }
        } catch (Exception ignored) {
        }

        return new DSL(stringList.toArray(new String[0]));
    }
}
