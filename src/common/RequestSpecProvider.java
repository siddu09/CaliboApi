package common;

public class RequestSpecProvider {

    private static RequestSpecification requestSpecification;

    public static void initialize(){

        requestSpecification =
                new RequestSpecBuilder()

                        .setBaseUri(Configuration.baseUrl)

                        .addHeader("Authorization",
                                "Bearer "
                                        + Configuration.accessToken)

                        .addHeader("x-tenantid",
                                Configuration.tenantId)

                        .setContentType(ContentType.JSON)

                        .build();

    }

    public static RequestSpecification get(){

        return requestSpecification;

    }

}