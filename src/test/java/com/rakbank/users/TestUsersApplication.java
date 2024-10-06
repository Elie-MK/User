package com.rakbank.users;

import org.springframework.boot.SpringApplication;

public class TestUsersApplication {

	public static void main(String[] args) {
		SpringApplication.from(UsersApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
