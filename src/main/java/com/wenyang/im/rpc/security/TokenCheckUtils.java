package com.wenyang.im.rpc.security;

/**
 * @Author wen.yang
 */
public class TokenCheckUtils {

    public static boolean checkValid(String clientId, String username, byte[] password) {
        String id = Tokenor.getUserId(password);
        if (id != null && id.equals(username)) {
            return true;
        }
        return false;
    }
}
