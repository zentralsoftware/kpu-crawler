package org.crawler.c1.test;

import org.crawler.c1.C1MetaDataCrawler;
import org.junit.Before;
import org.junit.Test;

public class C1MetaDataTest {

	private C1MetaDataCrawler metaData;
	
	@Before
	public void setUp()
	{
		metaData = new C1MetaDataCrawler("/Users/fadnan/Downloads/chromedriver");
	}
	
	@Test
	public void metaDataAceh()
	{
		metaData.loadByProvinceId(1, 2);
		metaData.finish();
	}
}
