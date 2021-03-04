package com.ivoice;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AskController {

  @GetMapping("/ask")
  public ResponseEntity<String> aks(@RequestParam("question") String question) {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
        .body("You asked '" + question + "', but our programmers still working on it...");
  }
}
