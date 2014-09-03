package org.crawler.c1.test;

import org.crawler.c1.C1Downloader;
import org.junit.Test;

public class C1DownloaderTest {

	@Test
	public void downloadAceh()
	{
		C1Downloader downloader = new C1Downloader("src/test/resources/c1metadata/1.csv", "src/test/resources/output");
		downloader.download();
	}
	
}
