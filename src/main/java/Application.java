import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Application {

	private static final boolean HEADLESS_MODE = true;
	private static final String BASE_URL = "https://www.readlightnovel.me/peerless-martial-god-2-/chapter-";
	private static final String NOVEL_SHORTNAME = "pmg2";
	private static final int CHAPTER_START = 1;
	private static final int CHAPTER_END = 499;

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(final String... args) throws FileNotFoundException, DocumentException {
		final WebDriver driver = initWebdriver();
		logger.info("webdriver initialised");
		for (int i = CHAPTER_START; i <= CHAPTER_END; i++) {
			driver.get(BASE_URL + i);
			final String webText = extractText(driver);
			final String formattedText = formatText(webText);
			printDocument(formattedText, i);
			logger.info("printed chapter {}", i);
		}

		driver.close();
		logger.info("Application finished");
	}

	private static WebDriver initWebdriver() {
		WebDriverManager.chromedriver().setup();
		final ChromeOptions options = new ChromeOptions();
		if (HEADLESS_MODE) {
			options.addArguments("--headless");
		}
		return new ChromeDriver(options);
	}

	private static String extractText(final WebDriver driver) {
		final WebElement root = driver.findElement(By.xpath("//div[@class='desc']"));
		return root.getAttribute("innerText");
	}

	private static String formatText(final String text) {
		String formattedText = text.replaceFirst("\n", "");
		formattedText = formattedText.replaceAll("\n\n\n\n\n\n\n", "");
		formattedText = formattedText.replaceAll("\u00a0", "");
		formattedText =
				formattedText.replaceFirst("If audio player doesn't work, press Stop then Play button again", "");
		formattedText = StringUtils.substringBefore(formattedText, "AROUND THE WEB");
		formattedText = formattedText.replaceAll("\n\n\n\n", "\n\n");
		return formattedText;
	}

	private static void printDocument(final String text, final int chapterNumber)
			throws FileNotFoundException, DocumentException {

		final int folderNumber = ((chapterNumber/100)+1)*100;

		final String dirPath = String.format("./export/%s/%d/", NOVEL_SHORTNAME, folderNumber);

		final File directory = new File(dirPath);
		if(!directory.exists()) {
			directory.mkdirs();
		}

		final Document document = new Document();
		PdfWriter.getInstance(document, new FileOutputStream(dirPath+ NOVEL_SHORTNAME + "-" + chapterNumber + ".pdf"));

		document.open();

		final Font font = FontFactory.getFont(FontFactory.HELVETICA, 20, BaseColor.BLACK);
		final Chapter chapter = new Chapter(new Paragraph(text, font), chapterNumber);
		document.add(chapter);

		document.close();
	}

}
