package in.lekhai.cucumber.context;

import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TestContext {
    private Response response;
    private final Map<String, Object> sessionData = new HashMap<>();

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public void set(String key, Object value) {
        sessionData.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) sessionData.get(key);
    }

    public void clear() {
        response = null;
        sessionData.clear();
    }
}
