package feiq;

import java.io.*;
class MySQL {
    private User user;
    private BufferedWriter addUsername;
    private BufferedWriter addPassword;
    private BufferedReader findUsername;
    private File username;
    private File password;

    MySQL() throws IOException {
        init();
    }

    private void init() throws IOException {
        user = new User(null, null);
        username = new File("E:\\Javase\\FlynnSQL\\Administrator\\Users\\admin\\user\\username.txt");
        password = new File("E:\\Javase\\FlynnSQL\\Administrator\\Users\\admin\\user\\password.txt");
        if (!(new File("E:\\Javase\\FlynnSQL\\Administrator\\Users\\admin\\user").exists())) {
            new File("E:\\Javase\\FlynnSQL\\Administrator\\Users\\admin\\user").mkdirs();
            if (!username.exists()) {
                boolean result1 = username.createNewFile();
                boolean result2 = password.createNewFile();
                addUsername = new BufferedWriter(new FileWriter(username));
                addPassword = new BufferedWriter(new FileWriter(password));
                addUsername.write("admin");
                addUsername.newLine();
                addPassword.write(md5("admin"));
                addPassword.newLine();
                addUsername.flush();
                addPassword.flush();
                addUsername.close();
                addPassword.close();
            }
        }
    }

    private boolean signUp(String username,String password) throws IOException {
        user.username = username;
        if (user.username != null && !user.username.contains(" ")) {
            findUsername = new BufferedReader(new FileReader(username));
            String getUsername = null;
            while ((getUsername = findUsername.readLine()) != null) {
                if (getUsername.equals(user.username)) {
                    findUsername.close();
                    return false;
                }
            }
            findUsername.close();
        } else {
            return false;
        }

        if (user.password != null) {
            addUsername = new BufferedWriter(new FileWriter(username, true));
            addPassword = new BufferedWriter(new FileWriter(password, true));
            addUsername.write(user.username);
            addUsername.newLine();
            addUsername.flush();
            addUsername.close();
            addPassword.write(md5(user.password));
            addPassword.newLine();
            addPassword.flush();
            addPassword.close();
            return true;
        } else {
            return false;
        }
    }

    public boolean logIn(String user_log,String pass_log) throws Exception {
        user.username = user_log;
        user.password = pass_log;
        findUsername = new BufferedReader(new FileReader(username));
        String usernameFound = null;
        int usernameIndex = 0;
        boolean isFound = false;
        while ((usernameFound = findUsername.readLine()) != null) {
            usernameIndex++;
            if (usernameFound.equals(user.username)) {
                isFound = true;
                break;
            }
        }
        findUsername.close();
        if (isFound) {
            BufferedReader findPassword = new BufferedReader(new FileReader(password));
            String passwordFound = null;
            int passwordIndex = 0;
            while ((passwordFound = findPassword.readLine()) != null) {
                passwordIndex++;
                if (passwordIndex == usernameIndex)
                    break;
            }
            findPassword.close();
            return passwordFound.equals(md5(user.password));
        }
        return false;
    }

    private String md5(String password) {
        String result = "";
        for (int i = 0; i < password.length(); i++) {
            result = result + (int) (Math.pow(2 * (int) (password.toCharArray()[i]), 2) + Math.pow(i, password.length() - i)) % 1000;
        }
        return result;
    }

    static class User {
        private String username;
        private String password;

        User(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
