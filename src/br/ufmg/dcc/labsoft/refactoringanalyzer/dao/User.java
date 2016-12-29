package br.ufmg.dcc.labsoft.refactoringanalyzer.dao;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "users", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "username" }) })
public class User extends AbstractEntity {

	public enum UserRole {
		ADMIN,
		NORMAL
	}
	private static final long serialVersionUID = 7784934699567379073L;
	
	private String userName;
	private String name;
	private String familyName;
	private String password;
	private String email;
	@Enumerated(EnumType.STRING)
	private UserRole userRole;

	public String getUserName() {
		return userName;
	}

	public String getName() {
		return name;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getPassword() {
		return password;
	}
	
	public String getEmail() {
		return email;
	}
	
	public UserRole getUserRole() {
		return userRole;
	}
		
}
