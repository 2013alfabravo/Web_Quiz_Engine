package engine;

@SuppressWarnings("unused")
public class Result {
    public static final Result CORRECT = new Result(true, "Congratulations, you're right!");
    public static final Result FAIL = new Result(false, "Wrong answer! Please, try again.");

    private boolean success;
    private String feedback;

    public Result() {  }

    public Result(boolean success, String feedback) {
        this.success = success;
        this.feedback = feedback;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
