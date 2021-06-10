package com.hackerrank.sample.service;

import org.json.JSONObject;
import org.json.XML;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import com.hackerrank.sample.service.UserAuthService;
import com.hackerrank.sample.security.JWTUtil;
import org.springframework.web.multipart.MultipartFile;
import org.json.JSONArray;
import org.springframework.web.bind.annotation.ResponseBody;
import org.apache.commons.io.FileUtils;
import java.io.*; 
import java.util.regex.*;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service("apiService")
public class ApiService {

    @Autowired
	  private JWTUtil jwtTokenUtil;

	  @Autowired
	  private UserAuthService userDetailsService;

    @Autowired
	  private AuthenticationManager authenticationManager;
    
    @Value("${conversion.input}")
    private String type;

    @Value("${conversion.xml.format}")
    private String format;

    @Value("${conversion.xml.remove}")
    private String remove;
    
    @Value("${conversion.string.59F.format}")
    private String format_59f;

    @Value("${conversion.string.50F.format}")
    private String format_50f;

   

    public String authentication(String username,String paspassword) throws Exception
    {
               
        String[] userArray=username.split(",");
        String[] passArray=paspassword.split(",");
        String user=userArray[0];
        String pass=passArray[0];

        authenticate(user, pass);

        try{
            final UserDetails userDetails = userDetailsService
            .loadUserByUsername(user);
            final String token = jwtTokenUtil.generateToken(userDetails);
            System.out.println("token"+token);
            jwtTokenUtil.setToken(token);
            return "convert";
        }
        catch(Exception e)
        {
            JSONObject entity1 = new JSONObject();
            entity1.put("message", "Username or Password is Incorrect");
            return "error";
        }
    }

