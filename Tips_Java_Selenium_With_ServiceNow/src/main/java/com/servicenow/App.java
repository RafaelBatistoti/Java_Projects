package com.servicenow;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.bytebuddy.asm.Advice.Thrown;
import net.bytebuddy.implementation.bytecode.Throw;

public class App {

	public static int ADDBUTTON = 1;
	public static int ORBUTTON = 3;

	public static void main(String[] args) throws Exception {

//		<OPEN ChromeDriver INSTANCE>
		WebDriver driver = new ChromeDriver();
		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\RafaelBatistoti\\Documents\\ws-java\\Tips_Java_Selenium_With_ServiceNow\\webDriver\\chromedriver.exe");
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

//		<OPEN WEB BROWSER>
		String kyndrylMxDevUrl = "https://kyndrylmxdev.service-now.com";
		driver.get(kyndrylMxDevUrl);
		driver.manage().window().maximize();

//		<LOGIN PAGE>
		loginPage(driver);

//		<ALERT>
		String alertMsg = null;
		try {
			alertMsg = driver.switchTo().alert().getText();
		} catch (Exception e) {
			alertMsg = "Login ok!";
		}

		if (!(alertMsg.equals("Login ok!"))) {
			Alert alert = driver.switchTo().alert();
			alert.accept();
			loginPage(driver);
		}

//		<CHANGE DOMAIN TO ADO>
		Select				domainOptionsDropdown	= domainSelect(driver);
		List<WebElement>	domainOptionList		= domainOptionsDropdown.getOptions();
		String				returnDomain			= selectAdoDomain(domainOptionsDropdown, domainOptionList);
		if (returnDomain.equals("global")) {
			Thread.sleep(5000);
			domainOptionsDropdown = domainSelect(driver);
			domainOptionList = domainOptionsDropdown.getOptions();
			returnDomain = selectAdoDomain(domainOptionsDropdown, domainOptionList);
		}

//		<CALL JSON FILE>

		ObjectMapper				mapper		= new ObjectMapper();
		List<Map<String, Object>>	dataList	= mapper.readValue(new File(
				"C:\\Users\\RafaelBatistoti\\Documents\\ws-java\\Tips_Java_Selenium_With_ServiceNow\\jsonDoc\\incidents_flow.json"),
				new TypeReference<List<Map<String, Object>>>() {
														});

		for (Map<String, Object> data : dataList) {
			String	origin				= (String) data.get("Origen");
			String	solicitedService	= (String) data.get("SERVICIO SOLICITADO");
			String	subject				= (String) data.get("PALABRAS OBLIGATORIAS QUE DEBE CONTENER EL CORREO");
			String	bodyText			= (String) data
					.get("PALABRAS DE LAS CUALES AL MENOS UNA DEBE CONTENER EL CORREO");

//		<GET URL FLOW DESIGNER>		
			String	fwUrl				= driver.findElement(By.id("6b74c6e5672222006cc275f557415a3c"))
					.getAttribute("href");

//		<ACCESS FLOW DESIGNER>
			driver.get(fwUrl);

//		<CREATE NEW FLOW>		
			driver.findElement(By.id("new_btn")).click();
			List<WebElement> allListItems = getListOfTypeFlowCanBeCreated(driver);
			selectNewFlow(driver, allListItems);

//		<INSERT INFORMATION FROM JSON IN DESCRIPTION OF FLOW>
			driver.findElement(By.id("name")).sendKeys(origin);
			driver.findElement(By.id("description")).sendKeys(solicitedService);
			driver.findElement(By.id("flow_properties_submit_btn")).click();

//		<ADD TRIGGER>
			WebElement flowTriggerTextToggle;
			try {
				Thread.sleep(3000);
				flowTriggerTextToggle = new WebDriverWait(driver, Duration.ofSeconds(10))
						.until(ExpectedConditions.elementToBeClickable(By.id("flow_trigger_text_toggle")));
				flowTriggerTextToggle.click();
				System.out.println("I press id!");
			} catch (Exception e) {
				Thread.sleep(3000);
				flowTriggerTextToggle = new WebDriverWait(driver, Duration.ofSeconds(10))
						.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#flow_trigger_text_toggle")));
				flowTriggerTextToggle.click();
				System.out.println("I press cssSelector!");
			}

//		<SELECT APPLICATION>	
			Thread.sleep(1000);
			String				typeTrigger			= "Application";
			List<WebElement>	pickerScopeListLi	= getListOfTypeTriggerCanBeCreated(driver, typeTrigger);
			pickerScopeList(pickerScopeListLi, driver, typeTrigger);

//		<SELECT INBOUND EMAIL>	
			typeTrigger = "Inbound Email";
			pickerScopeListLi = getListOfTypeTriggerCanBeCreated(driver, typeTrigger);
			applicationOptionsList(pickerScopeListLi, driver, typeTrigger);

//		<ADD TYPE AND NOTIFICATION TYPE CONDITIONS>
			for (int i = 2; i <= 3; i++) {

//			<ACCESS LIST OF EMAIL CONDITIONS>
				selectType(driver, i);

//			<ACESS THE OPTIONS ACCORDINGLY OPTIONS CHOOSE FIRST FILED>
				WebElement webElementEmailConditionsSelect;
				if (i != 3) {
					webElementEmailConditionsSelect = driver.findElement(By.xpath(
							"/html/body/div/div/div[1]/div[2]/main/div/div[2]/div/div[1]/section/div/div[1]/div[1]/div/div[2]/form/div[1]/div[2]/div[2]/div/div/div/div/div/div/div/form/div/div[2]/div/div[1]/fieldset/div/div["
									+ i + "]/ng-switch/label/ng-switch/span/div/span/span/select"));

				} else {
					webElementEmailConditionsSelect = driver.findElement(By.xpath(
							"/html/body/div/div/div[1]/div[2]/main/div/div[2]/div/div[1]/section/div/div[1]/div[1]/div/div[2]/form/div[1]/div[2]/div[2]/div/div/div/div/div/div/div/form/div/div[2]/div/div[1]/fieldset/div/div["
									+ i + "]/ng-switch/label/ng-switch/span/div/span/span/select"));
				}

				EmailConditionsSelect(i, webElementEmailConditionsSelect);

				if (i != 3) {
					andButtonClick(driver);
				}
//			
			}

//		<ROTINE TO ADD SUBJECT AND BODY TEXT>
			List<String> bodyTextConditions = new ArrayList<String>();
			handleStringJsonDataBodyText(bodyText, bodyTextConditions);

			List<String> subjectTextConditions = new ArrayList<String>();
			handleStringJsonDataSubject(subject, subjectTextConditions);

			andButtonClick(driver);

			int	subjectTextCount	= subjectTextConditions.size();
			int	bodyTextCount		= bodyTextConditions.size();

			int	j					= 0;
			for (int i = 0; i < subjectTextCount; i++) {
				if (i == 0 || i == 1) {
					addSubject(driver, subjectTextConditions, i);
				} else {
					j = 3 + i;
					addSubjectNew(driver, subjectTextConditions, j, i);
				}
			}

			for (int i = 0; i < bodyTextCount; i++) {
				if (i == 0) {
					j += 1;
					addBodyTextNew(driver, bodyTextConditions, j, i);
				} else {
					j++;
					addBodyTextNew(driver, bodyTextConditions, j, i);
				}

			}

			Actions builder = new Actions(driver);

			doneButton(driver);

			action(driver);

			createIncidentsFields(driver);

			addFieldsForEachOptions(driver, builder);

			moveToAttachment(driver, builder);

			associeteRecordEmail(driver, builder);

			saveFlow(driver);
			
			Thread.sleep(10000);
			WebElement closeFlw = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions
					.elementToBeClickable(By.xpath("//*[@id=\"react-tabs-2\"]/button")));
			closeFlw.click();

			driver.get(kyndrylMxDevUrl);
			
			Thread.sleep(5000);
			
			ADDBUTTON = 1; 
			ORBUTTON = 3;
		}

	}

	private static void doneButton(WebDriver driver) {
		WebElement done;
		try {
			done = new WebDriverWait(driver, Duration.ofSeconds(10))
					.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"flow_action_btnDone\"]")));

		} catch (Exception e) {
			done = new WebDriverWait(driver, Duration.ofSeconds(10))
					.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"flow_trigger_btn_done\"]")));
		}

		done.click();
	}

	private static void action(WebDriver driver) throws InterruptedException {
		newActionMethod(driver);

		WebElement createRecord = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions
				.elementToBeClickable(By.xpath("//*[@id=\"action-picker-search-results\"]/ul[2]/ul/li[5]/button[1]")));
		createRecord.click();

		Thread.sleep(5000);
		WebElement selectTable = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"s2id_label_table_name_0\"]/a")));
		selectTable.click();

		driver.findElement(By.xpath("/html/body/div[3]/div/input")).sendKeys("Incident [incident]");

		WebElement selectIncident = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[3]/ul/li/div")));
		selectIncident.click();

	}

	private static void createIncidentsFields(WebDriver driver) throws InterruptedException {
		String[] fields = { "Short description", "Description", "Reported By", "Affected user", "Channel" };

		for (int i = 0; i <= 4; i++) {
			WebElement addInput = new WebDriverWait(driver, Duration.ofSeconds(10))
					.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"values\"]")));
			addInput.click();

			Thread.sleep(1000);
			WebElement insertDescription = new WebDriverWait(driver, Duration.ofSeconds(10))
					.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"values__" + i + "\"]")));
			insertDescription.click();

			int count = i + 3;

			driver.findElement(By.xpath("/html/body/div[" + count + "]/div/input")).sendKeys(fields[i]);

			WebElement selectDescriptionSearched = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
					ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[" + count + "]/ul/li/div")));
			selectDescriptionSearched.click();
		}

	}

	private static void addFieldsForEachOptions(WebDriver driver, Actions builder) throws Exception {

		emailRecordClick(driver);
		WebElement	subject = null;
		
		for(int sub = 4; sub < 200; sub++) {
			String subjectValue = driver.findElement(By.xpath("/html/body/div[1]/div/div[1]/div[2]/main/div/div[2]/div/div[2]/div/section/div[2]/div/div/div[2]/ul/li[4]/div[2]/ul/li["+sub+"]/div[1]/div/div/span[1]")).getText();
			if(!subjectValue.contains("Subject")){
				continue;
			}else {
				subject	= driver.findElement(By.xpath("/html/body/div[1]/div/div[1]/div[2]/main/div/div[2]/div/div[2]/div/section/div[2]/div/div/div[2]/ul/li[4]/div[2]/ul/li["+sub+"]/div[1]/div/div/span[1]"));
				break;
			}	
		}
		
		WebElement	subjectField	= driver.findElement(By.xpath("//*[@id=\"short_description\"]"));

		builder.dragAndDrop(subject, subjectField).perform();

		String bjectField = driver.findElement(By.xpath("//*[@id=\"short_description\"]")).getText();
		if (!bjectField.contains("Subject")) {			
			subject			= driver.findElement(By.xpath(
					"/html/body/div[1]/div/div[1]/div[2]/main/div/div[2]/div/div[2]/div/section/div[2]/div/div/div[2]/ul/li[4]/div[2]/ul/li[23]/div[1]/div/div/span[1]"));
			builder.dragAndDrop(subject, subjectField).perform();
		}
		
		Thread.sleep(1000);
		emailRecordClick(driver);

		WebElement	bodyText		= driver.findElement(By.xpath(
				"/html/body/div[1]/div/div[1]/div[2]/main/div/div[2]/div/div[2]/div/section/div[2]/div/div/div[2]/ul/li[4]/div[2]/ul/li[3]/div[1]/div/div/span[1]"));
		WebElement	bodyTextField	= driver.findElement(By.xpath("//*[@id=\"description\"]"));

		builder.dragAndDrop(bodyText, bodyTextField).perform();
		Thread.sleep(1000);
		emailRecordClick(driver);

		WebElement userId = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.xpath(
						"//*[@id=\"editor-body\"]/div/div[2]/div/section/div[2]/div/div/div[2]/ul/li[4]/div[2]/ul/li[34]/div[1]/div[2]/div")));
		userId.click();

		WebElement	sysId		= driver.findElement(By.xpath(
				"/html/body/div[1]/div/div[1]/div[2]/main/div/div[2]/div/div[2]/div/section/div[2]/div/div/div[2]/ul/li[4]/div[2]/ul/li[34]/div[2]/ul/li[55]/div[1]/div/div/span[1]"));
		WebElement	sysIdField	= driver.findElement(By.xpath("//*[@id=\"s2id_values_u_qs_reported_by_2\"]"));

		builder.dragAndDrop(sysId, sysIdField).perform();
		Thread.sleep(1000);
		emailRecordClick(driver);

		userId = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(By
				.xpath("//*[@id=\"editor-body\"]/div/div[2]/div/section/div[2]/div/div/div[2]/ul/li[4]/div[2]/ul/li[34]/div[1]/div[2]/div")));
		userId.click();

		sysId = driver.findElement(By.xpath(
				"/html/body/div[1]/div/div[1]/div[2]/main/div/div[2]/div/div[2]/div/section/div[2]/div/div/div[2]/ul/li[4]/div[2]/ul/li[34]/div[2]/ul/li[55]/div[1]/div/div/span[1]"));
		WebElement affecterUserField = driver.findElement(By.xpath("//*[@id=\"s2id_values_caller_id_3\"]"));

		builder.dragAndDrop(sysId, affecterUserField).perform();

		WebElement contactType = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"s2id_contact_type\"]")));
		contactType.click();

		WebElement emailSelected = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[3]/ul/li[3]/div")));
		emailSelected.click();

		doneButton(driver);

	}

	private static void emailRecordClick(WebDriver driver) {
		WebElement emailRecord = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.xpath(
						"//*[@id=\"editor-body\"]/div/div[2]/div/section/div[2]/div/div/div[2]/ul/li[4]/div[1]/div[2]/div")));
		emailRecord.click();
	}

	private static void moveToAttachment(WebDriver driver, Actions builder) {
		newActionMethod(driver);

		WebElement moveAttachment = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions
				.elementToBeClickable(By.xpath("//*[@id=\"action-picker-search-results\"]/ul[2]/ul/li[25]/button[1]")));
		moveAttachment.click();

		WebElement	emailRecord		= driver.findElement(By.xpath(
				"/html/body/div[1]/div/div[1]/div[2]/main/div/div[2]/div/div[2]/div/section/div[2]/div/div/div[2]/ul/li[4]/div[1]/div[2]/div/span[1]"));
		WebElement	attchmentField	= driver
				.findElement(By.xpath("//*[@id=\"s2id_label_source_attachment_record_0\"]"));

		builder.dragAndDrop(emailRecord, attchmentField).perform();

		WebElement	incidentRecord		= driver.findElement(By.xpath(
				"/html/body/div[1]/div/div[1]/div[2]/main/div/div[2]/div/div[2]/div/section/div[2]/div/div/div[3]/ul/li[1]/div[1]/div[2]/div/span[1]"));
		WebElement	targetRecordField	= driver.findElement(By.xpath("//*[@id=\"target_record\"]"));

		builder.dragAndDrop(incidentRecord, targetRecordField).perform();

		doneButton(driver);
	}

	private static void associeteRecordEmail(WebDriver driver, Actions builder) {
		newActionMethod(driver);

		WebElement associeteRecordToEmail = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions
				.elementToBeClickable(By.xpath("//*[@id=\"action-picker-search-results\"]/ul[2]/ul/li[29]/button[1]")));
		associeteRecordToEmail.click();

		WebElement	emailRecord			= driver.findElement(By.xpath(
				"/html/body/div[1]/div/div[1]/div[2]/main/div/div[2]/div/div[2]/div/section/div[2]/div/div/div[2]/ul/li[4]/div[1]/div[2]/div/span[1]"));
		WebElement	emailRecordField	= driver.findElement(By.xpath("//*[@id=\"email_record\"]"));

		builder.dragAndDrop(emailRecord, emailRecordField).perform();

		WebElement	incidentRecord		= driver.findElement(By.xpath(
				"/html/body/div[1]/div/div[1]/div[2]/main/div/div[2]/div/div[2]/div/section/div[2]/div/div/div[3]/ul/li[1]/div[1]/div[2]/div/span[1]"));
		WebElement	targetRecordField	= driver.findElement(By.xpath("//*[@id=\"target_record\"]"));

		builder.dragAndDrop(incidentRecord, targetRecordField).perform();

	}

	private static void saveFlow(WebDriver driver) {
		WebElement save = new WebDriverWait(driver, Duration.ofSeconds(15))
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"editor_save_btn\"]")));
		save.click();

		WebElement active = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"editor_publish_btn\"]")));
		active.click();

		WebElement confirmActive = new WebDriverWait(driver, Duration.ofSeconds(10)).until(
				ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"doNotShowActivate_confirmButton\"]")));
		confirmActive.click();
	}

	private static void newActionMethod(WebDriver driver) {
		driver.findElement(By.xpath("//*[@id=\"flow_action_btnToggleAction\"]")).click();

		WebElement newAction = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"flow_action_newAction\"]")));
		newAction.click();

		WebElement serviceNowCore = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions
				.elementToBeClickable(By.xpath("//*[@id=\"action-picker-search-results\"]/ul[1]/li[4]/button")));
		serviceNowCore.click();
	}

	private static void addSubject(WebDriver driver, List<String> subjectTextConditions, int i) {
		String buttonText = driver
				.findElement(By.xpath("//*[@id=\"6-content\"]/div/div[1]/fieldset/div/div[4]/span[1]/span/button"))
				.getText();
		if (!buttonText.contains("Subject")) {
			List<WebElement> filterField = filterFieldContainerSys(driver);
			for (WebElement el : filterField) {
				String type = el.getText();
				if (type.equals("Subject")) {
					WebElement typeElement = el
							.findElement(By.xpath("//*[@id=\"filter-field-container-sys_email\"]/ul/li[24]/a"));
					typeElement.click();
					break;
				}

			}
		}

		Select		ruleDataValueSelect	= new Select(driver.findElement(By.xpath(
				"/html/body/div/div/div[1]/div[2]/main/div/div[2]/div/div[1]/section/div/div[1]/div[1]/div/div[2]/form/div[1]/div[2]/div[2]/div/div/div/div/div/div/div/form/div/div[2]/div/div[1]/fieldset/div/div[4]/span[2]/select")));
		WebElement	optionRules			= ruleDataValueSelect.getFirstSelectedOption();
		String		ruleDataValue		= optionRules.getText();

		if (!ruleDataValue.equals("contains")) {
			int					r						= 2 + i;
			Select				rulesOptionsDropdown	= ruleSelect(driver, r);
			List<WebElement>	ruleOptionList			= rulesOptionsDropdown.getOptions();
			selectRules(rulesOptionsDropdown, ruleOptionList);
		}

		driver.findElement(By.xpath(
				"//*[@id=\"6-content\"]/div/div[1]/fieldset/div/div[4]/ng-switch/label/ng-switch/span/div/span/span/input"))
				.click();
		driver.findElement(By.xpath(
				"//*[@id=\"6-content\"]/div/div[1]/fieldset/div/div[4]/ng-switch/label/ng-switch/span/div/span/span/input"))
				.sendKeys(subjectTextConditions.get(i));
		orButtonClick(driver);

	}

	private static void addSubjectNew(WebDriver driver, List<String> subjectTextConditions, int j, int i) throws Exception {
		String buttonText = driver
				.findElement(
						By.xpath("//*[@id=\"6-content\"]/div/div[1]/fieldset/div/div[" + j + "]/span[1]/span/button"))
				.getText();
		if (!buttonText.contains("Subject")) {
			List<WebElement> filterField = filterFieldContainerSysNew(driver, j);
			for (WebElement el : filterField) {
				String type = el.getText();
				if (type.equals("Subject")) {
					WebElement typeElement = el
							.findElement(By.xpath("//*[@id=\"filter-field-container-sys_email\"]/ul/li[24]/a"));
					typeElement.click();
					break;
				}

			}
		}

		Select		ruleDataValueSelect	= new Select(driver.findElement(By.xpath(
				"/html/body/div/div/div[1]/div[2]/main/div/div[2]/div/div[1]/section/div/div[1]/div[1]/div/div[2]/form/div[1]/div[2]/div[2]/div/div/div/div/div/div/div/form/div/div[2]/div/div[1]/fieldset/div/div["
						+ j + "]/span[2]/select")));
		WebElement	optionRules			= ruleDataValueSelect.getFirstSelectedOption();
		String		ruleDataValue		= optionRules.getText();

		if (!ruleDataValue.equals("contains")) {
			int					r						= 2 + i;
			Select				rulesOptionsDropdown	= ruleSelect(driver, r);
			List<WebElement>	ruleOptionList			= rulesOptionsDropdown.getOptions();
			selectRules(rulesOptionsDropdown, ruleOptionList);
		}

		driver.findElement(By.xpath("//*[@id=\"6-content\"]/div/div[1]/fieldset/div/div[" + j
				+ "]/ng-switch/label/ng-switch/span/div/span/span/input")).click();
		driver.findElement(By.xpath("//*[@id=\"6-content\"]/div/div[1]/fieldset/div/div[" + j
				+ "]/ng-switch/label/ng-switch/span/div/span/span/input")).sendKeys(subjectTextConditions.get(i));
		orButtonClickNew(driver, j);

	}

	private static void addBodyTextNew(WebDriver driver, List<String> subjectTextConditions, int j, int i) throws Exception {
		String buttonText = driver
				.findElement(
						By.xpath("//*[@id=\"6-content\"]/div/div[1]/fieldset/div/div[" + j + "]/span[1]/span/button"))
				.getText();
		if (!buttonText.contains("Body text")) {
			List<WebElement> filterField = filterFieldContainerSysNew(driver, j);
			for (WebElement el : filterField) {
				String type = el.getText();
				if (type.equals("Body text")) {
					WebElement typeElement = el
							.findElement(By.xpath("//*[@id=\"filter-field-container-sys_email\"]/ul/li[3]/a"));
					typeElement.click();
					break;
				}

			}
		}

		Select		ruleDataValueSelect	= new Select(driver.findElement(By.xpath(
				"/html/body/div/div/div[1]/div[2]/main/div/div[2]/div/div[1]/section/div/div[1]/div[1]/div/div[2]/form/div[1]/div[2]/div[2]/div/div/div/div/div/div/div/form/div/div[2]/div/div[1]/fieldset/div/div["
						+ j + "]/span[2]/select")));
		WebElement	optionRules			= ruleDataValueSelect.getFirstSelectedOption();
		String		ruleDataValue		= optionRules.getText();

		if (!ruleDataValue.equals("contains")) {
			Select				rulesOptionsDropdown	= ruleSelectNew(driver, j);
			List<WebElement>	ruleOptionList			= rulesOptionsDropdown.getOptions();
			selectRules(rulesOptionsDropdown, ruleOptionList);
		}

		driver.findElement(By.xpath("//*[@id=\"6-content\"]/div/div[1]/fieldset/div/div[" + j
				+ "]/ng-switch/label/ng-switch/span/div/span/span/input")).click();
		driver.findElement(By.xpath("//*[@id=\"6-content\"]/div/div[1]/fieldset/div/div[" + j
				+ "]/ng-switch/label/ng-switch/span/div/span/span/input")).sendKeys(subjectTextConditions.get(i));
		orButtonClickNew(driver, j);

	}

	private static List<WebElement> filterFieldContainerSys(WebDriver driver) {
		WebElement emailConditionsClick = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
						"div.ng-filter-widget-row:nth-child(4) > span:nth-child(1) > span:nth-child(1) > button:nth-child(1)")));
		emailConditionsClick.click();

		WebElement			filterFieldContainerSysEmail	= driver
				.findElement(By.id("filter-field-container-sys_email"));
		List<WebElement>	filterField						= filterFieldContainerSysEmail
				.findElements(By.cssSelector("li > a"));

		return filterField;
	}

	private static List<WebElement> filterFieldContainerSysNew(WebDriver driver, int j) throws Exception {
		WebElement emailConditionsClick;
		try {
			Thread.sleep(3000);
			 emailConditionsClick = new WebDriverWait(driver, Duration.ofSeconds(10))
					.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.ng-filter-widget-row:nth-child(" + j + ") > span:nth-child(1) > span:nth-child(1) > button:nth-child(1)")));
			 System.out.println("passou no try");
		} catch (Exception e) {
			Thread.sleep(3000); 
			emailConditionsClick = new WebDriverWait(driver, Duration.ofSeconds(10))
					.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.ng-filter-widget-row:nth-child(" + j	+ ") > span:nth-child(1) > span:nth-child(1) > button:nth-child(1)")));
			 System.out.println("passou no catch");
		}
		
		emailConditionsClick.click();

		WebElement			filterFieldContainerSysEmail	= driver.findElement(By.id("filter-field-container-sys_email"));
		System.out.println("passou no filterFieldContainerSysEmail");
		List<WebElement>	filterField						= filterFieldContainerSysEmail.findElements(By.cssSelector("li > a"));
		System.out.println("passou no filterField");

		return filterField;
	}

	private static void EmailConditionsSelect(int i, WebElement webElementEmailConditionsSelect) {
		Select				EmailConditionsSelect		= new Select(webElementEmailConditionsSelect);
		List<WebElement>	optionsListConditionsSelect	= EmailConditionsSelect.getOptions();
		for (WebElement emailCondition : optionsListConditionsSelect) {
			String optionValue = emailCondition.getText();
			if (i == 2) {
				if (optionValue.contains("received")) {
					EmailConditionsSelect.selectByVisibleText(optionValue);
					break;
				}
			} else {
				if (optionValue.equals("SMTP")) {
					EmailConditionsSelect.selectByVisibleText(optionValue);
					break;
				}
			}
		}
	}

	private static void selectType(WebDriver driver, int i) {
		List<WebElement> filterField = filterFieldContainerSysEmail(driver, i);
		for (WebElement el : filterField) {
			String type = el.getText();
			if (i == 2) {
				if (type.equals("Type")) {
					WebElement typeElement = el
							.findElement(By.xpath("//*[@id=\"filter-field-container-sys_email\"]/ul/li[29]/a"));
					typeElement.click();
					break;
				}
			} else {
				if (type.equals("Notification type")) {
					WebElement typeElement = el
							.findElement(By.xpath("//*[@id=\"filter-field-container-sys_email\"]/ul/li[18]/a"));
					typeElement.click();
					break;
				}
			}

		}
	}

	private static List<WebElement> getListOfTypeTriggerCanBeCreated(WebDriver driver, String typeTrigger) {

		WebElement pickerScopeList;

		if (typeTrigger.equals("Application")) {
			pickerScopeList = driver.findElement(By.className("picker-scope-list"));
		} else {
			pickerScopeList = driver.findElement(By.className("picker-result-list"));
		}
		List<WebElement> pickerScopeListLi = pickerScopeList.findElements(By.tagName("li"));
		return pickerScopeListLi;
	}

	private static List<WebElement> getListOfTypeFlowCanBeCreated(WebDriver driver) {
		WebElement			flowOptions		= driver.findElement(By.id("menu-items"));
		List<WebElement>	allListItems	= flowOptions.findElements(By.tagName("li"));
		return allListItems;
	}

	private static List<WebElement> filterFieldContainerSysEmail(WebDriver driver, int i) {
		WebElement emailConditionsClick = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.ng-filter-widget-row:nth-child(" + i
						+ ") > span:nth-child(1) > span:nth-child(1) > button:nth-child(1)")));
		emailConditionsClick.click();
		WebElement			filterFieldContainerSysEmail	= driver
				.findElement(By.id("filter-field-container-sys_email"));
		List<WebElement>	filterField						= filterFieldContainerSysEmail
				.findElements(By.cssSelector("li > a"));
		return filterField;
	}

	private static void andButtonClick(WebDriver driver) {
		ADDBUTTON += 1;
		WebElement andButton = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.ng-filter-widget-row:nth-child("
						+ ADDBUTTON + ") > span:nth-child(4) > button:nth-child(3)")));
		andButton.click();
	}

	private static void orButtonClick(WebDriver driver) {
		WebElement andButton = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
						"div.ng-filter-widget-row:nth-child(4) > span:nth-child(4) > button:nth-child(2)")));
		andButton.click();
	}

	private static void orButtonClickNew(WebDriver driver, int j) {
		WebElement andButton = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
						"div.ng-filter-widget-row:nth-child(" + j + ") > span:nth-child(4) > button:nth-child(2)")));
		andButton.click();
	}

	private static List<String> handleStringJsonDataSubject(String subject, List<String> conditionsText) {

		String		subjectWords		= subject.toString();
		String[]	subjectWordsSplit	= subjectWords.split(",");

		String		subUpperCase		= null;
		String		subLowerCase		= null;
		String		subFirstLowerCase	= null;

		for (String sub : subjectWordsSplit) {
			subUpperCase = sub;
			subLowerCase = sub.toLowerCase();
			subFirstLowerCase = subLowerCase.substring(0, 1).toUpperCase() + subLowerCase.substring(1);

			conditionsText.add(subUpperCase);
			conditionsText.add(subLowerCase);
			conditionsText.add(subFirstLowerCase);
		}
		return conditionsText;
	}

	private static List<String> handleStringJsonDataBodyText(String bodyText, List<String> conditionsText) {

		String		bodyTextWords		= bodyText.toString();
		String[]	bodyTextWordsSplit		= bodyTextWords.split(",");

		String		bodyTextUpperCase		= null;
		String		bodyTextLowerCase		= null;
		String		bodyTextFirstUpperCase	= null;

		for (String sub : bodyTextWordsSplit) {
			bodyTextUpperCase = sub;
			bodyTextLowerCase = sub.toLowerCase();
			bodyTextFirstUpperCase = bodyTextLowerCase.substring(0, 1).toUpperCase() + bodyTextLowerCase.substring(1);

			conditionsText.add(bodyTextUpperCase);
			conditionsText.add(bodyTextLowerCase);
			conditionsText.add(bodyTextFirstUpperCase);
		}
		return conditionsText;
	}

	private static Select domainSelect(WebDriver driver) {
		WebElement domainPickerSelectHeader = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.id("domain_picker_select_header")));
		domainPickerSelectHeader.click();
		WebElement	webElementDomainOptions	= driver.findElement(By.id("domain_picker_select_header"));
		Select		domainOptionsDropdown	= new Select(webElementDomainOptions);
		return domainOptionsDropdown;
	}

	private static void loginPage(WebDriver driver) {
		driver.findElement(By.id("user_name")).sendKeys("rafael.batistoti@kyndryl.com");
		driver.findElement(By.id("user_password")).sendKeys("Nokia@2014");
		driver.findElement(By.id("sysverb_login")).click();
	}

	private static void applicationOptionsList(List<WebElement> applicationList, WebDriver driver, String typeTrigger) {
		for (WebElement app : applicationList) {
			String applicationValue = app.getText();
			if (applicationValue.equals(typeTrigger)) {

				WebElement buttonElement = new WebDriverWait(driver, Duration.ofSeconds(10))
						.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
								"li.picker-component-result-info-btn:nth-child(2) > button:nth-child(1)")));
				buttonElement.click();
				break;
			}
		}
	}

	private static void pickerScopeList(List<WebElement> pickerScopeListLi, WebDriver driver, String typeTrigger) {
		for (WebElement picker : pickerScopeListLi) {
			String pickerValue = picker.getText();
			if (pickerValue.equals(typeTrigger)) {
				WebElement buttonElement;
				try {
					buttonElement = new WebDriverWait(driver, Duration.ofSeconds(10))
							.until(ExpectedConditions.elementToBeClickable(
									By.cssSelector("li.picker-component-result:nth-child(3) > button:nth-child(1)")));
				} catch (Exception e) {
					buttonElement = new WebDriverWait(driver, Duration.ofSeconds(10))
							.until(ExpectedConditions.elementToBeClickable(
									By.xpath("//*[@id=\"action-picker-search-results\"]/ul[1]/li[3]/button")));
				}

				buttonElement.click();
				break;
			}
		}
	}

	private static void selectNewFlow(WebDriver driver, List<WebElement> allListItems) {
		for (WebElement listItem : allListItems) {
			String optionText = listItem.getText();
			if (optionText.equals("Flow")) {
				String elementId = listItem.getAttribute("id");
				driver.findElement(By.id(elementId)).click();
				break;
			}
		}
	}

	private static Select ruleSelect(WebDriver driver, int r) {
		WebElement domainPickerSelectHeader = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.id("cond_operator_" + r)));
		domainPickerSelectHeader.click();

		WebElement	webElementDomainOptions	= driver.findElement(By.id("cond_operator_" + r));

		Select		domainOptionsDropdown	= new Select(webElementDomainOptions);
		return domainOptionsDropdown;
	}

	private static Select ruleSelectNew(WebDriver driver, int r) {

		WebElement domainPickerSelectHeader = new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.elementToBeClickable(By.xpath(
						"/html/body/div/div/div[1]/div[2]/main/div/div[2]/div/div[1]/section/div/div[1]/div[1]/div/div[2]/form/div[1]/div[2]/div[2]/div/div/div/div/div/div/div/form/div/div[2]/div/div[1]/fieldset/div[2]/div["
								+ r + "]/span[2]/select")));
		domainPickerSelectHeader.click();

		WebElement	webElementDomainOptions	= driver.findElement(By.xpath(
				"/html/body/div/div/div[1]/div[2]/main/div/div[2]/div/div[1]/section/div/div[1]/div[1]/div/div[2]/form/div[1]/div[2]/div[2]/div/div/div/div/div/div/div/form/div/div[2]/div/div[1]/fieldset/div[2]/div["
						+ r + "]/span[2]/select"));

		Select		domainOptionsDropdown	= new Select(webElementDomainOptions);
		return domainOptionsDropdown;
	}

	private static String selectAdoDomain(Select dropdown, List<WebElement> options) {
		String optionValue = null;
		for (WebElement option : options) {
			optionValue = option.getText();
			if (optionValue.contains("ADO")) {
				dropdown.selectByVisibleText(optionValue);
				break;
			}
		}
		return optionValue;
	}

	private static void selectRules(Select dropdown, List<WebElement> options) {
		String optionValue = null;
		for (WebElement option : options) {
			optionValue = option.getText();
			if (optionValue.contains("contains")) {
				dropdown.selectByVisibleText(optionValue);
				break;
			}
		}
	}
}
