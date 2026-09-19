package in.lekhai.cucumber;

import org.springframework.test.context.ActiveProfilesResolver;

public class CucumberDbProfileResolver implements ActiveProfilesResolver {

    static final String LOCAL_DB_PROFILE = "cucumber-local";

    @Override
    public String[] resolve(Class<?> testClass) {
        String mode = System.getProperty("cucumber.db", "container");
        return switch (mode) {
            case "container" -> new String[]{"test"};
            case "local" -> new String[]{"test", LOCAL_DB_PROFILE};
            default -> throw new IllegalArgumentException(
                    "Unknown cucumber.db mode '" + mode + "'; expected 'container' or 'local'");
        };
    }
}
