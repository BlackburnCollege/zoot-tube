package zoot.tube.googleapi;

public class NoRefreshTokenFound extends RuntimeException {

    public NoRefreshTokenFound() {
        super("No refresh token found.");
    }
}
