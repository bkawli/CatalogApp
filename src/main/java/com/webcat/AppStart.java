package com.webcat;
import static spark.Spark.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.webcatlib.DBConnector;

import spark.*;

public class AppStart {

	public static void main(String[] args) throws Exception {		
		
		String IP_ADDRESS = System.getenv("OPENSHIFT_DIY_IP");		
		int PORT;
		final String REPO_DIR;
		final String DATA_DIR;	
		final StringBuilder ADMIN_COOKIE=new StringBuilder(32);		
		final String COOKIE_GEN="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final int COOKIE_GEN_LEN=COOKIE_GEN.length();
		final Random rnd=new Random();		
		
		//final ArrayList <String> BASE_URLs=new ArrayList <String>();
		
		DBConnector dbconn=new DBConnector();
		final DB db=dbconn.getDB();
		
		if (IP_ADDRESS==null)
		{
			IP_ADDRESS = "localhost";		
			PORT = 8080;
			REPO_DIR=System.getProperty("user.dir") + "/resources/public/";	
			DATA_DIR="E:/CatalogApp_Data_dir/";			
			//BASE_URLs.add("http://" + IP_ADDRESS + ":" + PORT + "/");
		}
		else
		{				
			PORT = Integer.parseInt(System.getenv("OPENSHIFT_DIY_PORT"));	
			REPO_DIR=System.getenv("OPENSHIFT_REPO_DIR") + "resources/public/";	
			DATA_DIR=System.getenv("OPENSHIFT_DATA_DIR");
			//BASE_URLs.add("http://" + System.getenv("OPENSHIFT_APP_DNS") +"/");			
		}
		externalStaticFileLocation(DATA_DIR + "public/");
		setIpAddress(IP_ADDRESS);
		setPort(PORT);	
		
		//BASE_URLs.add(BASE_URLs.get(0)+"");
		
		/*
		after(new Filter(){
			@Override
			public void handle(Request request, Response response) throws Exception 
			{
				System.out.println("Req Url:" + request.url());				
				System.out.println("Resp Status:" + response.raw().getStatus());
			}			
		});
		*/
		
		get("/",new Route(){
			public Object handle(Request request, Response response) 
			{					 
				response.redirect("home");			 
				return "";	             
			}
		});
		
		
		get("/home",new Route(){
			public Object handle(Request request, Response response) 
			{		
				String html = "";
				try 
				{
					html=getStringFromFile(REPO_DIR + "home.html");
				} 
				catch (Exception e) 
				{					
					e.printStackTrace();
				}
				return html;	             
			}
		});
		
		get("/products",new Route(){
			public Object handle(Request request, Response response) 
			{		
				String html = "";
				try 
				{
					html=getStringFromFile(REPO_DIR + "products.html");
				} 
				catch (Exception e) 
				{					
					e.printStackTrace();
				}
				return html;	             
			}
		});
		
		get("/products/*",new Route(){
			public Object handle(Request request, Response response) 
			{		
				String html = "";
				String reqprod=request.splat()[0];
				DBCollection coll = db.getCollection("products");				
				BasicDBObject querydoc=new BasicDBObject("ProductName",reqprod);
				BasicDBObject projectiondoc=new BasicDBObject("_id",0);
				DBCursor cursor = coll.find(querydoc,projectiondoc);
				if(cursor.length()>0)
				{
					try 
					{
						html=getStringFromFile(REPO_DIR + "productPage.html");
						
					} 
					catch (Exception e) 
					{					
						e.printStackTrace();
					}
					return html;	     
				}
				else
				{
					try 
					{
						html=getStringFromFile(REPO_DIR + "notfound.html");						
					} 
					catch (Exception e) 
					{					
						e.printStackTrace();
					}
					return html;	   
				}
			}
		});
		
		get("/getProductDetails/*",new Route(){
			public Object handle(Request request, Response response) 
			{		
				String html = "";
				String reqprod=request.splat()[0];
				DBCollection coll = db.getCollection("products");				
				BasicDBObject querydoc=new BasicDBObject("ProductName",reqprod);
				BasicDBObject projectiondoc=new BasicDBObject("_id",0);
				DBObject prod=coll.findOne(querydoc,projectiondoc);
				if(prod!=null)
				{					
					return prod.toString();	     
				}
				else
				{
					try 
					{
						html=getStringFromFile(REPO_DIR + "notfound.html");						
					} 
					catch (Exception e) 
					{					
						e.printStackTrace();
					}
					return html;	   
				}             
			}
		});
		
		get("/contact",new Route(){
			public Object handle(Request request, Response response) 
			{		
				String html = "";
				try 
				{
					html=getStringFromFile(REPO_DIR + "contact.html");
				} 
				catch (Exception e) 
				{					
					e.printStackTrace();
				}
				return html;	             
			}
		});
		
		get("/getProducts",new Route(){
			public Object handle(Request request, Response response) 
			{	
				//mongodb query
				//db.products.find({},{_id:0,Images:{$slice:1}})
				String productsList = "";
				DBCollection coll = db.getCollection("products");				
				BasicDBObject querydoc=new BasicDBObject();
				BasicDBObject projectiondoc=new BasicDBObject("_id",0);
				projectiondoc.put("Images", new BasicDBObject("$slice",1));		
				projectiondoc.put("Index", 0);
				DBCursor cursor = coll.find(querydoc,projectiondoc).sort(new BasicDBObject("Index",1));
				try 
				{				
					BasicDBObject prodlst=new BasicDBObject("products",cursor);					
					productsList=prodlst.toString();
				} 
				finally 
				{
					   cursor.close();
				}
				return productsList;				
			}
		});
		
		get("/notfound",new Route(){
			public Object handle(Request request, Response response) 
			{		
				String html = "";
				try 
				{
					html=getStringFromFile(REPO_DIR + "notfound.html");
				} 
				catch (Exception e) 
				{					
					e.printStackTrace();
				}
				return html;
			}			
		});		
		
		get("/unknown",new Route(){
			public Object handle(Request request, Response response) 
			{		
				String html = "";
				try 
				{
					html=getStringFromFile(REPO_DIR + "unknown.html");
				} 
				catch (Exception e) 
				{					
					e.printStackTrace();
				}
				return html;
			}			
		});	
		
		/*
		get("*",new Route(){
			public Object handle(Request request, Response response) 
			{	
				if(response.raw().getStatus()==404)
				{
					response.redirect("notfound");
				}
				return "";
			}
		});
		*/	
				
		/*
		get("/json",new Route(){
			public Object handle(Request request, Response response) 
			{						 
				DBCollection coll = db.getCollection("testcoll");
				DBObject myDoc = coll.findOne();				 
				String dbstr=myDoc.toString();					 		 
				return dbstr;	             
			}
		});
		
		
		get("/updimg",new Route(){
			public Object handle(Request request, Response response) 
			{
				String html = "";
				try 
				{
					html=getStringFromFile(REPO_DIR + "updimg.html");
				} 
				catch (Exception e) 
				{					
					e.printStackTrace();
				}
				return html;
			}			
		});
		
		
		get("/updimg_app.js",new Route(){
			public Object handle(Request request, Response response) 
			{
				String html = "";
				try 
				{
					html=getStringFromFile(REPO_DIR + "updimg_app.js");
				} 
				catch (Exception e) 
				{					
					e.printStackTrace();
				}
				return html;
			}			
		});
		*/
		
		get("/admin",new Route(){
			public Object handle(Request request, Response response) 
			{	
				String html = "";	
				if(request.cookie("Catalogapp_Adm_cookie")==null)
				{						
					response.redirect("/admin/login");
				}	
				else if(ADMIN_COOKIE.toString().equals(""))
				{						
					response.redirect("/admin/login");
				}
				else if(request.cookie("Catalogapp_Adm_cookie").equals(ADMIN_COOKIE.toString())==false)
				{
					response.redirect("/admin/login");
				}
				else if(request.cookie("Catalogapp_Adm_cookie").equals(ADMIN_COOKIE.toString()))
				{					
					try 
					{
						html=getStringFromFile(REPO_DIR + "admin.html");
					} 
					catch (Exception e) 
					{					
						e.printStackTrace();
					}
				}
				return html;	             
			}
		});	
		
		get("/admin/login",new Route(){
			public Object handle(Request request, Response response) 
			{						
				String html = "";
				try 
				{
					html=getStringFromFile(REPO_DIR + "adminLogin.html");
				} 
				catch (Exception e) 
				{					
					e.printStackTrace();
				}				
				return html;	             
			}
		});	
		
		post("/authenticate",new Route(){
			public Object handle(Request request, Response response) 
			{	
				String username="";
				String password="";
				if(ServletFileUpload.isMultipartContent(request.raw())==true)
				{						
					DiskFileItemFactory factory = new DiskFileItemFactory();				    
				    //factory.setSizeThreshold(1024*1024*8); // maximum size that will be stored in memory
				    //factory.setRepository(new File(DATA_DIR + "temp"));
				   
				    ServletFileUpload upload = new ServletFileUpload(factory);  // Create a new file upload handler
				    upload.setFileSizeMax(1024*1024*5); // maximum file size to be uploaded.
				    
				    try
				    {
				    	List<FileItem> fileItems = upload.parseRequest(request.raw());				    	
				    	Iterator<FileItem> i = fileItems.iterator();
				    	while (i.hasNext ()) 
				        {
				    		FileItem fi = (FileItem)i.next();
				    		if (fi.isFormField ())	
				    		{				            	
				            	String fieldName = fi.getFieldName();
				            	String data=getStringFromInputStream(fi.getInputStream());
				            	if(fieldName.equals("username"))
				            	{
				            		username=data;
				            	}
				            	if(fieldName.equals("userpass"))
				            	{
				            		password=data;
				            	}
				            }				    					            
				        }			    	
				    }
				    catch (Exception e)
				    {
				    	e.printStackTrace();
				    }
				    if(username.equals("administrator") && password.equals("peeppeepdeep"))
				    {
				    	ADMIN_COOKIE.delete(0, 32);
				    	for (int i=0;i<32;i++)
				    	{
				    		ADMIN_COOKIE.append(COOKIE_GEN.charAt(rnd.nextInt(COOKIE_GEN_LEN)));	
				    	}				    	
				    	response.cookie("Catalogapp_Adm_cookie", ADMIN_COOKIE.toString());				    	
				    	return "Authenticated";
				    }	
				    else
				    {
				    	return "Authentication failed";
				    }
				}								
				return "";					
			}
		});
		
		post("/upload",new Route(){
			public Object handle(Request request, Response response) 
			{	
				
				if(ServletFileUpload.isMultipartContent(request.raw())==true)
				{						
					DiskFileItemFactory factory = new DiskFileItemFactory();				    
				    //factory.setSizeThreshold(1024*1024*8); // maximum size that will be stored in memory
				    //factory.setRepository(new File(DATA_DIR + "temp"));
				   
				    ServletFileUpload upload = new ServletFileUpload(factory);  // Create a new file upload handler
				    upload.setFileSizeMax(1024*1024*5); // maximum file size to be uploaded.
				    
				    try
				    {
				    	List<FileItem> fileItems = upload.parseRequest(request.raw());				    	
				    	Iterator<FileItem> i = fileItems.iterator();
				    	while (i.hasNext ()) 
				        {
				    		FileItem fi = (FileItem)i.next();
				    		if (fi.isFormField ())	
				    		{
				            	System.out.println("Not File");
				            	String fieldName = fi.getFieldName();
				            	String data=getStringFromInputStream(fi.getInputStream());
				            	System.out.println(fieldName);
					            System.out.println(data);
				            }
				    		else
				            {
				               // Get the uploaded file parameters
				               String fieldName = fi.getFieldName();
				               String fileName = fi.getName();				               
				               System.out.println(fieldName);
				               System.out.println(fileName);					               
				               File file = new File(DATA_DIR + "abc.txt");
				               fi.write(file);
				            }				            
				        }			    	
				    }
				    catch (Exception e)
				    {
				    	e.printStackTrace();
				    }
				    
				}
				else 
				{
					System.out.println("Not Multipart");					
				}					
				return "";					
			}
		});
		
		get("/admin/logout",new Route(){
			public Object handle(Request request, Response response) 
			{					 
				response.cookie("Catalogapp_Adm_cookie",ADMIN_COOKIE.toString(),0);
				ADMIN_COOKIE.delete(0, 32);		
				ADMIN_COOKIE.append("");				
				response.redirect("/admin/login");			 
				return "";	             
			}
		});		
		
		get("/isAdmin",new Route(){
			public Object handle(Request request, Response response) 
			{					 
				if(request.cookie("Catalogapp_Adm_cookie").equals(ADMIN_COOKIE.toString()))
				{			 
					return "true";	  
				}
				else
				{
					return "false";
				}
			}
		});	
		
		get("/getSlideShowImages",new Route(){
			public Object handle(Request request, Response response) 
			{					 
				String imagesList = "";
				DBCollection coll = db.getCollection("slides");				
				imagesList=coll.findOne().toString();	 
				return imagesList;	             
			}
		});
	}
	
	
	
	public static String getStringFromFile(String file_path) throws Exception
	{
		File fl=new File(file_path);
		FileInputStream is=new FileInputStream(fl);
		byte[] container_data=null;	
		ByteArrayOutputStream container = new ByteArrayOutputStream();
        byte[] buf = new byte[8*1024];
        int read;
        while ((read = is.read(buf, 0, 8*1024)) > 0) 
        {
            container.write(buf, 0, read);            
        }			
        container_data=container.toByteArray();   
        is.close();
        return new String(container_data);
	}
	
	public static String getStringFromInputStream(InputStream is) throws Exception
	{		
		byte[] container_data=null;	
		ByteArrayOutputStream container = new ByteArrayOutputStream();
        byte[] buf = new byte[8*1024];
        int read;
        while ((read = is.read(buf, 0, 8*1024)) > 0) 
        {
            container.write(buf, 0, read);            
        }			
        container_data=container.toByteArray();   
        is.close();
        return new String(container_data);
	}

}
