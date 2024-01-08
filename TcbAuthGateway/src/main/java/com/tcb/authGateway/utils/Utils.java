package com.tcb.authGateway.utils;

public class Utils {
    private Utils() {
    }



    public static Object createObject(String className) {
        try {
            Class clazz = Class.forName(className);
            Object obj = clazz.newInstance();
            return obj;
        }
        catch (ClassNotFoundException ex) {
            throw new RuntimeException("Create object failed.", ex);
        }
        catch (Exception ex) {
            throw new RuntimeException("Create object failed.", ex);
        }
    }

}