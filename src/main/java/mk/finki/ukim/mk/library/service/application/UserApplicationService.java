package mk.finki.ukim.mk.library.service.application;

import mk.finki.ukim.mk.library.model.Dto.CreateUserDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayUserDto;
import mk.finki.ukim.mk.library.model.Dto.LoginResponseDto;
import mk.finki.ukim.mk.library.model.Dto.LoginUserDto;

import java.util.Optional;

public interface UserApplicationService {

    Optional<DisplayUserDto> register(CreateUserDto createUserDto);

//    Optional<DisplayUserDto> login(LoginUserDto loginUserDto);
    Optional<LoginResponseDto> login(LoginUserDto loginUserDto);
    Optional<DisplayUserDto> findByUsername(String username);
}
