package com.users.papi.internal;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
	
	private static final int SALT_ROUNDS = 12;
	
	public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(SALT_ROUNDS));
    }

    public static boolean checkPassword(String password, String hashed) {
        try {
            return BCrypt.checkpw(password, hashed);
        } catch (Exception e) {
            return false;
        }
    }

}
