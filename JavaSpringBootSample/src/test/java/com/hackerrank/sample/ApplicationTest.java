package com.hackerrank.sample;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.context.web.WebAppConfiguration;
import com.hackerrank.sample.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.junit.runners.MethodSorters;
import com.hackerrank.sample.controller.ApiController;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class ApplicationTest {
    public String token="";
    @Autowired
    private ObjectMapper mapper; 

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc; 
    @Autowired
      WebApplicationContext context;
     @Before
      public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
      }


    @Test
    public void contextLoads() {
        
    }
    
    @Test
    public void testHomeController() {
        ApiController homeController = new ApiController();
        String result = homeController.firstPage();
        assertEquals(result, "login");
    }

    @Test
    public void testLogin() throws Exception{
        User u =  new User("user1","test1");
        byte[] iJson = toJson(u);
        MvcResult result =    mockMvc.perform(post("/api/auth/")
                .content(iJson)
                 .accept(MediaType.APPLICATION_JSON))
                 .andReturn();
        

    }

       @Test
    public void testConverstion() throws Exception {
    MockMultipartFile file 
      = new MockMultipartFile(
        "file", 
        "hello.txt", 
        MediaType.TEXT_PLAIN_VALUE, 
        "Hello, World!".getBytes()
      );
    /* MockMvc mockMvc 
      = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    mockMvc.perform(multipart(/api/convert/).file(file))
      .andExpect(status().isOk());*/
    }

    private byte[] toJson(Object r) throws Exception {
        ObjectMapper map = new ObjectMapper();
        return map.writeValueAsString(r).getBytes();
    }

}
