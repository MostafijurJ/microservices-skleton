package com.learn.ms.addressservice.controller;

import com.learn.ms.addressservice.dto.AddressDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/address")
public class AddressController {


	@GetMapping("/getAddress")
	public AddressDto getAddress() {
		return new AddressDto("123", "Pune", "MH");
	}

}
