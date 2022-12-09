package us.zoom.sdkexample2;

public interface Constants {

	// TODO Change it to your web domain
    String WEB_DOMAIN = "zoom.us";

	// TODO change it to your user ID
    String USER_ID = "SwuCv9uHTjK-vJNY72eG2g";
	
	// TODO change it to your token
    String ZOOM_ACCESS_TOKEN = "";
	
	// TODO Change it to your exist meeting ID to start meeting
    String MEETING_ID = "84912303738";

    /**
     * We recommend that, you can generate jwttoken on your own server instead of hardcore in the code.
     * We hardcore it here, just to run the demo.
     *
     * You can generate a jwttoken on the https://jwt.io/
     * with this payload:
     * {
     *     "appKey": "string", // app key
     *     "iat": long, // access token issue timestamp
     *     "exp": long, // access token expire time
     *     "tokenExp": long // token expire time
     * }
     */
    public final static String SDK_JWTTOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOm51bGwsImlzcyI6IlFyWUw4YWM4UlBTQTB2WnNya0J2YnciLCJleHAiOjE2NzA1NzA5MzQsImlhdCI6MTY3MDQ4NDUzNH0.wCdEp27-Sg7u6XH75Dk3ciHbno10p-68S6LKIw__X0A";

}
