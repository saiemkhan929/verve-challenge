package com.verve.challenge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ChallengeApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testAcceptRequest() throws Exception {
		mockMvc.perform(get("/api/verve/accept").param("id", "1234"))
				.andExpect(status().isOk())
				.andExpect(content().string("ok"));
	}

	@Test
	public void testAcceptRequestWithEndpoint() throws Exception {
		mockMvc.perform(get("/api/verve/accept")
						.param("id", "5678")
						.param("endpoint", "http://localhost:8080"))
				.andExpect(status().isOk())
				.andExpect(content().string("ok"));
	}
}
