package project.uprising.User;

public class Session {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUsername() {
        return (currentUser != null && currentUser.getUsername() != null) ? currentUser.getUsername() : "Guest";
    }


    public static int getCurrentUserID() {
        return currentUser.getId();
    }
}