    private String convertMultiPartToFile(MultipartFile file) throws IOException
    {
        File convFile = new File( file.getOriginalFilename() );
        FileOutputStream fos = new FileOutputStream( convFile );
        fos.write( file.getBytes() );
        fos.close();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getOriginalFilename())));
        String line;
        String sb = "";
        while((line=br.readLine())!= null){
          sb+=line.trim();
        }
        return sb;
        
    }
    private void authenticate(String username, String password) throws Exception {
        try {
          System.out.println(username+" "+password);
          authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } 
        catch (BadCredentialsException e) {
          throw new Exception("Incorrect username or password", e);
        }
    }
    //Convertion Implementation
    public ResponseEntity<byte[]> convertionXStoJson(MultipartFile sourceFile)throws Exception 
    {

        String s=convertMultiPartToFile(sourceFile);
        JSONObject j=new JSONObject();
        try
        {
            if(sourceFile == null)
            {
                throw new JSONException("No file is selected");
            }
            if(type.contains("XML"))
            {
                j=XML.toJSONObject(s); 
                j= xmlConversion(j);
            }
            else if(type.contains("String"))
            {
                j= stringCoversion(s);
            }
            else
            {
                throw new JSONException("Only XML or String format allowed");
            }
            //Coverting jsonobject to download file 
            byte[] isr = j.toString(4).getBytes();
            String fileName = "convert.json";
            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentLength(isr.length);
            respHeaders.setContentType(new MediaType("text", "json"));
            respHeaders.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            respHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            return new ResponseEntity<byte[]>(isr, respHeaders, HttpStatus.OK);
         }
         catch(JSONException e)
         {
            j=new JSONObject();
            j.put("message", "Please check the XML format inside the file");
            byte[] isr1 = j.toString(4).getBytes();
            HttpHeaders respHeaders1 = new HttpHeaders();
            respHeaders1.setContentLength(isr1.length);
            e.printStackTrace();
            return new ResponseEntity<byte[]>(isr1, respHeaders1, HttpStatus.NO_CONTENT);
         }
         
    }
 
      public JSONObject xmlConversion(JSONObject j)
      {
            int count=0;
            int amt=0;
            String regex = "[a-zA-Z0-9]+";
            Pattern p = Pattern.compile(regex);
            
            String[] frm=format.split("/");
            if(j.has(frm[0]))
            {
                  j.getJSONObject(frm[0]).remove(remove);
                  if(j.getJSONObject(frm[0]).has(frm[1]))
                  {
                      Object item = j.getJSONObject(frm[0]).get(frm[1]);
                      if(item instanceof JSONArray)
                      {
                          JSONArray ja=j.getJSONObject(frm[0]).getJSONArray(frm[1]);
                          count=ja.length();
                          for(int i=0;i<count;i++)
                          {
                            amt+=ja.getJSONObject(i).getInt(frm[2]);
                            ja.getJSONObject(i).remove("content");
                            String str=ja.getJSONObject(i).getString("UniqueID");
                            Matcher m = p.matcher(str);
                            if (!m.matches())
                            {
                              throw new JSONException("Unique ID is not valid");
                            }
                          }
                      }  
                      else
                      {
                          JSONObject ja=j.getJSONObject(frm[0]).getJSONObject(frm[1]);
                          amt+=ja.getInt(frm[2]);
                          ja.remove("content");
                          String str=ja.getString("UniqueID");
                          Matcher m = p.matcher(str);
                          if (!m.matches())
                          {
                              throw new JSONException("Unique ID is not valid");
                          }           
                          count=1;
                      }
            
                    }
                    JSONObject jo = new JSONObject();
                    jo.put("TotAmt",amt);
                    jo.put("TotCnt",count);
                    j.getJSONObject(frm[0]).put("Header",jo);
            }
            return j;
      }

      public JSONObject stringCoversion(String s)
      {
          JSONObject j=new JSONObject();
          JSONArray array = new JSONArray();
          String[] f=format_59f.split("-");
          array=stringManipulation(s,f);
          JSONArray array1 = new JSONArray();
          String[] f1=format_50f.split("-");
          array1=stringManipulation(s,f1);
          for (int i = 0; i < array.length(); i++) {
              array1.put(array.getJSONObject(i));
          }
          j.put("data",array1);
          return j;
      }

      public JSONArray stringManipulation(String s,String[] f)
      {
   
          JSONArray array = new JSONArray();
          String str="";
          

          String str1=s.substring(s.indexOf(f[0]));
          if(str1.indexOf("\\n:")!=-1)
            str=str1.substring(0,str1.indexOf("\\n:"));
          else
            str=str1;

          String[] cama=f[1].split(",");
          JSONObject j1=new JSONObject();
          JSONObject j2=new JSONObject();
          JSONObject j3=new JSONObject();
          for(int i=0;i<cama.length;i++)
          {
            
            String[] colon=cama[i].split(":");

            String[] slas=colon[1].split("\\\\");

            if(str.indexOf(colon[0])!=-1)
            {
                String sr=str.substring(str.indexOf(colon[0]));
                if(sr.indexOf("\\n")!=-1)
                {
                    String sub=sr.substring(0,sr.indexOf("\\n"));
                    sub=sub.replace(colon[0],"");
                    String sn=sr.substring(sr.lastIndexOf(colon[0]),sr.length());
                    if(sn.contains(colon[0]))
                    {
                        if(sn.indexOf("\\n")!=-1)
                        {
                          String sr1=sn.substring(sn.indexOf(colon[0]),sn.indexOf("\\n"));
                          sr1=sr1.replace(colon[0], "");
                          if(sn!=sr1)
	                          sn=sub+" "+sr1;
                        } 
                        else
                        {
                          sn=sub+" "+sn;
	                        sn=sn.replace(colon[0], "");
                        }
                    }
                
                    if(slas.length>2)
                    {
                      j3.put(slas[2],sn);
                      j2.put(slas[1],j3);
                    } 
                    else
                      j2.put(slas[1],sn);
                      
                    }
                    j1.put(slas[0],j2);
               }
               
            }
            
            
            array.put(j1);
            return array;
      }
}