package com.example.demo.service;

import com.example.demo.exception.UserException;
import com.example.demo.model.dto.userdto.UserCertDto;
import com.example.demo.model.dto.userdto.UserDto;
import com.example.demo.model.dto.userdto.UserLoginDto;
import com.example.demo.model.dto.userdto.UserRegisterDto;

public interface UserService {
	public UserCertDto login(UserLoginDto userLoginDto) throws UserException;

	public void addUser(UserRegisterDto userRegisterDto);

	public UserDto getUser(String username);

	public boolean verifyUserToken(String token);
}
