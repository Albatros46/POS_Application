package com.sakcadag.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Customer first name is required")
    private String firstName;
    @NotBlank(message = "Customer last name is required")
    private String lastName;

    @NotBlank(message = "Customer name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;
    @Pattern(regexp = "^(\\+\\d{1,3})?\\d{10,15}$", message = "Invalid phone format")
    private String phone;


    @JsonIgnore
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY) //cascade = CascadeType.ALL: Eğer bir müşteri silinirse, ona bağlı tüm siparişler de silinir.
    private List<Order> orders = new ArrayList<>();
    public void fullNameMerge(){
        this.fullName=this.firstName+" "+this.lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        fullNameMerge();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        fullNameMerge();
    }
}
