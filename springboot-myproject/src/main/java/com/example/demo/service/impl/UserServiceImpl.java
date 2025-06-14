package com.example.demo.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.exception.PasswordInvalidException;
import com.example.demo.exception.UserNoFoundException;
import com.example.demo.exception.UserRegisterException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.dto.userdto.UserCertDto;
import com.example.demo.model.dto.userdto.UserDto;
import com.example.demo.model.dto.userdto.UserLoginDto;
import com.example.demo.model.dto.userdto.UserRegisterDto;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.VerificationToken;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VerificationTokenRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired 
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
    private EmailService emailService;
	
	@Autowired
	private VerificationTokenRepository verificationTokenRepository;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	UserMapper userMapper;
	
	//-----------登入----------
	@Override
	public UserCertDto login(UserLoginDto userLoginDto){
		//驗證密碼
		User user = userRepository.getUser(userLoginDto.getUsername())
				.orElseThrow(()-> new UserNoFoundException("用戶不存在"));
		if(!passwordEncoder.matches(userLoginDto.getPassword(),user.getPasswordHash())) {
			throw new PasswordInvalidException("密碼錯誤");
		}
		
		//生成token回傳UserCertDto權限驗證
		String token = jwtUtil.generateToken(user.getId(),user.getUsername(), user.getRole());
		return new UserCertDto(user.getId(),user.getUsername() ,user.getRole(), token);
	}
	
	//-----------註冊----------
	@Override
	@Transactional
	public void addUser(UserRegisterDto userRegisterDto) {
		// 用戶保存
		User user = saveUser(userRegisterDto);

		// 產token驗證與發送
		createAndSendVerificationToken(user);
	}
	// 子方法用戶保存
	private User saveUser(UserRegisterDto userRegisterDto) {
		if (userRepository.existsByUsername(userRegisterDto.getUsername())) {
		    throw new UserRegisterException("用戶名已被註冊");
		}
		String passwordHash = passwordEncoder.encode(userRegisterDto.getPassword());

		User user = new User(null, userRegisterDto.getUsername(), userRegisterDto.getEmail(), passwordHash, false,
				userRegisterDto.getCreated(), userRegisterDto.getRole(), new ArrayList<>(), new ArrayList<>());
		return userRepository.save(user); // 這樣就含有id了
	}
	// 子方法token驗證與發送
	private void createAndSendVerificationToken(User user) {
		String token = UUID.randomUUID().toString();
		VerificationToken verificationToken = new VerificationToken(null, token, user,
				LocalDateTime.now().plusHours(24));
		verificationTokenRepository.save(verificationToken);
		
		//
		String verifyLink = "http://localhost:8081/user/verify?token=" + token;
		emailService.sendEmail(user.getEmail(), "請驗證您的帳號",
                "請點擊以下連結完成驗證：\n" + verifyLink);
	}
	
	//----------token驗證----------
	@Override
	@Transactional
	public boolean verifyUserToken(String token) {
		Optional<VerificationToken> optVerificationToken = verificationTokenRepository.getToken(token);
		if(optVerificationToken.isEmpty()) {
			return false;
		}
		VerificationToken verToken = optVerificationToken.get();
		if(verToken.getExpiryDate().isBefore(LocalDateTime.now())) {
			return false;
		}
		//驗證成功,修改用戶郵箱驗證:
		User user = verToken.getUser();
		user.setVerified(true);
		userRepository.save(user);
		//刪除token
		verificationTokenRepository.delete(verToken);
		return true;	
	}
	
	
	//獲取單個用戶
	@Override
	public UserDto getUserDto(Integer userId) {
		Optional<User> optUser = userRepository.findById(userId);
		return userMapper.toDto(optUser.orElseThrow(()-> new UserNoFoundException("用戶不存在")));
	}
	
}
