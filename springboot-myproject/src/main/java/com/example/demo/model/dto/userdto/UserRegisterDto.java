package com.example.demo.model.dto.userdto;

import java.time.LocalDateTime;
//註冊用dto,前端傳回資料

public class UserRegisterDto {
	
	private String password;

	private String username;

	private String email;

	private LocalDateTime created;

	private String role;
}
