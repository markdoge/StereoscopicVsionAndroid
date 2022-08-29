package privacyPolicyTool;

public class CheckCommit {
    private static boolean userTerm = false;
    private static boolean privacyPolitics = false;

    public static void setPrivacyPolitics() {
        CheckCommit.privacyPolitics = true;
    }

    public static void setUserTerm() {
        CheckCommit.userTerm = true;
    }

    public static boolean isPrivacyPolitics() {
        return privacyPolitics;
    }

    public static boolean isUserTerm() {
        return userTerm;
    }
}
