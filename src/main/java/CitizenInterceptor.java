import org.apache.ignite.configuration.ConnectorMessageInterceptor;
import org.jetbrains.annotations.Nullable;

/**
 * Interceptor for transform request parameter string to POJO and from POJO to string.
 */
public class CitizenInterceptor implements ConnectorMessageInterceptor {
    /**
     * Catch storing objects before it will drop to cache.
     * @param obj string parameter from http request.
     * @return POJO Citizen.
     */
    @Nullable
    public Object onReceive(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        if (!obj.toString().contains(",")) {
            return Integer.parseInt(obj.toString());
        }
        System.out.println("Received: " + obj.toString());
        if (obj.toString().split(",").length == 4) {
            System.out.println("Recivied: " + obj.toString());
            String s = obj.toString();
            String[] params = s.split(",");
            Citizen citizen = new Citizen();
            citizen.id = Long.parseLong(params[0]);
            citizen.passportNumber = Integer.parseInt(params[1]);
            citizen.month = Integer.parseInt(params[2]);
            citizen.index = Integer.parseInt(params[3]);
            return citizen;
        }
        return obj;
    }

    @Nullable
    public Object onSend(Object obj) {
        return obj.toString();
    }
}
