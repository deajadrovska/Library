package mk.finki.ukim.mk.library.service.application.Impl;

import mk.finki.ukim.mk.library.model.Dto.CreateUserDto;
import mk.finki.ukim.mk.library.model.Dto.DisplayUserDto;
import mk.finki.ukim.mk.library.model.Dto.LoginResponseDto;
import mk.finki.ukim.mk.library.model.Dto.LoginUserDto;
import mk.finki.ukim.mk.library.model.domain.User;
import mk.finki.ukim.mk.library.security.JwtHelper;
import mk.finki.ukim.mk.library.service.application.UserApplicationService;
import mk.finki.ukim.mk.library.service.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserApplicationServiceImpl implements UserApplicationService {

    private final UserService userService;

    @Autowired(required = false)
    private JwtHelper jwtHelper;

    public UserApplicationServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Optional<DisplayUserDto> register(CreateUserDto createUserDto) {
        User user = userService.register(
                createUserDto.username(),
                createUserDto.password(),
                createUserDto.repeatPassword(),
                createUserDto.name(),
                createUserDto.surname(),
                createUserDto.role()
        );
        return Optional.of(DisplayUserDto.from(user));
    }

    @Override
    public Optional<LoginResponseDto> login(LoginUserDto loginUserDto) {
        User user = userService.login(
                loginUserDto.username(),
                loginUserDto.password()
        );

        if (jwtHelper == null) {
            throw new RuntimeException("JWT Helper not available");
        }

        String token = jwtHelper.generateToken(user);

        return Optional.of(new LoginResponseDto(token));
    }


    @Override
    public Optional<DisplayUserDto> findByUsername(String username) {
        return Optional.of(DisplayUserDto.from(userService.findByUsername(username)));
    }
}
