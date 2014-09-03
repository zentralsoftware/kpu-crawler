package org.crawler.c1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class C1Downloader {
	private final static String VIEW_URL = "http://scanc1.kpu.go.id/viewp.php";
	private final static String C1_HOST = "scanc1.kpu.go.id";
	
	private String outputDirectory = "output";	
	private String c1FileName;
	
	public C1Downloader(String c1FileName, String outputDirectory)
	{
		this.c1FileName = c1FileName;
		this.outputDirectory = outputDirectory;
	}
	
	// load file
	// for each line, 
	// create c1row object
	// create directories
	// download file
	public void download()
	{
		try (
				Reader reader = new FileReader(new File(c1FileName));
				BufferedReader in = new BufferedReader(reader);
				)		
		{
			String line = "";
			while ((line = in.readLine()) != null)
			{
				C1Row row = instance(line);
				System.err.println(row);				
				downloadScannedC1(row);
			}			
		} catch (Exception e)
		{
			e.printStackTrace();
		}				
	}
	
	public C1Row instance(String line)
	{
		C1Row row = new C1Row();
		String[] cols = line.split(",");
		row.setPropinsi(cols[0]);
		row.setKota(cols[1]);
		row.setKecamatan(cols[2]);
		row.setKelurahan(cols[3]);
		row.setKelurahanId(Integer.parseInt(cols[4]));
		for (int i=5;i<cols.length;i++)
		{
			row.getTpsIds().add(Integer.parseInt(cols[i]));
		}
		return row;
	}
	
	public void downloadScannedC1(C1Row row)
	{
		List<Path> files = buildOutputFiles(row);
		HttpClientConnectionManager cm = buildCm(C1_HOST, 80);
		CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
		for (Path file:files)
		{
			File output = file.toFile();
			if (!output.exists())
			{
				String url = buildUrl(file);
				System.err.println("downloading " + url);
				try {		
					Files.createDirectories(file.getParent());
					HttpGet httpget = new HttpGet(url);
					HttpResponse response = httpClient.execute(httpget);
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						if (entity.getContentType().getValue().equals("image/jpeg"))
						{
							BufferedInputStream bis = new BufferedInputStream(entity.getContent());
							output.createNewFile();
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(output));
							int inByte;
							while((inByte = bis.read()) != -1) bos.write(inByte);
							bis.close();
							bos.close();							
						}
					}	
				} catch (Exception e)
				{
					System.err.println("failed downloading: " + url);
					e.printStackTrace();
				}
			}
		}
	}	
	
	protected HttpClientConnectionManager buildCm(String host, int port)
	{
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(20);
		HttpHost httpHost = new HttpHost(host, port);
		cm.setMaxPerRoute(new HttpRoute(httpHost), 50);
		return cm;
	}
	
	public List<Path> buildOutputFiles(C1Row row) 
	{
		List<Path> paths = new ArrayList<Path>();
		for (int i=1;i<=row.getTpsIds().size();i++)
		{
			String strTpsNo = String.format("%03d", i);
			for (int j=1;j<=4;j++)
			{
				String scanId = String.format("%02d", j);
				String strId = String.format("%07d", row.getKelurahanId());
				StringBuilder sb = new StringBuilder();
				sb.append(outputDirectory);
				sb.append(System.getProperty("file.separator"));
				sb.append(row.getPropinsi());
				sb.append(System.getProperty("file.separator"));
				sb.append(row.getKota());
				sb.append(System.getProperty("file.separator"));
				sb.append(row.getKecamatan());
				sb.append(System.getProperty("file.separator"));			
				sb.append(row.getKelurahan());
				sb.append(System.getProperty("file.separator"));
				sb.append(row.getTpsIds().get(i-1));
				sb.append(System.getProperty("file.separator"));
				sb.append(strId + strTpsNo + scanId + ".jpg");
				paths.add(Paths.get(sb.toString()));
			}
		}
		return paths;
	}	
	
	protected String buildUrl(Path path)
	{
		File file = path.toFile();
		String name = file.getName();
		StringBuffer sb = new StringBuffer();
		sb.append(VIEW_URL);
		sb.append("?f=");
		sb.append(name);
		return sb.toString();
	}	
}
