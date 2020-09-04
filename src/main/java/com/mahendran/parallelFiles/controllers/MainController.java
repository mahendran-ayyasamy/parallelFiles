package com.mahendran.parallelFiles.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Controller
@Slf4j
public class MainController {

	@RequestMapping("/")
	public String index() {

		return "home";
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/zip")
	public void upload(@RequestParam("file") MultipartFile[] fileArray, RedirectAttributes attributes,
			HttpServletResponse response) throws IOException {
		if (fileArray.length == 0) {
			attributes.addFlashAttribute("message", "Please select a file.");
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.addHeader("Content-Disposition", "attachment; filename=\"output.zip\"");

		

		Flux<MultipartFile> flux = Flux.fromArray(fileArray);
		Scheduler scheduler = Schedulers.boundedElastic();
		ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
		Disposable disposable = flux.subscribeOn(scheduler).subscribe(multiPartFile -> {
			
				Stream<String> lines;
				try{
					String fileName = multiPartFile.getOriginalFilename();
					byte[] bytes = multiPartFile.getBytes();
					log.info("Thread name is: {} and file name is {}",Thread.currentThread().getName(), fileName);
					String content = new String(bytes, StandardCharsets.UTF_8);
					content = content.replaceAll("[A-Z]", "");
					File convFile = new File(fileName);
					FileUtils.writeStringToFile(convFile, content, StandardCharsets.UTF_8, false);
					zipOutputStream.putNextEntry(new ZipEntry(multiPartFile.getOriginalFilename()));
					FileInputStream fileInputStream = new FileInputStream(convFile);
					IOUtils.copy(fileInputStream, zipOutputStream);
					fileInputStream.close();
					zipOutputStream.closeEntry();
				} catch (IOException e) {
					log.error("did not save", e);
					return ;
				}
		}, error -> {
			log.error("Exception happened in subscriber",error);
			attributes.addFlashAttribute("status", "Failed to convert files");
			return;
		}, () -> {
			log.info("Subscribe Oncomplete called");
			try {
				zipOutputStream.close();
			} catch (IOException e) {
				log.error("Could not close zipOutStream", e);
			}
			attributes.addFlashAttribute("status", "Saved file(s) to folder");
			return;
		});
		while(!disposable.isDisposed()) {
		}
		log.info("WoooHOOOO! Disposed: {}",disposable.isDisposed());
	}

}
