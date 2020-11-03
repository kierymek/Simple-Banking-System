package banking;

import java.sql.*;
import java.util.*;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();

    public static void main(String[] args) {
        String fileName = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-fileName")) {
                fileName = args[i + 1];            }
        }
        runSystem(fileName);
    }

    static void runSystem(String databaseName) {

        String url = "jdbc:sqlite:" + databaseName;

        try (Connection con = DriverManager.getConnection(url);
            // Statement creation
            Statement statement = con.createStatement()){
                // Statement execution
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "number TEXT NOT NULL," +
                        "pin TEXT NOT NULL," +
                        "balance INTEGER DEFAULT 0);");

        List<String> menuList = new ArrayList<>();
        menuList.add("1. Create an account");
        menuList.add("2. Log into account");
        menuList.add("0. Exit");

        boolean exited = false;
        boolean loggedIn = false;
        int clientNumber = 0;

        int ID = 1;
            try (ResultSet correctData = statement.executeQuery("SELECT * FROM card;")) {
                while (correctData.next()) {
                    ID++;
                }
            }
        while (!exited) {
            for (var s : menuList) {
                System.out.println(s);
            }
            int option = scanner.nextInt();
            if (!loggedIn) {
                switch (option) {
                    case 0:
                        exited = true;
                        break;
                    case 1:
                        StringBuilder number = new StringBuilder();
                        for (int i = 0; i < 15; i++) {
                            if (i == 0) number.append(4);
                            else if (i < 6) number.append(0);
                            else number.append(random.nextInt(10));
                        }
                        findChecksum(number);
                        StringBuilder pin = new StringBuilder();
                        for (int i = 0; i < 4; i++) {
                            pin.append(random.nextInt(10));
                        }
                        statement.executeUpdate("INSERT INTO card VALUES " +
                                "(" + ID +", '" + number.toString() + "', '" + pin.toString() + "', " + 0 + ");");

                        ID++;
                        System.out.println("Your card has been created");
                        System.out.println("Your card number:");
                        System.out.println(number);
                        System.out.println("Your card PIN:");
                        System.out.println(pin);
                        break;
                    case 2:
                        System.out.println("Enter your card number:");
                        String currentNumber = scanner.next();
                        System.out.println("Enter your PIN:");
                        String currentPin = scanner.next();

                        try (ResultSet correctData = statement.executeQuery("SELECT id, pin FROM card " +
                                "WHERE number =" + "'" + currentNumber + "';")) {
                            if (correctData.next()) {
                                // Retrieve column values
                                clientNumber = correctData.getInt("id");
                                String correctPin = correctData.getString("pin");
                                if (correctPin.equals(currentPin)) {
                                    System.out.println("You have successfully logged in!");
                                    loggedIn = true;
                                    menuList.clear();
                                    menuList.add("1. Balance");
                                    menuList.add("2. Add income");
                                    menuList.add("3. Do transfer");
                                    menuList.add("4. Close account");
                                    menuList.add("5. Log out");
                                    menuList.add("0. Exit");
                                } else {
                                    System.out.println("Wrong card number or PIN!");
                                }
                            } else {
                                System.out.println("Wrong card number or PIN!");
                            }
                            break;
                        }
                    default:
                        System.out.println("Wrong option!");
                        break;
                }
            } else {
                switch (option) {
                    case 1:
                        try (ResultSet correctData = statement.executeQuery("SELECT balance FROM card " +
                                "WHERE id =" + clientNumber + ";")) {
                            if (correctData.next()) {
                                // Retrieve column values
                                int currentBalance = correctData.getInt("balance");
                                System.out.println("Balance : " + currentBalance);
                            break;
                            }
                        }
                    case 2:
                        System.out.println("Enter income:");
                        int income = scanner.nextInt();
                        statement.executeUpdate("UPDATE card " +
                                "SET balance = balance + " + income +
                                " WHERE id = " + clientNumber);
                        System.out.println("Income was added!");
                        break;
                    case 3:
                        System.out.println("Transfer");
                        System.out.println("Enter card number:");
                        String targetAccount = scanner.next();
                        StringBuilder tmp = new StringBuilder(targetAccount.substring(0, 14));
                        findChecksum(tmp);
                            try (ResultSet correctData = statement.executeQuery("SELECT id FROM card " +
                                    "WHERE number =" + "'" + targetAccount + "';")) {
                                if (correctData.next()) {
                                    // Retrieve column values
                                    int targetAccountId = correctData.getInt("id");
                                    System.out.println("Enter how much money you want to transfer:");
                                    int amountOfMoney = scanner.nextInt();
                                    try (ResultSet SendersAccount = statement.executeQuery("SELECT balance, number FROM card " +
                                            "WHERE id = " + clientNumber + " ;")) {
                                        if (correctData.next()) {
                                            // Retrieve column values
                                            int currentBalance = SendersAccount.getInt("balance");
                                            String currentNumber = SendersAccount.getString("number");
                                            if (currentNumber.equals(targetAccount)) {
                                                System.out.println("You can't transfer money to the same account!");
                                                break;
                                            } else if (currentBalance < amountOfMoney) {
                                                System.out.println("Not enough money!");
                                                break;
                                            } else {
                                                statement.executeUpdate("UPDATE card SET balance" +
                                                        " = balance + " + amountOfMoney +
                                                        " WHERE id = " + targetAccountId + " ;");
                                                statement.executeUpdate("UPDATE card SET balance" +
                                                        " = balance - " + amountOfMoney +
                                                        " WHERE id = " + clientNumber + " ;");
                                                System.out.println("Success!");
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    System.out.println("Such a card does not exist.");
                                    if (!tmp.toString().equals(targetAccount)) {
                                        System.out.println("Probably you made mistake in the card number. Please try again!");
                                    }
                                }
                                break;
                            }
                    case 4:
                        statement.executeUpdate("DELETE FROM card WHERE id = " + clientNumber + ";");
                        System.out.println("The account has been closed!!");
                        menuList.clear();
                        menuList.add("1. Create an account");
                        menuList.add("2. Log into account");
                        menuList.add("0. Exit");
                        loggedIn = false;
                        break;
                    case 5:
                        loggedIn = false;
                        System.out.println("You have successfully logged out!");
                        menuList.clear();
                        menuList.add("1. Create an account");
                        menuList.add("2. Log into account");
                        menuList.add("0. Exit");
                        break;
                    case 0:
                        exited = true;
                        break;
                    default:
                        System.out.println("Wrong option!");
                        break;
                }
            }
        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void findChecksum(StringBuilder number) {
        int[] tmp = number.chars().map(x -> x - '0').toArray();
        int sum = 0;
        for (int i = 0; i < tmp.length; i++) {
            if (i % 2 == 0) {
                tmp[i] *= 2;
                if (tmp[i] > 9) tmp[i] -= 9;
            }
            sum += tmp[i];
        }
        int checksum = sum % 10 == 0? 0: 10 - sum % 10;
        number.append(checksum);
    }
}
