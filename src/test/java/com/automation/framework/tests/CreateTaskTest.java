package com.automation.framework.tests;

import com.automation.framework.base.BaseTest;
import com.automation.framework.listeners.TestListener;
import com.automation.framework.pages.TaskPage;
import com.automation.framework.utils.JsonDataUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateTaskTest extends BaseTest {
    @DataProvider(name = "createTaskData", parallel = true)
    public Object[][] createTaskDataProvider() throws IOException {
        List<Map<String, String>> data = JsonDataUtil.readJsonData();
        List<Object[]> rows = new ArrayList<>();

        for (Map<String, String> item : data) {
            if ("create_task".equalsIgnoreCase(item.get("testCaseName"))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
                rows.add(new Object[]{
                        item.get("title") + "_" + timestamp,
                        item.get("description") + " " + timestamp,
                        item.get("priority"),
                        item.get("status"),
                        item.get("company"),
                        item.get("contact")
                });
            }
        }

        return rows.toArray(new Object[0][0]);
    }

    @Test(dataProvider = "createTaskData")
    public void createNewTaskAndVerify(String title, String description, String priority, String status,
                                       String company, String contact) {
        TaskPage taskPage = new TaskPage(getDriver());
        TestListener.logStep("Open the Tasks page");
        taskPage.openTasksPage();

        TestListener.logStep("Create the task");
        taskPage.createTask(title, description, priority, status, company, contact);

        TestListener.logStep("Verify the created task details");
        Assert.assertTrue(taskPage.isTaskVisible(title, description, priority, status, company, contact),
                "Created task should display the expected details: " + title);

        TestListener.logStep("Capture a screenshot after task creation is confirmed");
        captureScreenshot("task_created");
    }
}
