package ar.edu.um.events_backend.mapper;

import org.springframework.stereotype.Component;

import ar.edu.um.events_backend.dto.UserDto;
import ar.edu.um.events_backend.entity.User;

@Component
public class UserMapper {

    public UserDto toDto(User u) {
        if (u == null) return null;
        UserDto d = new UserDto();
        d.setIdUser(u.getIdUser());
        d.setName(u.getName());
        d.setSurname(u.getSurname());
        d.setMail(u.getMail());
        d.setPassword(u.getPassword());
        return d;
    }

    public User toEntity(UserDto d) {
        if (d == null) return null;
        User u = new User();
        u.setIdUser(d.getIdUser());
        u.setName(d.getName());
        u.setSurname(d.getSurname());
        u.setMail(d.getMail());
        u.setPassword(d.getPassword());
        return u;
    }
}
