package me.swaggy;

import java.util.Date;

public class Loan {
    private double amountOwed;
    private double amountPaid;
    private Date dateTaken;

    public Loan(double amountOwed, double initialFee) {
        this.amountOwed = amountOwed + initialFee; // Total loan amount includes fee
        this.amountPaid = 0;
        this.dateTaken = new Date(); // Sets to current date
    }

    // Constructor for loading from file
    public Loan(double amountOwed, double amountPaid, String dateTaken) {
        this.amountOwed = amountOwed;
        this.amountPaid = amountPaid;
        this.dateTaken = new Date(dateTaken);
    }

    public double repay(double amount) {
        amountPaid += amount;
        amountOwed -= amount;
        return amountOwed;
    }

    public double getAmountOwed() {
        return amountOwed;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    public void setAmountOwed(double amountOwed) {
        this.amountOwed = amountOwed;
    }

    // Additional methods for managing loan data may be implemented here
}