package com.tosan.bookstore.services;

import com.tosan.bookstore.dtos.inputs.*;
import com.tosan.bookstore.dtos.outputs.*;
import com.tosan.bookstore.exceptions.BusinessException;
import com.tosan.bookstore.exceptions.FaultCodes;
import com.tosan.bookstore.models.*;
import com.tosan.bookstore.daos.UserRepository;
import com.tosan.bookstore.utils.enums.EnumUtils;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserService extends BaseService {
    private final UserRepository repository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<Integer, String> GetUserTypes() {
        return EnumUtils.GetEnumNames(UserType.class);
    }

    public UserOutputDto GetUser(Long id) {
        User user = repository.findById(id).orElse(null);
        if (user == null) {
            throw new BusinessException(FaultCodes.UserNotExists);
        }
        return modelMapper.map(user, UserOutputDto.class);
    }

    public List<UserOutputDto> GetUsers() {
        List<UserOutputDto> outputDto = new ArrayList<>();
        List<User> users = (List<User>) repository.findAll();
        for (User user : users) {
            outputDto.add(modelMapper.map(user, UserOutputDto.class));
        }

        return outputDto;
    }

    public void Register(UserInputDto inputDto) {
        var user = modelMapper.map(inputDto, User.class);
        user.setPassword(passwordEncoder.encode(inputDto.getPassword()));
        user.setActive(true);

        repository.save(user);
    }

    public LoginOutputDto Login(LoginInputDto inputDto) {
        User user = repository.findByUsername(inputDto.getUsername());
        if (user == null) {
            throw new BusinessException(FaultCodes.UserNotExists);
        }

        if (!user.getActive()) {
            throw new BusinessException(FaultCodes.UserNotActive);
        }

        if (passwordEncoder.matches(inputDto.getPassword(), user.getPassword())) {
            var outputDto = modelMapper.map(user, LoginOutputDto.class);
            user.setLastLoginDate(LocalDateTime.now());
            repository.save(user);
            return outputDto;
        } else {
            throw new BusinessException(FaultCodes.UserLoginFailed);
        }
    }

    public void ChangePassword(ChangePasswordInputDto inputDto) {
        if (passwordEncoder.matches(inputDto.getOldPassword(), inputDto.getNewPassword())) {
            throw new BusinessException(FaultCodes.UserSameOldAndNewPassword);
        }

        User user = repository.findByUsername(inputDto.getUsername());
        if (user == null) {
            throw new BusinessException(FaultCodes.UserNotExists);
        }

        if (!user.getActive()) {
            throw new BusinessException(FaultCodes.UserNotActive);
        }

        if (passwordEncoder.matches(inputDto.getOldPassword(), user.getPassword())) {
            user.setPassword(inputDto.getNewPassword());
            repository.save(user);
        } else {
            throw new BusinessException(FaultCodes.UserLoginFailed);
        }
    }

    public void ResetPassword(ResetPasswordInputDto inputDto) {
        User user = repository.findByUsername(inputDto.getUsername());
        if (user == null) {
            throw new BusinessException(FaultCodes.UserNotExists);
        }

        if (!user.getActive()) {
            throw new BusinessException(FaultCodes.UserNotActive);
        }

        user.setPassword(inputDto.getNewPassword());
        repository.save(user);
    }

    public void ActiveUser(Long id) {
        User user = repository.findById(id).orElse(null);
        if (user == null) {
            throw new BusinessException(FaultCodes.UserNotExists);
        }

        user.setActive(true);
        repository.save(user);
    }

    public void DeActiveUser(Long id) {
        User user = repository.findById(id).orElse(null);
        if (user == null) {
            throw new BusinessException(FaultCodes.UserNotExists);
        }

        user.setActive(false);
        repository.save(user);
    }
}
