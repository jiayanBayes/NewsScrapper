package common.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.Random;

public class HumanSimulator {

    private final WebDriver driver;
    private final Random random;
    private final Actions actions;

    public HumanSimulator(WebDriver driver) {
        this.driver = driver;
        this.random = new Random();
        this.actions = new Actions(driver);
    }

    /**
     * Simulates human scrolling behavior on the page.
     */
    public void simulateScroll() {
        int scrollSteps = random.nextInt(6) + 5; // Randomly scroll 5 to 10 times
        for (int i = 0; i < scrollSteps; i++) {
            int scrollAmount = random.nextInt(300) + 200; // Scroll by 200-500 pixels
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, arguments[0]);", scrollAmount);
            sleepRandom(1000, 3000); // Wait 1-3 seconds between scrolls
        }
        System.out.println("Simulated " + scrollSteps + " scrolls.");
    }

    /**
     * Simulates human-like key presses (e.g., PAGE_DOWN, UP, DOWN).
     */
    public void simulateKeyPress() {
        List<WebElement> bodyElements = driver.findElements(org.openqa.selenium.By.tagName("body"));
        if (!bodyElements.isEmpty()) {
            WebElement body = bodyElements.get(0);
            Keys[] keys = {Keys.PAGE_DOWN, Keys.PAGE_UP, Keys.ARROW_DOWN, Keys.ARROW_UP};
            Keys randomKey = keys[random.nextInt(keys.length)];
            body.sendKeys(randomKey);
            sleepRandom(1000, 2000); // Wait 1-2 seconds after key press
            System.out.println("Simulated a key press: " + randomKey.name());
        }
    }

    /**
     * Simulates a mouse movement to random coordinates on the page.
     */
    public void simulateMouseMovement() {
        int xOffset = random.nextInt(1000); // Random X coordinate
        int yOffset = random.nextInt(800);  // Random Y coordinate
        actions.moveByOffset(xOffset, yOffset).perform();
        sleepRandom(500, 1500); // Wait 0.5-1.5 seconds
        System.out.println("Simulated mouse movement to: (" + xOffset + ", " + yOffset + ")");
    }

    /**
     * Simulates clicking a random element on the page.
     */
    public void simulateRandomClick() {
        List<WebElement> clickableElements = driver.findElements(org.openqa.selenium.By.cssSelector("a, button, [role='button']"));
        if (!clickableElements.isEmpty()) {
            WebElement randomElement = clickableElements.get(random.nextInt(clickableElements.size()));
            actions.moveToElement(randomElement).click().perform();
            sleepRandom(1000, 2000); // Wait 1-2 seconds after click
            System.out.println("Simulated click on element: " + randomElement.getTagName());
        } else {
            System.out.println("No clickable elements found for random click simulation.");
        }
    }

    /**
     * Simulates text input in a text field.
     */
    public void simulateTextInput(String text) {
        List<WebElement> inputFields = driver.findElements(org.openqa.selenium.By.cssSelector("input[type='text'], textarea"));
        if (!inputFields.isEmpty()) {
            WebElement randomInputField = inputFields.get(random.nextInt(inputFields.size()));
            randomInputField.click();
            sleepRandom(500, 1000); // Wait 0.5-1 second before typing
            for (char c : text.toCharArray()) {
                randomInputField.sendKeys(String.valueOf(c));
                sleepRandom(100, 300); // Pause between typing each character
            }
            System.out.println("Simulated text input: " + text);
        } else {
            System.out.println("No text input fields found for simulation.");
        }
    }

    /**
     * Simulates a random delay.
     */
    public void simulateDelay() {
        sleepRandom(2000, 5000); // Wait 2-5 seconds
        System.out.println("Simulated random delay.");
    }

    /**
     * Helper method to sleep for a random duration.
     */
    private void sleepRandom(int minMillis, int maxMillis) {
        try {
            Thread.sleep(random.nextInt(maxMillis - minMillis) + minMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
