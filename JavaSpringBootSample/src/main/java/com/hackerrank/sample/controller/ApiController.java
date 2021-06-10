package com.hackerrank.sample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ResponseBody;
import com.hackerrank.sample.service.ApiService;
import org.springframework.ui.Model;


@Controller
public class ApiController {

    @Autowired
    private ApiService apiService;

    //Initial Login page allows only 2 users as per coding
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String firstPage()
    {
      return "login";
    }

    //Basic Authentication when Login and generate token
    @RequestMapping(value = "/api/auth/", method = RequestMethod.POST)
	  public String authentication(@RequestParam("username") String name,@RequestParam("password") String pass) throws Exception {
        return apiService.authentication(name,pass);
    }

    
    //Convertion method calling 

    @RequestMapping(value = "/api/convert/", method = RequestMethod.POST,produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<byte[]> conversion(@RequestParam("file") MultipartFile sourceFile)throws Exception {
        return apiService.convertionXStoJson(sourceFile);
    }

   
}
