package com.tank2d.masterserver.core;

import com.tank2d.masterserver.db.AccountRepository;

public class AccountManager {
    private static final AccountRepository repo = new AccountRepository();

    public static boolean login(String username, String password) {
        return repo.login(username, password);
    }

    public static boolean register(String username, String password) {
        return repo.register(username, password);
    }
}