package com.auction.server.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích băm và kiểm tra mật khẩu.
 */
public class PasswordUtil {
    public static String hashPassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    public static boolean checkPassword(String password, String hashed){
        return BCrypt.checkpw(password, hashed);
    }
}
