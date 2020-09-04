package com.mahendran.parallelFiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.mahendran.parallelFiles.controllers.MainController;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class TestParallelController {
	
	@Autowired
	private MainController controller;
	
	@Autowired
	private MockMvc mockMvc;


	@Test
	public void contextLoads() {
		assertThat(controller).isNotNull();
	}
	
	@Test
	public void testHomePage() throws Exception {
		this.mockMvc.perform(get("/"))
			.andExpect(status().isOk())
			.andExpect(content().contentType("text/html;charset=UTF-8"));
	}

	@Test
	public void testUpload() throws Exception {
		MultipartFile[] fileArray = new MultipartFile[]{
		 };
		this.mockMvc.perform(MockMvcRequestBuilders.multipart("/upload").file(new MockMultipartFile("test.txt",
	            "test.txt",
	            "text/plain",
	            "ThiS Is a Test\r\nOOOOoooOoO".getBytes(StandardCharsets.UTF_8)))
				.file(new MockMultipartFile("test2.txt",
	            "test2.txt",
	            "text/plain",
	            "&^%*^&SKNJHKJHJKKJDDHHssssssest\r\nOOOOo444444ooOoO".getBytes(StandardCharsets.UTF_8))))
			.andExpect(status().isOk());
	}

}