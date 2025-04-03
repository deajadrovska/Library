package mk.finki.ukim.mk.library.model.Dto;

import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.model.enumerations.Role;

public record DisplayUserDto(String username, String name, String surname, Role role) {

    public static DisplayUserDto from(User user) {
        return new DisplayUserDto(
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getRole()
        );
    }

    public User toUser() {
        return new User(username, name, surname, role.name());
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String surname() {
        return surname;
    }

    @Override
    public Role role() {
        return role;
    }
}
