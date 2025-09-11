package com.example.convenient.Utils;

public class StringHelper {
    //Set regular pattern for email
    public static boolean isValidEmail(String email) {
        //Set pattern:
        String emailPattern = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        return email.matches(emailPattern);

    }
}
