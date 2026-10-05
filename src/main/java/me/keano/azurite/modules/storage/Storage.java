package me.keano.azurite.modules.storage;

import me.keano.azurite.modules.teams.Team;
import me.keano.azurite.modules.users.User;

/**
 * Copyright (c) 2025. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public interface Storage {

    void loadTeams();

    void saveTeams();

    void saveTeam(Team team, boolean async);

    void deleteTeam(Team team);

    void deleteUser(User user);

    void loadUsers();

    void saveUsers();

    void saveUser(User user, boolean async);

    void loadTimers();

    void saveTimers();

    void load();

    void close();
}