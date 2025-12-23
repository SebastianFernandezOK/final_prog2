package ar.edu.um.events_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.events_backend.dto.UserDto;
import ar.edu.um.events_backend.entity.User;
import ar.edu.um.events_backend.exception.ResourceNotFoundException;
import ar.edu.um.events_backend.mapper.UserMapper;
import ar.edu.um.events_backend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserService(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public UserDto create(UserDto dto) {
        User user = mapper.toEntity(dto);
        User saved = repository.save(user);
        return mapper.toDto(saved);
    }

    @Transactional
    public UserDto update(Integer id, UserDto dto) {
        User existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        existing.setName(dto.getName());
        existing.setSurname(dto.getSurname());
        existing.setMail(dto.getMail());
        existing.setPassword(dto.getPassword());
        User saved = repository.save(existing);
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public UserDto getById(Integer id) {
        User u = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        return mapper.toDto(u);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        repository.deleteById(id);
    }
}
