package dev.codescreen.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@Table(name = "USER_BALANCE")
@NoArgsConstructor
@AllArgsConstructor
public class UserBalance {
	@Id
	@Column(name="USER_ID")
	private String userId;
	
	@Column(name="BALANCE")
	private double balance;
}
