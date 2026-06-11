package facoffe.users.event;

public class UserDeactivatedPayload {
    private String userId;
    private String reason;

    public UserDeactivatedPayload(String userId, String reason) {
        this.userId = userId;
        this.reason = reason;
    }

    // Getters e Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}