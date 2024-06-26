package com.example.demo.v1.models;

import com.example.demo.v1.enumerations.ETransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "banking")
public class Banking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Relationship with Customer
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER,  cascade = CascadeType.ALL)
    @JoinColumn(name = "receipt_id")
    private Customer receiptCustomer;

    private String account;

    private String receiptAccount;

    private ETransactionType type;

    private Double amount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "banking_date_time")
    private Date bankingDateTime;

    // Last Updated Timestamp date
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated")
    private Date lastUpdatedDateTime;

    @PrePersist
    protected void onCreate() {
        bankingDateTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedDateTime = new Date();
    }
}
