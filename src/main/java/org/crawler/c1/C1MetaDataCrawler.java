package org.crawler.c1;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class C1MetaDataCrawler {

	private WebDriver driver;
	public static final Logger logger = Logger.getLogger(C1MetaDataCrawler.class);
	public static final int TIMEOUT = 10;	
	public static final String C1_URL = "http://pilpres2014.kpu.go.id/c1.php";
	
	public C1MetaDataCrawler(String chromeDriverLocation)
	{
		System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
		driver = new ChromeDriver();		
	}
	
	public void loadByProvinceId(int startP, int endP)
	{	
		int currentP = startP;
		int currentC = 1;
		int currentD = 1;

		Select provinceSelect = null;
		Select citySelect = null;
		Select districtSelect = null;
		Select villageSelect = null;

		WebElement province = null;
		WebElement city = null;
		WebElement district = null;
		WebElement village = null;
		
		boolean retry = false;
		do {
			retry = false;
			int phase = 0;
			try {
				provinceSelect = loadProvince();
				phase = 1;
				for (int indexP=currentP;indexP<=endP;indexP++)
				{
					MDC.put("tag", "loadByProvinceId-" + indexP);						
					System.err.println("indexP: " + indexP);
					province = provinceSelect.getOptions().get(indexP);
					citySelect = loadCityFromProvince(provinceSelect, indexP);	
					phase = 2;
					for (int indexC=currentC;indexC<citySelect.getOptions().size();indexC++)
					{
						System.err.println("indexC: " + indexC);
						city = citySelect.getOptions().get(indexC);
						districtSelect = loadDistrictFromCity(citySelect, indexC);
						phase = 3;
						for (int indexD=currentD;indexD<districtSelect.getOptions().size();indexD++)
						{
							System.err.println("indexD: " + indexD);
							district = districtSelect.getOptions().get(indexD);
							villageSelect = loadVillageFromDistrict(districtSelect, indexD);
							for (int v=1;v<villageSelect.getOptions().size();v++)
							{
								village = villageSelect.getOptions().get(v);
								String villageId = village.getAttribute("value");
								phase = 4;
								villageSelect.selectByIndex(v);
								(new WebDriverWait(driver, TIMEOUT)).until(ExpectedConditions.presenceOfElementLocated(By.id("daftartps")));				
								WebElement daftartps = driver.findElement(By.id("daftartps"));
								WebElement table = daftartps.findElement(By.tagName("table"));
								List<WebElement> allRows = table.findElements(By.tagName("tr"));
								int expectedTpsIndex = 1;
								StringBuffer tpsSb = new StringBuffer();
								for (WebElement row : allRows) {
									List<WebElement> cells = row.findElements(By.tagName("td"));
									if (cells.size() > 0)
									{
										WebElement firstCell = cells.get(0);
										if (firstCell.getText().equals(Integer.toString(expectedTpsIndex)))
										{
											WebElement secondCell = cells.get(1);
											tpsSb.append(",");
											tpsSb.append(secondCell.getText());
											expectedTpsIndex++;
										}									
									}
								}		
								logger.info(province.getText() + "," + city.getText() + "," + district.getText() + "," + village.getText() + "," + villageId + tpsSb);
							}
							currentD++;
						}
						currentD = 1;
						currentC++;
					}		
					currentC = 1;
					currentP++;
				} 			
			} catch (Throwable e)
			{
				if (phase == 0)
				{
					logger.error("loadProvince - " + e.getMessage());
				} else if (phase == 1)
				{
					logger.error("loadCityFromProvince," + province.getText() + " - " + e.getMessage());
				} else if (phase == 2)
				{
					logger.error("loadDistrictFromCity," + province.getText() + "," +  city.getText() + " - " + e.getMessage());
				} else if (phase == 3)
				{
					logger.error("loadVillageFromDistrict," + province.getText() + "," +  city.getText() + "," + district.getText() + " - " + e.getMessage());
				} else if (phase == 4)
				{
					logger.error("loadTps," + province.getText() + "," +  city.getText() + "," + district.getText() + "," + village.getText() + " - " + e.getMessage());
				} else
				{
					e.printStackTrace();
				}
				retry = true;
			} finally {
				MDC.remove("tag");
			}
		} while (retry);		
	}		
	
	public Select loadProvince()
	{
		driver.get(C1_URL);		
		(new WebDriverWait(driver, TIMEOUT)).until(ExpectedConditions.presenceOfElementLocated(By.id("loading_0")));		
		Select provinceSelect = getSelectByCat(CategoryEnum.PROVINCE);				
		return provinceSelect;		
	}	
	
	public Select loadCityFromProvince(Select provinceSelect, int provinceIndex)
	{
		Select citySelect = null;
		WebElement province = provinceSelect.getOptions().get(provinceIndex);
		String provinceValue = province.getAttribute("value");
		provinceSelect.selectByIndex(provinceIndex); 	
		(new WebDriverWait(driver, TIMEOUT)).until(ExpectedConditions.presenceOfElementLocated(By.id("loading_" + provinceValue)));
		citySelect = getSelectByCat(CategoryEnum.CITY);			
		return citySelect;
	}
	
	public Select loadDistrictFromCity(Select citySelect, int cityIndex)
	{
		WebElement city = citySelect.getOptions().get(cityIndex);
		String cityValue = city.getAttribute("value");
		citySelect.selectByIndex(cityIndex);
		(new WebDriverWait(driver, TIMEOUT)).until(ExpectedConditions.presenceOfElementLocated(By.id("loading_" + cityValue)));		
		Select districSelect = getSelectByCat(CategoryEnum.DISTRICT);		
		return districSelect;
	}
	
	public Select loadVillageFromDistrict(Select districtSelect, int districtIndex)
	{
		WebElement kecamatan = districtSelect.getOptions().get(districtIndex);
		String kecamatanValue = kecamatan.getAttribute("value");
		districtSelect.selectByIndex(districtIndex);
		(new WebDriverWait(driver, TIMEOUT)).until(ExpectedConditions.presenceOfElementLocated(By.id("loading_" + kecamatanValue)));		
		Select villageSelect = getSelectByCat(CategoryEnum.VILLAGE);
		return villageSelect;
	}	
	
	public Select getSelectByCat(CategoryEnum category)
	{
		List<WebElement> wilayahElements = driver.findElements(By.name("wilayah_id"));		
		Select select = null;
		WebElement element = null;
		switch (category){
			case PROVINCE:
				element = wilayahElements.get(0);
				select = new Select(element);
				break;
			case CITY:
				element = wilayahElements.get(1);
				select = new Select(element);
				break;
			case DISTRICT:
				element = wilayahElements.get(2);
				select = new Select(element);
				break;				
			case VILLAGE:
				element = wilayahElements.get(3);
				select = new Select(element);
				break;								
		}
		return select;			
	}	
	
	public void finish()
	{
		driver.close();
		driver.quit();
	}
}
