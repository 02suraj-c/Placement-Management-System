package org.example;

import org.example.cli.PlacementConsoleApp;
import org.example.service.PlacementManagementSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class PlacementConsoleAppTest {

    private String runConsoleWithInput(String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);

        PlacementManagementSystem pms = new PlacementManagementSystem();
        PlacementConsoleApp app = new PlacementConsoleApp(pms, in, ps);
        app.start();

        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Should display main menu and exit gracefully")
    void testMainMenuExit() {
        String output = runConsoleWithInput("0\n");

        assertTrue(output.contains("PLACEMENT MANAGEMENT SYSTEM CONSOLE"));
        assertTrue(output.contains("1. Company Management"));
        assertTrue(output.contains("2. Placement Drive Management"));
        assertTrue(output.contains("3. Student Eligibility Check"));
        assertTrue(output.contains("4. Application Management"));
        assertTrue(output.contains("5. Interview Round Management"));
        assertTrue(output.contains("Exiting Placement Management System. Goodbye!"));
    }

    @Test
    @DisplayName("Should handle 1-click sample demo data seeding")
    void testSeedSampleData() {
        String output = runConsoleWithInput("7\n0\n");

        assertTrue(output.contains("Sample demo data successfully loaded!"));
        assertTrue(output.contains("Google"));
        assertTrue(output.contains("Microsoft"));
        assertTrue(output.contains("Software Development Engineer"));
    }

    @Test
    @DisplayName("Should add and view companies via console menu")
    void testAddAndListCompanyMenu() {
        // Menu 1 (Company), 1 (Add), Name, Desc, Loc, Web, 2 (List), 0 (Back), 0 (Exit)
        String input = String.join("\n",
                "1",
                "1",
                "Uber Technologies",
                "Ride sharing & logistics",
                "Bangalore",
                "https://uber.com",
                "2",
                "0",
                "0",
                ""
        );

        String output = runConsoleWithInput(input);
        assertTrue(output.contains("Company registered successfully!"));
        assertTrue(output.contains("Uber Technologies"));
    }

    @Test
    @DisplayName("Should seed data and check student eligibility via console menu")
    void testEligibilityCheckViaConsole() {
        // 7 (Seed Data), 3 (Eligibility Check), STU-1, DRIVE-1, 0 (Exit)
        String input = String.join("\n",
                "7",
                "3",
                "STU-1",
                "DRIVE-1",
                "0",
                ""
        );

        String output = runConsoleWithInput(input);
        assertTrue(output.contains("Result: ELIGIBLE"));
    }

    @Test
    @DisplayName("Should handle invalid menu choices and inputs without crashing")
    void testInvalidMenuChoiceGraceful() {
        String output = runConsoleWithInput("invalid_choice\n0\n");

        assertTrue(output.contains("Invalid selection. Please enter a valid menu option."));
        assertTrue(output.contains("Exiting Placement Management System"));
    }
}
